package com.wyb.request;

import android.os.Bundle;

import com.wyb.requestlibrary.RequestManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 主界面
 *
 * @author wyb
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestManager.init(this, "https:123");
        RequestManager.getInstance().sendRequest(new TestRequest("adsfsadf", "999", "2"), String.class, false, null);
    }
}
