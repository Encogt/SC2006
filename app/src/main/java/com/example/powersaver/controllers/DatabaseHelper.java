package com.example.powersaver.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.powersaver.objects.Appliance;
import com.example.powersaver.objects.Device;
import com.example.powersaver.objects.User;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_db";
    private static final int DATABASE_VERSION = 8;  // Increment version to force database upgrade

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_IS_ADMIN = "isAdmin";
    private static final String COLUMN_CONTACT = "contact";
    private static final String COLUMN_ADDRESS = "address";

    // Devices table
    public static final String TABLE_DEVICES = "devices";
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_DEVICE_NAME = "device_name";
    public static final String COLUMN_POWER_USAGE = "power_usage";
    public static final String COLUMN_DURATION = "duration";  // Total duration in minutes
    public static final String COLUMN_DEVICE_USER_ID = "device_user_id";  // User ID for devices

    // ApplianceRatings table
    public static final String TABLE_APPLIANCE_RATINGS = "ApplianceRatings";
    public static final String COLUMN_APPLIANCE_ID = "appliance_id";
    public static final String COLUMN_APPLIANCE_NAME = "appliance_name";
    public static final String COLUMN_POWER_RATING = "power_rating";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_IS_ADMIN + " INTEGER DEFAULT 0,"
                + COLUMN_CONTACT + " TEXT,"
                + COLUMN_ADDRESS + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Devices Table
        String CREATE_DEVICES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + "("
                + COLUMN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DEVICE_NAME + " TEXT,"
                + COLUMN_POWER_USAGE + " INTEGER,"
                + COLUMN_DURATION + " INTEGER,"  // Store duration as total minutes (integer)
                + COLUMN_DEVICE_USER_ID + " INTEGER"  // Tie devices to a User, to be used so
                //only user can view their own Device list
                + ")";
        db.execSQL(CREATE_DEVICES_TABLE);

        // Create ApplianceRatings Table
        String CREATE_APPLIANCE_RATINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_APPLIANCE_RATINGS + "("
                + COLUMN_APPLIANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_APPLIANCE_NAME + " TEXT UNIQUE,"  // Unique name for each appliance
                + COLUMN_POWER_RATING + " INTEGER"
                + ")";
        db.execSQL(CREATE_APPLIANCE_RATINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old tables and create new ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLIANCE_RATINGS);
        onCreate(db);
    }

    // Method to get user's email by userID
    public String getUserEmail(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = null;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_EMAIL + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(userID)});

        if (cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(0);  // Get the email from the first column of the result set
            cursor.close();
        }

        db.close();
        return email;
    }

    // Method to add or update an appliance in the database
    public boolean addOrUpdateAppliance(Appliance appliance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPLIANCE_NAME, appliance.getName());
        values.put(COLUMN_POWER_RATING, appliance.getPowerRating());

        long result = db.insertWithOnConflict(TABLE_APPLIANCE_RATINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1; // Returns true if successful, false if failed
    }
//method to delete appliance by ID
    public void removeAppliance(int applianceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPLIANCE_RATINGS, COLUMN_APPLIANCE_ID + " = ?", new String[]{String.valueOf(applianceId)});
        db.close();
    }

    // Method to update the duration of an existing device by ID
    public boolean updateDeviceDuration(int deviceID, String deviceName, int newDuration,int usage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, deviceName);
        values.put(COLUMN_DURATION, newDuration);
        values.put(COLUMN_POWER_USAGE,usage);

        int rowsUpdated = db.update(TABLE_DEVICES, values, COLUMN_DEVICE_ID + " = ?", new String[]{String.valueOf(deviceID)});
        db.close();
        return rowsUpdated > 0;
    }

    // Method to add or update an appliance rating (for admin use)
    public boolean addOrUpdateApplianceRating(String applianceName, int powerRating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPLIANCE_NAME, applianceName);
        values.put(COLUMN_POWER_RATING, powerRating);

        long result = db.insertWithOnConflict(TABLE_APPLIANCE_RATINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // Fetch all appliances and returns as object ArrayList
    public ArrayList<Appliance> getAllAppliances() {
        ArrayList<Appliance> applianceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPLIANCE_RATINGS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_APPLIANCE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLIANCE_NAME));
                int powerRating = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POWER_RATING));
                applianceList.add(new Appliance(id, name, powerRating));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return applianceList;
    }

    // Optional: Method to delete an appliance (if admin wants to remove an entry)
    public boolean deleteAppliance(String applianceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APPLIANCE_RATINGS, COLUMN_APPLIANCE_NAME + " = ?", new String[]{applianceName});
        return result > 0;
    }

    // Add a new device (stores duration in minutes)
    public boolean addDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, device.getDeviceName());
        values.put(COLUMN_POWER_USAGE, device.getUsage());
        values.put(COLUMN_DURATION, device.getDuration());
        values.put(COLUMN_DEVICE_USER_ID, device.getUserID()); // Updated to match Device class

        long result = db.insert(TABLE_DEVICES, null, values);
        return result != -1; // Return true if insertion was successful
    }

    // Remove a device by name
    public boolean removeDevice(String deviceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DEVICES, COLUMN_DEVICE_NAME + " = ?", new String[]{deviceName});
        return result > 0;
    }
    // Remove a device by ID
    public boolean removeDevice(int deviceID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DEVICES, COLUMN_DEVICE_ID + " = ?", new String[]{Integer.toString(deviceID)});
        return result > 0;
    }

    // Remove a device by ID with User ID verification
    public boolean removeDevice(int deviceID, int ownerUserID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DEVICES, COLUMN_DEVICE_ID + " = ? AND " + COLUMN_DEVICE_USER_ID + " = ?", new String[]{Integer.toString(deviceID), Integer.toString(ownerUserID)});
        return result > 0;
    }

    // Retrieve all devices
    public Cursor getAllDevices() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_DEVICES, null);
        return data;
    }
