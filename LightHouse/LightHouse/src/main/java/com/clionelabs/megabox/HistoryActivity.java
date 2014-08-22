/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.megabox;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.clionelabs.lighthouse.R;
import com.clionelabs.megabox.listadapter.HistoryListAdaptor;
import com.clionelabs.megabox.model.History;
import com.google.common.collect.Lists;

import java.util.List;

public class HistoryActivity extends Activity {

    private HistoryListAdaptor mHistoryListAdaptor;
    private ListView mLvHistory;
    private List<History> mHistories;

    //TODO configurable
    private static String visitorId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mLvHistory = (ListView)findViewById(R.id.mLvHistory);
        mHistories = Lists.newArrayList();
        mHistoryListAdaptor = new HistoryListAdaptor(this, mHistories);
        mLvHistory.setAdapter(mHistoryListAdaptor);
    }

}
