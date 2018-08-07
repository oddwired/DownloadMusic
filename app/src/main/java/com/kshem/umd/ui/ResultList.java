package com.kshem.umd.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kshem.umd.R;
import com.kshem.umd.Song;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by kshem on 4/15/18.
 */

public class ResultList extends BaseAdapter {

    private Activity activity;
    private ArrayList<Song> songs;

    public ResultList(Activity activity, ArrayList<Song> songs){
        this.activity = activity;
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Song getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView songName;
        CheckBox selected;
        if(convertView == null){
            convertView = activity.getLayoutInflater().inflate(R.layout.song_item, parent, false);
        }

        final Song songItem = getItem(position);

        songName = (TextView) convertView.findViewById(R.id.name);
        selected = (CheckBox) convertView.findViewById(R.id.btnSelected);
        selected.setChecked(songItem.isSelected());

        songName.setText(songItem.getName());

        selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songItem.isSelected()){
                    songItem.setSelected(false);
                }else{
                    songItem.setSelected(true);
                }
            }
        });

        return convertView;
    }
}
