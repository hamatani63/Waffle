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

import org.json.JSONException;
import org.json.JSONObject;

public class TextFragment extends Fragment {
    FragmentActivity listener;
    private TextView mTextMessage;

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
        return inflater.inflate(R.layout.fragment_text, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTextMessage = (TextView) view.findViewById(R.id.message);
        mTextMessage.setText(R.string.title_home_message);
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

    public void setJsonData(JSONObject json){
        try {
            mTextMessage.setText(json.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
