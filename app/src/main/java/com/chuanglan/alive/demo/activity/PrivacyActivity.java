package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;
import com.chuanglan.sdk.constant.LogConstant;
import com.chuanglan.sdk.tools.LogTool;

public class PrivacyActivity extends Activity {
    private WebView mWebView;
    private RelativeLayout mBackRootLayout;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            LogTool.d(LogConstant.EXCEPTION_LOGTAG, "PrivacyActivity onCreate-->");
            setContentView(R.layout.face_verify_privace_activity);
            initViews();
            initEvent();
            setListener();
        } catch (Exception e) {
            LogTool.d(LogConstant.EXCEPTION_LOGTAG, "PrivacyActivityonCreate Exception-->", e);
            e.printStackTrace();
            finish();
        }
    }

    private void setListener() {
        mBackRootLayout.setOnClickListener(view -> {
            back();
        });
    }

    private void back() {
        if (null != mWebView) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finishtask();
            }
        } else {
            finishtask();
        }
    }

    private void initEvent() {
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowFileAccess(false);
        webSetting.setAllowFileAccessFromFileURLs(false);
        webSetting.setAllowUniversalAccessFromFileURLs(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            mWebView.removeJavascriptInterface("accessibility");
            mWebView.removeJavascriptInterface("accessibilityTraversal");
        }
        webSetting.setSupportZoom(true);
        webSetting.setSavePassword(false);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setAllowFileAccessFromFileURLs(false);
        webSetting.setAllowUniversalAccessFromFileURLs(false);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setPluginState(WebSettings.PluginState.ON);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl("https://doc.chuanglan.com/document/6JFXVLDBQDXNK0HS");
    }

    private void initViews() {
        mWebView = findViewById(R.id.face_verify_baseweb_webview);
        mBackRootLayout = findViewById(R.id.face_verify_titlebar_back_root);
        mTitleText = findViewById(R.id.face_verify_titlebar_title);
        mTitleText.setText(R.string.face_verify_privacy_name);
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, android.net.http.SslError error) {
            handler.cancel();
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 解决进入后台后显任务列表显示两个APP的问题
     */
    private void finishtask() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } catch (Exception e) {
            LogTool.d(LogConstant.EXCEPTION_LOGTAG, "PrivacyActivity finishtask Exception-->", e);
            e.printStackTrace();
        }
    }
}