package com.example.powersaver.activity.dashboard;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.powersaver.activity.login.LoginActivity;
import com.example.powersaver.activity.login.UpdateDetailsActivity;
import com.example.powersaver.activity.devicemanager.DeviceManagerActivity;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.databinding.ActivityDashboardBinding;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private String currentUsername;
    private int currentUserID;
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/gmail.send"};
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_PERMISSIONS = 1002;
    private static final String TAG = "DashboardActivity";

    // Variables to hold email parameters for retry
    private Uri pendingFileUri;
    private String pendingFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        currentUsername = getIntent().getStringExtra("username");
        currentUserID = getIntent().getIntExtra("userID", 0);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);



        if (currentUsername == null || currentUsername.isEmpty() || currentUserID == 0) {
            Toast.makeText(this, "No username provided. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            getSupportActionBar().setTitle("PowerSaver - Dashboard");
        }

        // Initialize Google Account Credentials
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accountName = prefs.getString("accountName", null);
        if (accountName == null) {
            startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            mCredential.setSelectedAccountName(accountName);
        }

        // Add button functionality for exporting PDF
        Button exportPdfButton = findViewById(R.id.export_pdf_button);
        exportPdfButton.setOnClickListener(v -> checkPermissionsAndGeneratePdf());
    }
//Check or propt for permissions to Notifications and save to Downloads
    private void checkPermissionsAndGeneratePdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No need to request storage permissions on Android Q and above
            generatePdf();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            } else {
                generatePdf();
            }
        } else {
            generatePdf();
        }
    }
//Checks permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    generatePdf();
                } else {
                    Toast.makeText(this, "Permissions are required to save and send the PDF.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permissions are required to save and send the PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        prefs.edit().putString("accountName", accountName).apply();
                        mCredential.setSelectedAccountName(accountName);
                        // Retry sending the email if pending
                        if (pendingFileUri != null && pendingFileName != null) {
                            sendEmailUsingGmailAPI(pendingFileUri, pendingFileName);
                        }
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // Retry sending the email after authorization
                    if (pendingFileUri != null && pendingFileName != null) {
                        sendEmailUsingGmailAPI(pendingFileUri, pendingFileName);
                    }
                } else {
                    Toast.makeText(this, "Permission required to send email.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
//Generates PDF triggered when button for export is pressed
    private void generatePdf() {
        ScrollView scrollView = findViewById(R.id.scrollViewDashboard);
        // Get the total height of the content
        int totalHeight = getScrollViewHeight(scrollView);

        // Define the height of each page (e.g., A4 size in pixels at 72 DPI)
        int pageWidth = scrollView.getWidth();
        int pageHeight = 1120; // Adjust as needed based on your content and desired page size

        PdfDocument pdfDocument = new PdfDocument();

        int totalPages = (int) Math.ceil((double) totalHeight / pageHeight);

        for (int i = 0; i < totalPages; i++) {
            int yOffset = i * pageHeight;
            Bitmap bitmap = getBitmapFromView(scrollView, yOffset, pageWidth, pageHeight);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap, 0, 0, paint);
            pdfDocument.finishPage(page);

            bitmap.recycle();
        }

        // Save the PDF document
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfToDownloadsAndroidQAndAbove(pdfDocument);
        } else {
            savePdfToDownloadsLegacy(pdfDocument);
        }

        pdfDocument.close();
    }
//Serve pdf generate print in order to get height to configure export
    private int getScrollViewHeight(ScrollView scrollView) {
        int totalHeight = 0;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            View child = scrollView.getChildAt(i);
            child.measure(
                    View.MeasureSpec.makeMeasureSpec(scrollView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += child.getMeasuredHeight();
        }
        return totalHeight;
    }
    //Serve pdf generate
    private Bitmap getBitmapFromView(ScrollView scrollView, int yOffset, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(0, -yOffset);
        scrollView.draw(canvas);
        return bitmap;
    }
    //Serves PDF exported, downloads to local storage on device
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void savePdfToDownloadsAndroidQAndAbove(PdfDocument pdfDocument) {
        String fileName = "dashboard_export.pdf";
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            pdfDocument.writeTo(out);
            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();

            // Send email using the URI
            sendEmailUsingGmailAPI(uri, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //Serves PDF exported, downloads to local storage on device
    private void savePdfToDownloadsLegacy(PdfDocument pdfDocument) {
        String directoryPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(directoryPath, "dashboard_export.pdf");

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (OutputStream fos = new FileOutputStream(file)) {
                pdfDocument.writeTo(fos);
            }

            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();
            sendEmailUsingGmailAPI(Uri.fromFile(file), file.getName());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
//Contracts email and address to handling sending
    private void sendEmailUsingGmailAPI(Uri fileUri, String fileName) {
        // Save the fileUri and fileName for potential retry
        pendingFileUri = fileUri;
        pendingFileName = fileName;

        new Thread(() -> {
            try {
                String recipientEmail = getUserEmail(currentUserID);

                if (recipientEmail == null || recipientEmail.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "No email found for this user.", Toast.LENGTH_SHORT).show());
                    return;
                }

                Gmail service = new Gmail.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        mCredential)
                        .setApplicationName("PowerSaver")
                        .build();

                MimeMessage emailContent = createEmailWithAttachment(
                        mCredential.getSelectedAccountName(),
                        recipientEmail,
                        "Dashboard Export PDF",
                        "Please find the attached dashboard export.",
                        fileUri,
                        fileName
                );

                Message message = createMessageWithEmail(emailContent);
                service.users().messages().send("me", message).execute();

                runOnUiThread(() -> Toast.makeText(this, "Email sent successfully!", Toast.LENGTH_SHORT).show());

                // Clear pending email data after success
                pendingFileUri = null;
                pendingFileName = null;

            } catch (UserRecoverableAuthIOException e) {
                // Prompt the user to grant permission
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (Exception e) {
                Log.e(TAG, "Error sending email: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
//Attach exported dashboard to Email
    private MimeMessage createEmailWithAttachment(String from, String to, String subject, String bodyText, Uri fileUri, String fileName)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);

        // Initialize MailcapCommandMap to resolve UnsupportedDataTypeException
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/mixed;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("application/pdf;; x-java-content-handler=com.sun.mail.handlers.application_pdf");
        CommandMap.setDefaultCommandMap(mc);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        // Create the message body part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");

        // Create the attachment part
        MimeBodyPart attachmentPart = new MimeBodyPart();

        // Use InputStream from ContentResolver to handle file URIs
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            if (inputStream != null) {
                byte[] pdfBytes = readBytesFromInputStream(inputStream);
                DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(fileName);
            } else {
                throw new IOException("Unable to open input stream from URI");
            }
        }

        // Create multipart
        Multipart multipart = new MimeMultipart("mixed");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        email.setContent(multipart);

        return email;
    }
//Serves Email function
    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buf)) != -1) {
            bos.write(buf, 0, bytesRead);
        }
        return bos.toByteArray();
    }
    //Serves Email function to create Email message
    private Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
//gets email address based on logged on user
    private String getUserEmail(int userID) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return databaseHelper.getUserEmail(userID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
//Handles actions in top menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_manage_devices) {
            Intent intent = new Intent(this, DeviceManagerActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("userID", currentUserID);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_account_details) {
            Intent intent = new Intent(DashboardActivity.this, UpdateDetailsActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("userID", currentUserID);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_logout) {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        if (id == R.id.action_close) {
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
