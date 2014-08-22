/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.megabox.listadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clionelabs.lighthouse.R;
import com.clionelabs.megabox.model.History;

import java.util.List;

/**
 * Created by gilbert on 14-8-22.
 */
public class HistoryListAdaptor extends BaseAdapter {

    private List<History> mHistories;
    private Context mContext;

    public HistoryListAdaptor(Context context, List<History> histories) {
        this.mHistories = histories;
        this.mContext = context;
    }

    public void setHistories(List<History> mHistories) {
        this.mHistories = mHistories;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mHistories.size();
    }

    @Override
    public Object getItem(int position) {
        return mHistories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final History history = (History) getItem(position);
        final HistoryListItemViewHolder historyListItemViewHolder;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_history, null);
            historyListItemViewHolder = new HistoryListItemViewHolder();
            historyListItemViewHolder.mIvType = (ImageView) convertView.findViewById(R.id.mIvType);
            historyListItemViewHolder.mTvBody = (TextView) convertView.findViewById(R.id.mTvProductName);
            historyListItemViewHolder.mTvTime = (TextView) convertView.findViewById(R.id.mTvTime);
            historyListItemViewHolder.mTvDuration = (TextView) convertView.findViewById(R.id.mTvDuration);
            historyListItemViewHolder.mTvDurationUnit = (TextView) convertView.findViewById(R.id.mTvDurationUnit);
        } else {
            historyListItemViewHolder = (HistoryListItemViewHolder) convertView.getTag();
        }

        historyListItemViewHolder.mIvType.setImageResource(history.getHistoryDrawableId());
        historyListItemViewHolder.mTvBody.setText(history.getDescription());
        historyListItemViewHolder.mTvTime.setText(history.getTimeString());

        if (history.getDurationInSeconds() != null) {
            historyListItemViewHolder.mTvDuration.setText(history.getDuration());
            historyListItemViewHolder.mTvDuration.setVisibility(View.VISIBLE);
            historyListItemViewHolder.mTvDurationUnit.setVisibility(View.VISIBLE);
        } else {
            historyListItemViewHolder.mTvDuration.setVisibility(View.GONE);
            historyListItemViewHolder.mTvDurationUnit.setVisibility(View.GONE);
        }

        convertView.setTag(historyListItemViewHolder);

        return convertView;
    }

    public class HistoryListItemViewHolder {
        public ImageView mIvType;
        public TextView mTvBody;
        public TextView mTvTime;
        public TextView mTvDuration;
        public TextView mTvDurationUnit;
    }
}
