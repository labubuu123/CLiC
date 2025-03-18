package com.example.clic;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    public static final String OTP_INTENT = "com.example.clic.OTP_RECEIVED";
    public static final String OTP_EXTRA = "otp_code";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        String title = extras.getString(Notification.EXTRA_TITLE, "No Title");
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT, "No Text");
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT); // Might contain bold text

        Log.d(TAG, "Received Notification");
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Text: " + text);
        if (bigText != null) {
            Log.d(TAG, "Big Text: " + bigText.toString());
        }

        // Use bigText if available, otherwise use text
        String message = (bigText != null) ? bigText.toString() : text.toString();

        if (message.contains("To complete login process to CLiC")) {
            String otpCode = extractBoldOtp(message);
            if (otpCode != null) {
                Log.d(TAG, "OTP Detected: " + otpCode);
                broadcastOtp(otpCode);
            }
        }
    }

    private String extractBoldOtp(String message) {
        // Look for exactly 6 digits in the message
        Pattern pattern = Pattern.compile("\\b(\\d{6})\\b");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String otp = matcher.group(1);
            Log.d(TAG, "Extracted OTP: " + otp);
            return otp;
        }

        return null;
    }

    private void broadcastOtp(String otp) {
        Intent intent = new Intent(OTP_INTENT);
        intent.putExtra(OTP_EXTRA, otp);
        sendBroadcast(intent);
    }
}