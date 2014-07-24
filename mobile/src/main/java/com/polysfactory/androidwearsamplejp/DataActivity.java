package com.polysfactory.androidwearsamplejp;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;


public class DataActivity extends Activity {

    private static final String TAG = "TEST";
    private static final String COUNT_KEY = "COUNT_KEY";
    private static final String PATH = "/count";
    private static final String START_ACTIVITY_PATH = "/start/MainActivity";

    private int count = 0;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
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

        ((Button) findViewById(R.id.button_open_wear_activity)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        fireMessageApi();
                        return null;
                    }
                }.execute();
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

    private void fireMessageApi() {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node, START_ACTIVITY_PATH, null).await();
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
            }
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

}
