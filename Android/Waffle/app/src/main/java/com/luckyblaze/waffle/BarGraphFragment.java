package com.luckyblaze.waffle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BarGraphFragment extends Fragment {
    FragmentActivity listener;
    private BarGraphView mBarGraphView;
    private double mPercent = 0.0;
    private TextView mTvSensorValue;
    private String mMsg = "Null";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (FragmentActivity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bar_graph, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBarGraphView = (BarGraphView) view.findViewById(R.id.bar_graph);
        mBarGraphView.setPercent(mPercent);
        mTvSensorValue = (TextView) view.findViewById(R.id.sensor_value);
        mTvSensorValue.setText(mMsg);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setPercent(double percent){
        mPercent = percent;
        if (mTvSensorValue != null) {
            mTvSensorValue.setText(String.valueOf((int)mPercent));
        }
        if (mBarGraphView != null) {
            mBarGraphView.setPercent(mPercent);
        }
    }
}
