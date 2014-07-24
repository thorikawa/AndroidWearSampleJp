package com.polysfactory.androidwearsamplejp;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.List;


public class MyActivity extends Activity {

    private static final String TAG = "TEST";
    private static final String COUNT_KEY = "COUNT_KEY";
    private static final String PATH = "/count";
    private static final String ACTION_OPEN_CUSTOM_ACTIVITY = "com.polysfactory.androidwearsamplejp.action_open_custom_activity";

    private int count = 0;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).build();
        mGoogleApiClient.connect();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PutDataMapRequest dataMap = PutDataMapRequest.create(PATH);
                Log.d(TAG, "put url:" + dataMap.getUri());
                dataMap.getDataMap().putInt(COUNT_KEY, count++);
                PutDataRequest request = dataMap.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request);
                Log.d(TAG, "data start sending" + request.getUri());
                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, "changed:" + dataItemResult.getDataItem().getUri());
                        Log.d(TAG, "count:" + count);
                    }
                });

            }
        });
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runAsyncTask();
            }
        });

        Button notificationButton = (Button) findViewById(R.id.button_notification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getLocalNodeId() {
        NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
        return nodeResult.getNode().getId();
    }

    private String getRemoteNodeId() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodesResult =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        List<Node> nodes = nodesResult.getNodes();
        if (nodes.size() > 0) {
            return nodes.get(0).getId();
        }
        return null;
    }

    private void runAsyncTask() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(mGoogleApiClient).await();
                for (DataItem dataItem : dataItemBuffer) {
                    Log.d(TAG, "url=[" + dataItem.getUri().toString() + "]");
                    Log.d(TAG, "val=[" + DataMapItem.fromDataItem(dataItem).getDataMap().get(COUNT_KEY) + "]");
                }

                Uri uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority("abcd77b0-5828-4f46-b94e-3a3df9e97f98").path(PATH).build();
                Log.d(TAG, "access to:[" + uri.toString() + "]");
                Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        DataItem dataItem = dataItemResult.getDataItem();
                        Log.d(TAG, "local dataItemResult:" + dataItem);
                        if (dataItem != null) {
                            count = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap().getInt(COUNT_KEY);
                            Log.d(TAG, "stored count:" + count);
                        }
                    }
                });


                Log.d(TAG, "local nodes:" + getLocalNodeId());
                String nodes = getRemoteNodeId();
                Log.d(TAG, "remote nodes:" + nodes.toString());
                return null;
            }
        }.execute();
    }

    private void sendNotification() {
        Intent intent = new Intent(ACTION_OPEN_CUSTOM_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("First Notification")
                        .setContentText("Hello world notification!")
                        .extend(new NotificationCompat.WearableExtender().setDisplayIntent(pendingIntent));

        Notification notification = notificationBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(101, notification);
    }
}
