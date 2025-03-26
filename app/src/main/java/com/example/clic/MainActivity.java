package com.example.clic;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CLIC_BASE_URL = "https://clic.mmu.edu.my/psp/csprd/EMPLOYEE/SA/";
    private static final String EBWISE_BASE_URL = "https://ebwise.mmu.edu.my/my/";
    private static final int BUTTON_MARGIN = 8;

    private WebView webView;
    private BroadcastReceiver otpReceiver;
    private Button weeklyScheduleButton;
    private Button coursesButton;
    private LinearLayout buttonContainer;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupWebView();
        setupButtons();
        registerOtpReceiver();

        webView.loadUrl(CLIC_BASE_URL + "h/?tab=DEFAULT&cmd=login");
    }

    private void initializeViews() {
        webView = findViewById(R.id.webView);
        buttonContainer = findViewById(R.id.buttonContainer);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        webView.addJavascriptInterface(new WebAppInterface(), "JSInterface");
        webView.setWebViewClient(new CustomWebViewClient());
    }

    private void setupButtons() {
        weeklyScheduleButton = createStyledButton("Weekly Schedule");
        coursesButton = createStyledButton("ebwise");

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);

        weeklyScheduleButton.setLayoutParams(buttonParams);
        coursesButton.setLayoutParams(buttonParams);

        buttonContainer.addView(weeklyScheduleButton);
        buttonContainer.addView(coursesButton);

        weeklyScheduleButton.setOnClickListener(v ->
                webView.loadUrl(CLIC_BASE_URL + "c/SA_LEARNER_SERVICES.SSR_SSENRL_SCHD_W.GBL?FolderPath=PORTAL_ROOT_OBJECT.CO_EMPLOYEE_SELF_SERVICE.N_NEW_ACADEMICS.N_NEW_CRSENRL.N_NEW_CLASSSCH.HC_SSR_SSENRL_SCHD_W_GBL&IsFolder=false&IgnoreParamTempl=FolderPath,IsFolder")
        );

        coursesButton.setOnClickListener(v -> toggleCoursesWebsite());
    }

    private Button createStyledButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(Color.rgb(182, 209, 146));
        button.setTextColor(Color.BLACK);
        button.setVisibility(View.GONE);
        button.setAllCaps(false);
        return button;
    }

    private void toggleCoursesWebsite() {
        String currentUrl = webView.getUrl();
        if (currentUrl == null) return;

        String targetUrl;
        String buttonText;

        if (currentUrl.contains("clic.mmu.edu.my")) {
            targetUrl = EBWISE_BASE_URL + "courses.php";
            buttonText = "CLiC";
        } else if (currentUrl.contains("ebwise.mmu.edu.my")) {
            targetUrl = CLIC_BASE_URL + "h/?tab=DEFAULT";
            buttonText = "ebwise";
        } else {
            targetUrl = EBWISE_BASE_URL + "courses.php";
            buttonText = "CLiC";
        }

        webView.loadUrl(targetUrl);
        coursesButton.setText(buttonText);
    }

    private void registerOtpReceiver() {
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

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(otpReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(otpReceiver, filter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering receiver", e);
        }
    }

    private void fillOtpInWebView(String otp) {
        String javascript = createOtpFillingScript(otp);

        webView.evaluateJavascript(javascript, value -> {
            Log.d(TAG, "JavaScript result: " + value);
            if (value.contains("OTP filled and submitted")) {
                Toast.makeText(MainActivity.this, "OTP entered and validated!", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    weeklyScheduleButton.setVisibility(View.VISIBLE);
                    coursesButton.setVisibility(View.VISIBLE);
                });
            } else {
                Toast.makeText(MainActivity.this, "Failed to validate OTP. Check the page structure.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String createOtpFillingScript(String otp) {
        return "javascript:(function() {" +
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
    }

    @Override
    public void onBackPressed() {
        String currentUrl = webView.getUrl();
        if (webView.canGoBack()) {
            if (currentUrl != null && currentUrl.contains("SA/h/?tab=DEFAULT")) {
                Toast.makeText(this, "You are already on the login page!", Toast.LENGTH_SHORT).show();
            } else {
                webView.goBack();
            }
        } else {
            super.onBackPressed();
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Block specific logout URLs
            if (url.contains("cmd=logout")) {
                view.loadUrl("https://clic.mmu.edu.my/psp/csprd/EMPLOYEE/SA/");
                return true;
            }
            return false;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            // Inject script to intercept and block original logout behavior
            String blockLogoutScript = "javascript:(function() {" +
                    "var signOutLink = document.getElementById('pthdr2signout');" +
                    "if (signOutLink) {" +
                    "   signOutLink.addEventListener('click', function(e) {" +
                    "       e.preventDefault();" +
                    "       e.stopPropagation();" +
                    "       window.JSInterface.onSignOutClicked(signOutLink.href);" +
                    "   }, true);" +
                    "}" +
                    "})()";

            view.evaluateJavascript(blockLogoutScript, null);

            boolean isLoginPage = url.contains("/SA/?cmd=logout") ||
                    url.contains("/SA/?&cmd=login&languageCd=ENG") ||
                    url.contains("https://clic.mmu.edu.my/psp/csprd/EMPLOYEE/SA/h/?tab=DEFAULT&cmd=login");

            buttonContainer.setVisibility(isLoginPage ? View.GONE : View.VISIBLE);

            updateCoursesButtonText(url);
            checkForOtpField();
        }

        private void injectSignOutHandler() {
            String javascript = "javascript:(function() {" +
                    "var signOutLink = document.getElementById('pthdr2signout');" +
                    "if (signOutLink) {" +
                    "   signOutLink.addEventListener('click', function(e) {" +
                    "       e.preventDefault();" +
                    "       window.JSInterface.onSignOutClicked(signOutLink.href);" +
                    "   });" +
                    "}" +
                    "})()";

            webView.evaluateJavascript(javascript, null);
        }

        private void updateCoursesButtonText(String url) {
            if (url.contains("clic.mmu.edu.my")) {
                coursesButton.setText("ebwise");
            } else if (url.contains("ebwise.mmu.edu.my")) {
                coursesButton.setText("CLiC");
            }
        }

        private void checkForOtpField() {
            String checkForOtpField = "javascript:(function() {" +
                    "var otpInput = document.getElementById('otp');" +
                    "return otpInput != null;" +
                    "})()";

            webView.evaluateJavascript(checkForOtpField, value -> {
                if (value != null && value.equals("true")) {
                    Toast.makeText(MainActivity.this, "OTP page detected. Waiting for OTP notification...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void onSignOutClicked(String url) {
            runOnUiThread(() -> {
                webView.loadUrl(url);

                webView.postDelayed(() -> {
                    webView.loadUrl("https://clic.mmu.edu.my/psp/csprd/EMPLOYEE/SA/h/?tab=DEFAULT&cmd=login");
                }, 500);

                Toast.makeText(MainActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
            });
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