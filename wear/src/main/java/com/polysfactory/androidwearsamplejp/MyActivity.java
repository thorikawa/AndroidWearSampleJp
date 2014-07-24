package com.polysfactory.androidwearsamplejp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import static android.view.View.OnApplyWindowInsetsListener;

public class MyActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        Intent intent = new Intent(this, DataLayerListenerService.class);
        startService(intent);

        ViewGroup localViewGroup = (ViewGroup) super.getWindow().getDecorView();
        //localViewGroup.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
        stub.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                Log.d("TEST", "onApplyWindowInsets");
                return windowInsets;
            }
        });
        // localViewGroup.setFitsSystemWindows(true);
        // localViewGroup.requestApplyInsets();
    }
}
