/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.megabox;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.clionelabs.lighthouse.R;
import com.clionelabs.megabox.listadapter.HistoryListAdaptor;
import com.clionelabs.megabox.model.History;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.common.collect.Lists;

import java.util.List;

public class HistoryActivity extends Activity {

    private HistoryListAdaptor mHistoryListAdaptor;
    private ListView mLvHistory;

    //TODO configurable
    private static String visitorId = "753E29AD-B113-4F96-8989-4E2FCA8748D4";
    private static String endpoint = "https://looppulse-megabox.firebaseio.com/visitors/";
    private static String api = "/logs";

    private static String getUrl() {
        return endpoint + visitorId + api;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mLvHistory = (ListView)findViewById(R.id.mLvHistory);
        mHistoryListAdaptor = new HistoryListAdaptor(this, Lists.<History>newArrayList());
        mLvHistory.setAdapter(mHistoryListAdaptor);

        Firebase firebase = new Firebase(getUrl());
        firebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<History> histories = Lists.newArrayList();
                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                    histories.add(dss.getValue(History.class));
                }
                mHistoryListAdaptor.setHistories(histories);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(HistoryActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mLvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ObjectMapper om = new ObjectMapper();
                final History h = (History) parent.getAdapter().getItem(position);

                String msg;
                try {
                    msg = om.writeValueAsString(h);
                } catch (JsonProcessingException e) {
                    msg = e.getMessage();
                    e.printStackTrace();
                }
                Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