//gets all devices regardless of ID returns as List of Device objects (currently unused due userID binding)
    public ArrayList<Device> getAllDevicesObj() {
        ArrayList<Device> deviceList=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data=db.rawQuery("SELECT * FROM " + TABLE_DEVICES, null);
        if (data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndexOrThrow(COLUMN_DEVICE_ID));
                String name = data.getString(data.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));
                int power_usage = data.getInt(data.getColumnIndexOrThrow(COLUMN_POWER_USAGE));
                int duration = data.getInt(data.getColumnIndexOrThrow(COLUMN_DURATION));
                int ownerID = data.getInt(data.getColumnIndexOrThrow(COLUMN_DEVICE_USER_ID));
                Device dev = new Device(id, name, power_usage, duration, ownerID);
                deviceList.add(dev);
            } while (data.moveToNext());
        }
        data.close();
        return deviceList;
    }

    // Retrieve devices by user ID
    public ArrayList<Device> getDevicesByUser(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Device> deviceList = new ArrayList<>();
        String selection = COLUMN_DEVICE_USER_ID + " = ?";
        String[] selectionArgs = {Integer.toString(userID)};
        Cursor data = db.query(TABLE_DEVICES, null, selection, selectionArgs, null, null, null);
        if (data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndexOrThrow(COLUMN_DEVICE_ID));
                String name = data.getString(data.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));
                int power_usage = data.getInt(data.getColumnIndexOrThrow(COLUMN_POWER_USAGE));
                int duration = data.getInt(data.getColumnIndexOrThrow(COLUMN_DURATION));
                Device dev = new Device(id, name, power_usage,duration,userID);
                deviceList.add(dev);
                //return dev;
            } while (data.moveToNext());
        }
        data.close();
        return deviceList;
    }


    // User Operations (Unchanged)
    public boolean addUser(String username, String password, boolean isAdmin, String name, String email) {
        if(!uniqueUsernameCheck(username)) //if not unique fails
        {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_ADMIN, isAdmin ? 1 : 0);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CONTACT, "");
        values.put(COLUMN_ADDRESS, "");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }
    //Add user intaking User object
    public boolean addUser(User newUser)
    {
        if(!uniqueUsernameCheck(newUser.getUsername())) //if not unique fails
        {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, newUser.getUsername());
        values.put(COLUMN_PASSWORD, newUser.getPassword());
        values.put(COLUMN_IS_ADMIN, newUser.getIsAdmin());
        values.put(COLUMN_NAME, newUser.getName());
        values.put(COLUMN_EMAIL, newUser.getEmail());
        values.put(COLUMN_CONTACT, "");
        values.put(COLUMN_ADDRESS, "");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }
