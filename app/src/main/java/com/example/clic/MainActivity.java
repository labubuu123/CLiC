package com.example.clic;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Toast;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("?cmd=login")) {
                    webView.loadUrl("javascript:window.history.pushState(null, null, document.URL);");
                }

                webView.loadUrl("javascript:(function() { " +
                        "setInterval(function() {" +
                        "    var homeButton = document.getElementById('PT_HOME');" +
                        "    if (homeButton) {" +
                        "        homeButton.click();" +
                        "        console.log('Home button clicked to keep session active');" +
                        "    }" +
                        "}, 540000);" +
                        "})();");
            }
        });

        webView.loadUrl("https://clic.mmu.edu.my");
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
