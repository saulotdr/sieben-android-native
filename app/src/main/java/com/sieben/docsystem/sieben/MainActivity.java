package com.sieben.docsystem.sieben;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Method;

import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MAIN = "http://docsystem5.clouddoc.com.br/SimplePortal/Pages/Login.html";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FILE_CHOOSER = 1001;
    private static boolean GO_BACKWARDS = false;
    private static String mQuery = "";

    private WebView mWebView;
    private UserHelper mUserHelper;
    private MenuItem mBtnMenuItemLogin;
    private MenuItem mBtnMenuItemFind;
    private MenuItem mBtnMenuItemRefresh;
    private ValueCallback<Uri[]> mUploadContent;
    private TextFieldBoxes mFindTextField;
    private EditText mFindEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
        initializeHelpers();
        addListeners();
        decorateActionBar();
        loadWebView(MAIN);
    }

    private void initializeHelpers() {
        mUserHelper = new UserHelper(getApplicationContext());
    }

    private void decorateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#817e7e")));
        }
    }

    private String loadSavedState() {
        User user = mUserHelper.getCredentials();
        if (user == null) {
            return "";
        }
        return user.toString();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeComponents() {
        mFindTextField = findViewById(R.id.text_field_boxes);
        mFindEditText = findViewById(R.id.extended_edit_text);
        mWebView = findViewById(R.id.webView);
        mWebView.addJavascriptInterface(this, "android");
        WebSettings settings = mWebView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
    }

    private void addListeners() {
        mFindTextField.getEndIconImageButton().setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        mBtnMenuItemLogin = menu.findItem(R.id.btnManageLogins);
        mBtnMenuItemRefresh = menu.findItem(R.id.btnRefresh);
        mBtnMenuItemFind = menu.findItem(R.id.btnFind);
        setDefaultComponentsState();
        return super.onCreateOptionsMenu(menu);
    }

    private void setDefaultComponentsState() {
        //All components in this method will be visible when the current page isn't the login page
        mBtnMenuItemRefresh.setVisible(false);
        mBtnMenuItemFind.setVisible(false);
        //Only triggered by find button in menu item
        mFindTextField.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnManageLogins) {
            openDialog();
        }
        if (id == R.id.btnRefresh) {
            onRefresh();
        }
        if (id == R.id.btnFind) {
            showFindDropDownMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFindDropDownMenu() {
        if (mFindTextField.getVisibility() == View.VISIBLE) {
            mFindTextField.setVisibility(View.INVISIBLE);
        } else {
            mFindTextField.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged(): Configuration has changed");
        super.onConfigurationChanged(newConfig);
    }

    private void loadWebView(String url) {
        mWebView.setWebViewClient(new WebViewClient() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading(): loading: " + url);
                if (url.equals(MAIN)) {
                    loadComponentsStatePerPage(true);
                } else {
                    loadComponentsStatePerPage(false);
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished(): loading: " + url);
                String state = loadSavedState();
                if (!TextUtils.isEmpty(state)) {
                    mWebView.loadUrl(state);
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadContent != null) {
                    mUploadContent.onReceiveValue(null);
                    mUploadContent = null;
                }
                mUploadContent = filePathCallback;
                openFileChooser();
                return true;
            }

            void openFileChooser() {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(getString(R.string.mime_types));
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, getString(R.string.choose_file_pt_br)),
                        FILE_CHOOSER);
            }
        });
        mWebView.loadUrl(url);
    }

    private void loadComponentsStatePerPage(boolean isMainPage) {
        if (isMainPage) {
            mFindTextField.setVisibility(View.INVISIBLE);
            mBtnMenuItemLogin.setVisible(true);
            mBtnMenuItemFind.setVisible(false);
            mBtnMenuItemRefresh.setVisible(false);
        } else {
            mBtnMenuItemLogin.setVisible(false);
            mBtnMenuItemRefresh.setVisible(true);
            mBtnMenuItemFind.setVisible(true);
        }
    }

    private void onRefresh() {
        Log.i(TAG, "onRefresh()");
        loadWebView(mWebView.getUrl());
    }

    @SuppressLint("InflateParams")
    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog, null);
        final EditText edtName = dialogView.findViewById(R.id.edtName);
        User user = mUserHelper.getCredentials();
        if (user != null) {
            edtName.setText(user.getLogin());
        }
        final EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.save_login_pt_br));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.save_pt_br), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                User user = new User();
                user.setLogin(edtName.getText().toString());
                user.setPassword(edtPassword.getText().toString());
                mUserHelper.setCredentials(user);
                mWebView.loadUrl(user.toString());
                showToast(getString(R.string.user_saved_pt_br));
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_pt_br), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        WebBackForwardList mWebBackForwardList = mWebView.copyBackForwardList();
                        String lastPage = mWebBackForwardList
                                .getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();
                        if (lastPage.equals(MAIN)) {
                            loadComponentsStatePerPage(true);
                        } else {
                            loadComponentsStatePerPage(false);
                        }
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILE_CHOOSER) {
            if (intent == null || resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "onActivityResult(): Error");
                return;
            }
            if (mUploadContent == null) {
                return;
            }
            mUploadContent.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
            mUploadContent = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.text_field_boxes_end_icon_button) {
            mQuery = mFindEditText.getText().toString().toLowerCase().trim();
            if (TextUtils.isEmpty(mQuery)) {
                mFindTextField.setError("", true);
                showToast(getString(R.string.not_found_pt_br));
                return;
            }

            mWebView.findAll(mQuery);
            try {
                Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                m.invoke(mWebView, true);
            } catch (Throwable ignored) {
            }
        }
    }
}
