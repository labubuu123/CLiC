package com.example.clic;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.content.Context;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    public static final String OTP_INTENT = "com.example.clic.OTP_RECEIVED";
    public static final String OTP_EXTRA = "otp_code";
    private static final String RECEIVE_SENSITIVE_NOTIFICATIONS = "android.permission.RECEIVE_SENSITIVE_NOTIFICATIONS";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= 35) { // API Level 35
            if (checkSelfPermission(RECEIVE_SENSITIVE_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Missing RECEIVE_SENSITIVE_NOTIFICATIONS permission!");
                return;
            }
        }

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        String title = extras.getString(Notification.EXTRA_TITLE, "No Title");
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT, "No Text");
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        Log.d(TAG, "Received Notification: " + title);
        if (bigText != null) {
            Log.d(TAG, "Big Text: " + bigText.toString());
        }

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