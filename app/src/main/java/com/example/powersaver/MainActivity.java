package com.example.powersaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.powersaver.controllers.GmailHelper;
import com.example.powersaver.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private GoogleAccountCredential credential;
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/gmail.send"};
    private static final int REQUEST_ACCOUNT_PICKER = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Initialize Google Account Credential
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES));

        // Handle account selection
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accountName = prefs.getString("accountName", null);
        if (accountName == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            credential.setSelectedAccountName(accountName);
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // PDF generation and email sending on Floating Action Button click
        binding.fab.setOnClickListener(view -> {
            String pdfPath = getFilesDir() + "/dashboard.pdf";
            generateDashboardPdfFromView(binding.getRoot(), pdfPath);
            Snackbar.make(view, "PDF created and ready for sending.", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .setAction("Action", null).show();

            // Convert the file path to a Uri
            Uri pdfUri = Uri.fromFile(new File(pdfPath));

            // Send the PDF via Gmail API
            sendEmailWithPdf("recipient@example.com", pdfUri); // Replace with actual recipient email
        });
    }
//Request Email account on device in order to use gmail API
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            String accountName = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putString("accountName", accountName).apply();
                credential.setSelectedAccountName(accountName);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//          if (id == R.id.action_settings) {
//              return true;
//          }

        if (id == R.id.action_manage_devices) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
//Handles creation of pdf of dashboard
    public void generateDashboardPdfFromView(View dashboardView, String pdfPath) {
        try {
            Bitmap bitmap = captureView(dashboardView);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            Image image = new Image(ImageDataFactory.create(bitmapData));
            image.setAutoScale(true);
            document.add(image);

            document.close();
            stream.close();

            Toast.makeText(this, "PDF generated at: " + pdfPath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }
//Attaches haved PDF and prepares for it to be sent via email API
    public void sendEmailWithPdf(String recipientEmail, Uri pdfUri) {
        new Thread(() -> {
            try {
                // Check if account is selected
                if (credential.getSelectedAccountName() == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Please select a Google account.", Toast.LENGTH_SHORT).show());
                    return;
                }

                GmailHelper gmailHelper = new GmailHelper(this, credential);
                gmailHelper.sendEmailWithAttachment(
                        credential.getSelectedAccountName(),
                        recipientEmail,
                        "PowerSaver Dashboard PDF",
                        "Please find attached the PowerSaver dashboard information.",
                        pdfUri,
                        "dashboard.pdf"
                );
                runOnUiThread(() -> Toast.makeText(this, "Email sent successfully", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error sending email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
//Used to grab current dashboard to export as PDF
    public Bitmap captureView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
