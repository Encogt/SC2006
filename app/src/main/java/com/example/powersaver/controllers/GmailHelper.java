package com.example.powersaver.controllers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

import java.io.IOException;
import java.util.Base64;

public class GmailHelper {

    private static final String TAG = "GmailHelper";
    private Gmail gmailService;
    private Context context;

    public GmailHelper(Context context, GoogleAccountCredential credential) {
        this.context = context;
        try {
            gmailService = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("PowerSaver")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();  // Consider using a logging mechanism here
            Log.e(TAG, "Error initializing Gmail service: " + e.getMessage());
        }
    }

    public Gmail getGmailService() {
        return gmailService;
    }

    // Method to send an email with a PDF attachment
    public void sendEmailWithAttachment(String from, String to, String subject, String bodyText, Uri fileUri, String fileName) throws MessagingException, IOException {
        MimeMessage emailContent = createEmailWithAttachment(from, to, subject, bodyText, fileUri, fileName);
        Message message = createMessageWithEmail(emailContent);
        gmailService.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmailWithAttachment(String from, String to, String subject, String bodyText, Uri fileUri, String fileName) throws MessagingException, IOException {
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

        // Text body part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");

        // Attachment body part
        MimeBodyPart attachmentPart = new MimeBodyPart();

        // Use InputStream from ContentResolver to handle file URIs
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream != null) {
                byte[] pdfBytes = readBytesFromInputStream(inputStream);
                DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(fileName);
            } else {
                throw new IOException("Unable to open input stream from URI");
            }
        }

        // Combine text and attachment into a multipart
        Multipart multipart = new MimeMultipart("mixed");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);
        email.setContent(multipart);

        return email;
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buf)) != -1) {
            bos.write(buf, 0, bytesRead);
        }
        return bos.toByteArray();
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] rawBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
