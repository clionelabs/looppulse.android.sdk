/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.lighthouse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by simon on 5/6/14.
 */
public class LightHouseAdapter extends ArrayAdapter<BeaconEvent> {

    public LightHouseAdapter(Context context, int resource) {
        super(context, resource);
    }

    public View getView(int position, View view, ViewGroup parent) {

        if (view== null) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.lighthouse_list_item, parent, false);
        }

        BeaconEvent event = getItem(position);
        configureView(view, event);

        return view;
    }

    private void configureView(View view, BeaconEvent event) {
        TextView textView = (TextView) view.findViewById(R.id.textView);

        String text;
        int color;

        if (event.getMajor() == 28364 ) {
            text = "Blue Zone   ";
            color = 0xFF0000FF;
        } else if (event.getMajor() == 54330) {
            text = "Green Zone  ";
            color = 0xFF00FF00;
        } else if (event.getMajor() == 58020) {
            text = "Purple Zone ";
            color = 0xFFCC00FF;
        } else if (event.getMajor() == 100) {
            text = "Red Zone    ";
            color = 0xFFFF0000;
        } else if (event.getMajor() == 10) {
            text = "White Zone  ";
            color =0xFF000000;
        } else {
            text = "Unknown Zone";
            color = 0xFF000000;
        }

        double secondsElapsed = Math.round((new Date().getTime() - event.getCreatedAt().getTime()) / 1000.0);

        textView.setText(String.format("%s %.0fs", text, secondsElapsed));
        textView.setTextColor(color);
    }


}
