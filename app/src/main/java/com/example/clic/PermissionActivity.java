package com.example.clic;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        TextView explanationText = findViewById(R.id.explanation_text);
        Button enableButton = findViewById(R.id.enable_button);
        Button skipButton = findViewById(R.id.skip_button);

        explanationText.setText("To automatically fill OTP codes, CLiC needs permission to read notifications.\n\n" +
                "This will only be used to detect OTP codes from Outlook for faster login.");

        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if notification access is granted
        if (isNotificationServiceEnabled()) {
            startMainActivity();
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (flat != null && !flat.isEmpty()) {
            return flat.contains(pkgName);
        }
        return false;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}