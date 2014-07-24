package com.polysfactory.androidwearsamplejp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends Activity {

    public static final String EXTRA_KEY_COUNT = "extra_key_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();
        int count = intent.getIntExtra(EXTRA_KEY_COUNT, -1);
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(String.format("count is %d", count));
    }
}
