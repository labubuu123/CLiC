package com.example.clic;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Toast;
import android.util.Log;
import android.webkit.ValueCallback;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BroadcastReceiver otpReceiver;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String checkForOtpField = "javascript:(function() {" +
                        "var otpInput = document.getElementById('otp');" +
                        "return otpInput != null;" +
                        "})()";

                webView.evaluateJavascript(checkForOtpField, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (value != null && value.equals("true")) {
                            Toast.makeText(MainActivity.this, "OTP page detected. Waiting for OTP notification...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Register OTP receiver
        registerOtpReceiver();

        // Load CLiC website
        webView.loadUrl("https://clic.mmu.edu.my");
    }

    private void registerOtpReceiver() {
        try {
            otpReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String otp = intent.getStringExtra(NotificationService.OTP_EXTRA);
                    if (otp != null && !otp.isEmpty()) {
                        fillOtpInWebView(otp);
                    }
                }
            };

            IntentFilter filter = new IntentFilter(NotificationService.OTP_INTENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(otpReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(otpReceiver, filter);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error registering receiver: " + e.getMessage());
        }
    }


    private void fillOtpInWebView(String otp) {
        Log.d("MainActivity", "Attempting to fill OTP and click validate: " + otp);

        String javascript = "javascript:(function() {" +
                "console.log('OTP Auto-fill script running');" +
                "var otpInput = document.getElementById('otp');" +
                "if(otpInput) {" +
                "  console.log('Found OTP input field by ID');" +
                "  otpInput.value = '" + otp + "';" +
                "  otpInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "  otpInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "  var validateBtn = document.getElementById('ps_submit_button');" +
                "  if (validateBtn) {" +
                "    console.log('Found Validate OTP button');" +
                "    validateBtn.click();" +
                "    return 'OTP filled and submitted';" +
                "  } else {" +
                "    return 'Validate button not found';" +
                "  }" +
                "} else {" +
                "  return 'OTP input field not found';" +
                "}" +
                "})()";

        webView.evaluateJavascript(javascript, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("MainActivity", "JavaScript result: " + value);
                if (value.contains("OTP filled and submitted")) {
                    Toast.makeText(MainActivity.this, "OTP entered and validated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to validate OTP. Check the page structure.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            String currentUrl = webView.getUrl();

            if (currentUrl != null && currentUrl.contains("NUI_FRAMEWORK.PT_LANDINGPAGE.GBL?")) {
                Toast.makeText(this, "You are already on the login page!", Toast.LENGTH_SHORT).show();
            } else {
                webView.goBack();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (otpReceiver != null) {
            unregisterReceiver(otpReceiver);
        }
        super.onDestroy();
    }
}