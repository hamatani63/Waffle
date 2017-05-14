package com.luckyblaze.waffle;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private Physicaloid mPhysicaloid;
    private LinearLayout parentLayout;

    private Handler mHandler;
    private JSONObject mJson;
    private TextFragment mTextFragment;
    private BarGraphFragment mBarGraphFragment;
    private ChartFragment mChartFragment;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectWaffle();
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
        }
        return true;
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

                        final String readStr;
                        try {
                            readStr = new String(buf, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return;
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (parseJson(readStr) != null) {
                                    mJson = parseJson(readStr);
                                    //for TextFragment
                                    if (mTextFragment != null) {
                                        mTextFragment.setJsonData(mJson);
                                    }
                                    //for BarGraphFragment
                                    if (mBarGraphFragment != null) {
                                        try {
                                            mBarGraphFragment.setPercent(mJson.getDouble("A0")/1023*100);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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

}
