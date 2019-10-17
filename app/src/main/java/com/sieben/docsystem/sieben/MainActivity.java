package com.sieben.docsystem.sieben;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN = "http://docsystem5.clouddoc.com.br/SimplePortal/Pages/Login.html";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FILE_CHOOSER = 1001;
    private static final int REQUEST_PERMISSION_ID = 1002;

    private WebView mWebView;
    private UserHelper mUserHelper;
    private MenuItem mBtnMenuItemLogins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserHelper = new UserHelper(getApplicationContext());
        initializeComponents();
        loadWebView(MAIN);
        requestPermissions();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_ID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_ID) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length <= 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //TODO: Show error screen
            }
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
        mWebView = findViewById(R.id.webView);
        WebSettings settings = mWebView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        mBtnMenuItemLogins = menu.findItem(R.id.btnManageLogins);
        return super.onCreateOptionsMenu(menu);
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
        return super.onOptionsItemSelected(item);
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
                if(!url.equals(MAIN)) {
                    mBtnMenuItemLogins.setVisible(false);
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
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                openFileChooser();
                return true;
            }

            void openFileChooser() {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(getString(R.string.mime_types));
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "Escolha seu arquivo"),
                        FILE_CHOOSER);
            }
        });
        mWebView.loadUrl(url);
    }


    private void onRefresh() {
        Log.i(TAG, "onRefresh()");
        loadWebView(mWebView.getUrl());
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Salvar dados de login");
        builder.setCancelable(true);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText edtName = dialogView.findViewById(R.id.edtName);
                EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
                User user = new User();
                user.setLogin(TextUtils.isEmpty(edtName.getText().toString()) ?
                        null :
                        edtName.getText().toString());
                user.setPassword(TextUtils.isEmpty(edtPassword.getText().toString()) ?
                        null :
                        edtPassword.getText().toString());
                mUserHelper.setCredentials(user);
                mWebView.loadUrl(user.toString());
                showToast("Usuário salvo com sucesso");
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
                        mWebView.goBack();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == 1001) {
            if (intent == null || resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "onActivityResult(): Error");
                return;
            }
            Uri data = intent.getData();
            if (data == null) {
                Log.e(TAG, "onActivityResult(): Null data");
                return;
            }
            getDataFromDocumentProvider(data);
        }
    }

    private void getDataFromDocumentProvider(Uri data) {
        Uri documentUri = Uri.parse(data.getScheme() + "://"
                + data.getAuthority()
                + data.getEncodedPath());
        try {
            getApplicationContext().getContentResolver().openInputStream(documentUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getDataFromDocumentProvider(): Invalid document");
            showToast("Impossível abrir arquivo!");
        }
    }
}
