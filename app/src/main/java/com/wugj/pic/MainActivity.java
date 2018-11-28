package com.wugj.pic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;


import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * description:
 * </br>
 * author: wugj
 * </br>
 * date: 2018/11/23
 * </br>
 * version:
 */
public class MainActivity extends Activity{


    WebView mWebView;
    ImageView bit;
    ImageView bit2;

    private final int SHOW_PIC= 0x001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bit = findViewById(R.id.bit);
        bit2 = findViewById(R.id.bit2);
        mWebView = findViewById(R.id.webview);

        initWebView();

        String base64 = readAssetsTxt(this,"base64pic");
        Bitmap bitmap = stringtoBitmap(base64);
        bit.setImageBitmap(bitmap);
    }



    private void initWebView() {

        WebSettings websettings = mWebView.getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setUserAgentString(websettings.getUserAgentString()+"appHaozu");
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                if (!TextUtils.isEmpty(scheme) && scheme.equals("tel")){

                }else{
                    view.loadUrl(url);
                }

                return true;
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                TextView tv = findViewById(R.id.tv);
                tv.setText(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {

                }
            }
        });

        mWebView.addJavascriptInterface(new JsToNative(), "AppHandler");
        mWebView.loadUrl("file:///android_asset/basetest.html");

    }





    private class JsToNative {
        @JavascriptInterface
        public void callNative(String params) {
            //解析获取到值 获取到type
            Log.e("Main", "callNative: " + params);

            if (mHandler == null) {
                return;
            }
            JsToNativeBean bean = JSONObject.parseObject(params, JsToNativeBean.class);
            Message msg = Message.obtain();
            msg.obj = bean;
            msg.what = SHOW_PIC;
            mHandler.sendMessage(msg);

        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_PIC:
                    JsToNativeBean obj = (JsToNativeBean) msg.obj;//h5调用原生 分享model
                    if (null == obj){
                        return;
                    }

                    Bitmap bitmap = stringtoBitmap(obj.type);
                    bit2.setImageBitmap(bitmap);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 读取assets下的txt文件，返回utf-8 String
     * @param context
     * @param fileName 不包括后缀
     * @return
     */
    public static String readAssetsTxt(Context context, String fileName){
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName+".txt");
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            // Should never happen!
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }

    public Bitmap stringtoBitmap(String string) {
        //将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


}
