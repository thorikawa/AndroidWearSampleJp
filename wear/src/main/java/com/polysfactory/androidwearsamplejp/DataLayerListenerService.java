package com.polysfactory.androidwearsamplejp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by poly on 7/6/14.
 */
public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "TEST";
    private static final String PATH = "/count";
    private static final String COUNT_KEY = "COUNT_KEY";
    private GoogleApiClient mGoogleApiClient;
    private int count = 0;
    private static final String START_ACTIVITY_PATH = "/start/MainActivity";
    private static final String UPDATE_NOTIFICATION_PATH = "/updated/notification";

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        // putCount();
                        getStoredCount();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (START_ACTIVITY_PATH.equals(messageEvent.getPath())) {
            Intent intent = new Intent(this, MyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
            int count = dataMap.getInt(COUNT_KEY);
            Log.d(TAG, "updated:" + count);

            // android:allowEmbedded="true" is required for target activity
            Intent intent = new Intent(this, NotificationActivity.class);
            intent.putExtra(NotificationActivity.EXTRA_KEY_COUNT, count);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .extend(
                            new Notification.WearableExtender()
                                    .setHintHideIcon(true)
                                    .setDisplayIntent(pendingIntent)
                    )
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1000, notification);

            break;
        }
    }

    private void getStoredCount() {
        // Uri uri = new Uri.Builder().scheme("wear").authority("abcd77b0-5828-4f46-b94e-3a3df9e97f98").path(PATH).build();
        // Log.d(TAG, "access to:[" + uri.toString() + "]");
        PendingResult<DataItemBuffer> dataItems = Wearable.DataApi.getDataItems(mGoogleApiClient, new Uri.Builder().scheme("wear").path("/count").build());
        dataItems.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    Log.d(TAG, "Uri: " + item.getUri().toString());
                    int count = DataMapItem.fromDataItem(item).getDataMap().getInt(COUNT_KEY);
                    Log.d(TAG, "count: " + count);
                }
            }
        });

        /*
        Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                DataItem dataItem = dataItemResult.getDataItem();
                Log.d(TAG, "local dataItemResult:" + dataItem);
                if (dataItem != null) {
                    int count = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap().getInt(COUNT_KEY);
                    Log.d(TAG, "stored count:" + count);
                }
            }
        });
        */
    }

    private void putCount() {
        PutDataMapRequest dataMap = PutDataMapRequest.create(PATH);
        dataMap.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        Log.d(TAG, "data start sending" + request.getUri());
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "changed:" + dataItemResult.getDataItem().getUri());
            }
        });
    }
}