//Update User using individual details
    public boolean updateUserAccountDetails(String currentUsername, String newUsername, String newPassword, String newName, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //check to make sure new Username is unique;
        if(! uniqueUsernameCheck(newUsername)) //if not unique fails
        {
            return false;
        }
        if (newUsername != null && !newUsername.isEmpty()) values.put(COLUMN_USERNAME, newUsername);
        if (newPassword != null && !newPassword.isEmpty()) values.put(COLUMN_PASSWORD, newPassword);
        if (newName != null && !newName.isEmpty()) values.put(COLUMN_NAME, newName);
        if (newEmail != null && !newEmail.isEmpty()) values.put(COLUMN_EMAIL, newEmail);

        int result = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{currentUsername});
        return result > 0;
    }
//Update User using User Object
    public boolean updateUserAccountDetails(String currentUsername,User newUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //check to make sure new Username is unique;
        if(! uniqueUsernameCheck(newUser.getUsername())&&!newUser.getUsername().equals(currentUsername)) //if not unique fails
        {
            return false;
        }
        if (newUser.getUsername() != null && !newUser.getUsername().isEmpty()) values.put(COLUMN_USERNAME, newUser.getUsername());
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) values.put(COLUMN_PASSWORD, newUser.getPassword());
        if (newUser.getName() != null && !newUser.getName().isEmpty()) values.put(COLUMN_NAME, newUser.getName());
        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty()) values.put(COLUMN_EMAIL, newUser.getEmail());

        int result = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{currentUsername});
        return result > 0;
    }
//Verify user for login
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean isValid = (cursor.getCount() > 0);
        cursor.close();
        return isValid;
    }
//Retrieves user ID during Login
    public int getCheckedUserID(String username, String password)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        int userID=0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                userID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userID;
    }
//Login handler for Admin login
    public boolean checkAdminLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ? AND " + COLUMN_IS_ADMIN + " = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean isValid = (cursor.getCount() > 0);
        cursor.close();
        return isValid;
    }
//Update password that happens during account recovery
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int result = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        return result > 0;
    }
//Verify user details for account recovery
    public boolean validateUserInfo(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, email});
        boolean isValid = (cursor.getCount() > 0);
        cursor.close();
        return isValid;
    }
//Checks if account is admin
    public boolean isAdminExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_IS_ADMIN + " = 1 LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
//Retrieve all users (for admin viewing)
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }
    //Retrieve all users (for admin viewing) returns as list of objects
    public ArrayList<User> getAllUserObj()
    {
        ArrayList<User> userList=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data=db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        //Iterate through the cursor and add each user to the list
        if (data != null && data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndexOrThrow(COLUMN_ID));
                String username = data.getString(data.getColumnIndexOrThrow(COLUMN_USERNAME));
                String password = data.getString(data.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String name = data.getString(data.getColumnIndexOrThrow(COLUMN_NAME));
                String email = data.getString(data.getColumnIndexOrThrow(COLUMN_EMAIL));
                int isAdmin = data.getInt(data.getColumnIndexOrThrow(COLUMN_IS_ADMIN));
                String contact = data.getString(data.getColumnIndexOrThrow(COLUMN_CONTACT));
                String address = data.getString(data.getColumnIndexOrThrow(COLUMN_ADDRESS));


                User user = new User(id,username,password,name,email,isAdmin,contact,address);
                userList.add(user);
            } while (data.moveToNext());
        }
        data.close();
        return userList;
    }
    //returns User object based on username
    public User getUserByUsername(String un)
    {
        User user=null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?  ";
        Cursor data = db.rawQuery(query, new String[]{un});
        //Iterate through the cursor and add each user to the list
        if (data != null && data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndexOrThrow(COLUMN_ID));
                String username = data.getString(data.getColumnIndexOrThrow(COLUMN_USERNAME));
                String password = data.getString(data.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String name = data.getString(data.getColumnIndexOrThrow(COLUMN_NAME));
                String email = data.getString(data.getColumnIndexOrThrow(COLUMN_EMAIL));
                int isAdmin = data.getInt(data.getColumnIndexOrThrow(COLUMN_IS_ADMIN));
                String contact = data.getString(data.getColumnIndexOrThrow(COLUMN_CONTACT));
                String address = data.getString(data.getColumnIndexOrThrow(COLUMN_ADDRESS));


                user = new User(id,username,password,name,email,isAdmin,contact,address);
            } while (data.moveToNext());
        }
        data.close();
        return user;
    }
//Checks if username is unique using getUserByUsername
    public boolean uniqueUsernameCheck(String userName)
    {
        if(getUserByUsername(userName)==null)
        {
            return true; //if no user with username is found return true for it is unique
        }
        else
        {
            return false; //if user was found with username returns false
        }
    }



}
