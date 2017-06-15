package com.luckyblaze.waffle;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.DataStoreEventListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.MilkcocoaException;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DataStoreEventListener {

    private String TAG = MainActivity.class.getSimpleName();
    private Physicaloid mPhysicaloid;
    private LinearLayout parentLayout;
    private View mLayout;

    //display datas
    private Handler mHandler;
    private JSONObject mJson;
    private int val = 0;
    private TextFragment mTextFragment;
    private BarGraphFragment mBarGraphFragment;
    private ChartFragment mChartFragment;

    //connect Milkcocoa
    private MilkCocoa milkcocoa;
    private DataStore messagesDataStore;
    private String milkcocoaAppId = "";

    //Send data according to a timer
    private int timer = 0;
    private Runnable mTimerCode;

    //tab bar with fragments
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    displayTextFragment();
                    return true;
                case R.id.navigation_dashboard:
                    displayBarGraphFragment();
                    return true;
                case R.id.navigation_notifications:
                    displayChartFragment();
                    return true;
            }
            return false;
        }
    };

    protected void displayTextFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mTextFragment.isAdded()) { // if the fragment is already in container
            ft.show(mTextFragment);
        } else { // fragment needs to be added to frame container
            ft.add(R.id.content, mTextFragment, "TextFragment");
        }
        if (mBarGraphFragment.isAdded()) { ft.hide(mBarGraphFragment); }
        if (mChartFragment.isAdded()) { ft.hide(mChartFragment); }
        ft.commit();
    }

    protected void displayBarGraphFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mBarGraphFragment.isAdded()) { // if the fragment is already in container
            ft.show(mBarGraphFragment);
        } else { // fragment needs to be added to frame container
            ft.add(R.id.content, mBarGraphFragment, "BarGraphFragment");
        }
        if (mTextFragment.isAdded()) { ft.hide(mTextFragment); }
        if (mChartFragment.isAdded()) { ft.hide(mChartFragment); }
        ft.commit();
    }

    protected void displayChartFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mChartFragment.isAdded()) { // if the fragment is already in container
            ft.show(mChartFragment);
        } else { // fragment needs to be added to frame container
            ft.add(R.id.content, mChartFragment, "ChartFragment");
        }
        if (mTextFragment.isAdded()) { ft.hide(mTextFragment); }
        if (mBarGraphFragment.isAdded()) { ft.hide(mBarGraphFragment); }
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.content);

        if(savedInstanceState==null){
            try {
                mTextFragment = TextFragment.class.newInstance();
                mBarGraphFragment = BarGraphFragment.class.newInstance();
                mChartFragment = ChartFragment.class.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            displayTextFragment();
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mPhysicaloid = new Physicaloid(getApplicationContext());
        parentLayout = (LinearLayout) findViewById(R.id.container);
        mHandler = new Handler();
    }

    private void connectMilkcocoa(String appId) {
        this.messagesDataStore = null;

        if (this.milkcocoa != null) {
            if(this.milkcocoa.getSession() != null){
                this.milkcocoa.disconnect();
            }
        }

        try {
            this.milkcocoa = new MilkCocoa(appId);
        } catch (MilkcocoaException e) {
            e.printStackTrace();
        }

        if (this.milkcocoa.getSession() != null) {
            this.messagesDataStore = this.milkcocoa.dataStore("bucket");
            Snackbar.make(mLayout, R.string.milkcocoa_available, Snackbar.LENGTH_SHORT).show();
//            Streaming stream = this.messagesDataStore.streaming();
//            stream.size(25);
//            stream.sort("desc");
//            stream.addStreamingListener(new StreamingListener() {
//
//                @Override
//                public void onData(ArrayList<DataElement> arg0) {
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            stream.next();
//
//            this.messagesDataStore.addDataStoreEventListener(this);
//            this.messagesDataStore.on("push");
        } else {
            Snackbar.make(mLayout, R.string.milkcocoa_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void sendDataToMilkcocoa(int val){
        DataElementValue params = new DataElementValue();
        params.put("sensorValue", val);
        Date date = new Date();
        params.put("date", date.getTime());
        if (this.messagesDataStore != null) {
            this.messagesDataStore.send(params);
        }
    }

    public void pushDataToMilkcocoa(int val){
        DataElementValue params = new DataElementValue();
        params.put("sensorValue", val);
        Date date = new Date();
        params.put("date", date.getTime());
        if (this.messagesDataStore != null) {
            this.messagesDataStore.push(params);
        }
    }

    @Override
    public void onPushed(DataElement dataElement) {
    }
    @Override
    public void onSetted(DataElement dataElement) {
    }
    @Override
    public void onSended(DataElement dataElement) {
    }
    @Override
    public void onRemoved(DataElement dataElement) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectWaffle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mPhysicaloid.isOpened()) {
            menu.findItem(R.id.menu_connected).setVisible(false);
            menu.findItem(R.id.menu_disconnected).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connected).setVisible(true);
            menu.findItem(R.id.menu_disconnected).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connected:
                disconnectWaffle();
                break;
            case R.id.menu_disconnected:
                connectWaffle();
                break;
            case R.id.milkcocoa_setting:
                showMilkcocoaDialog();
                break;
        }
        return true;
    }

    public void showMilkcocoaDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog, (ViewGroup)findViewById(R.id.layout_dialog));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        TextView appIdText = (TextView)layout.findViewById(R.id.appId_text);
        appIdText.setText(milkcocoaAppId);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText text = (EditText)layout.findViewById(R.id.edit_text);
                milkcocoaAppId = text.getText().toString();
                connectMilkcocoa(milkcocoaAppId + ".mlkcca.com");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // nothing to do
            }
        });
        builder.create().show();
    }

    public void connectWaffle(){
        try{
            if(mPhysicaloid.open()) {
                Snackbar.make(parentLayout, R.string.snackbar_connected, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

                mPhysicaloid.addReadListener(new ReadLisener() {
                    @Override
                    public void onRead(int size) {
                        final byte[] buf = new byte[size];
                        mPhysicaloid.read(buf, size);
//                        Log.i(TAG, String.valueOf(size));

                        final String readStr;
                        try {
                            readStr = new String(buf, "UTF-8");
//                            Log.i(TAG, "readStr is " + readStr);
                        } catch (UnsupportedEncodingException e) {
                            return;
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (parseJson(readStr) != null) {
                                    mJson = parseJson(readStr);
//                                    Log.i(TAG, "parsed JSON is " + String.valueOf(mJson));
                                    try {
                                        Double temp = mJson.getDouble("A0") / 1023 * 100;
                                        val = temp.intValue();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    //for TextFragment
                                    if (mTextFragment != null) {
                                        mTextFragment.setJsonData(mJson);
                                    }
                                    //for BarGraphFragment
                                    if (mBarGraphFragment != null) {
                                        mBarGraphFragment.setPercent(val);
                                    }
                                    //for ChartFragment

                                }
                            }
                        });
                    }
                });
            } else {
                Snackbar.make(parentLayout, R.string.snackbar_connection_failed, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        } catch(Exception e) {
            Log.e(TAG, "Physicaloid connection exception ", e);
        }
        invalidateOptionsMenu();
    }

    public void disconnectWaffle(){
        if(mPhysicaloid.isOpened()){
            mPhysicaloid.close();
            Snackbar.make(parentLayout, R.string.snackbar_disconnected, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
        invalidateOptionsMenu();
    }

    public JSONObject parseJson(String string){
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            //e.printStackTrace();
            return null;
        }
    }

    // Timer
    private void startTimer(){
        // Start a timer
        mTimerCode = new Runnable() {
            @Override
            public void run() {
                // Repeat this the same runnable code block again another 5 seconds
                mHandler.postDelayed(this, 5000);
                // Send data to Milkcocoa
                if(mPhysicaloid.isOpened()){
                    pushDataToMilkcocoa(val);
                }
            }
        };
        mHandler.post(mTimerCode);
    }

    private void stopTimer(){
        // Removes pending code execution
        mHandler.removeCallbacks(mTimerCode);
    }

}
