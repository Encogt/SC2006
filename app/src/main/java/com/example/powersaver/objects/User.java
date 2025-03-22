package com.example.powersaver.objects;

public class User {
    private Integer userID;
    private String username;
    private String password;
    private String name;
    private String email;
    private int isAdmin;
    private String contactNumber;
    private String address;

    public User()
    {

    }
    public User(String id,String username, String password, String name, String email, int isAdmin, String contactNumber, String address)
    {
        this.userID=Integer.parseInt(id);
        this.username=username;
        this.password=password;
        this.name=name;
        this.email=email;
        this.contactNumber=contactNumber;
        this.isAdmin=isAdmin;
        this.address=address;
    }
    public User(String username, String password, String name, String email, int isAdmin, String contactNumber, String address)
    {
        this.username=username;
        this.password=password;
        this.name=name;
        this.email=email;
        this.contactNumber=contactNumber;
        this.isAdmin=isAdmin;
        this.address=address;
    }
    public User(int id,String username, String password, String name, String email, int isAdmin, String contactNumber, String address)
    {
        this.userID=id;
        this.username=username;
        this.password=password;
        this.name=name;
        this.email=email;
        this.contactNumber=contactNumber;
        this.isAdmin=isAdmin;
        this.address=address;
    }
    public User(String id,String username, String password, String name, String email, int isAdmin)
    {
        this.userID=Integer.parseInt(id);
        this.username=username;
        this.password=password;
        this.name=name;
        this.email=email;
        this.isAdmin=isAdmin;
    }
    public User(int id,String username, String password, String name, String email, int isAdmin)
    {
        this.userID=id;
        this.username=username;
        this.password=password;
        this.name=name;
        this.email=email;
        this.isAdmin=isAdmin;
    }

    public Integer getUserID() {
        return userID;
    }
    public void setUserID(Integer id)
    {
        userID=id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String un)
    {
        username=un;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String n)
    {
        name=n;
    }
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String e)
    {
        email=e;
    }
    public String getContactNumber()
    {
        return contactNumber;
    }
    public void setContactNumber(String c)
    {
        contactNumber=c;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String p)
    {
        password=p;
        }
    public Integer getIsAdmin()
    {
        return isAdmin;
    }
    public void setIsAdmin(Integer a)
    {
        isAdmin=a;
    }
    public String getAddress()
    {
        return address;
    }
    public void setAddress(String adds) {
        this.address = adds;
    }
}
