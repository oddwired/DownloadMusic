package com.kshem.umd;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kshem on 4/15/18.
 */

public class Song {
    private int id;
    private int owner_id;
    private String artist;
    private String title;
    private String name;
    private int duration;
    private String song_id;
    private boolean selected = false;

    private static String map[] = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "a", "b", "c", "d", "e", "f", "g", "h", "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v", "x", "y", "z",
            "1", "2", "3"};

    private String encode(int input){
        int length = map.length;
        String encoded = "";

        if(input == 0){
            return map[0];
        }

        if(input < 0){
            input *= -1;
            encoded += "-";
        }

        while(input > 0){
            int val = input % length;
            input = input / length;
            encoded += map[val];
        }

        return encoded;
    }

    public Song(JSONObject jsonObject){
        try {
            this.id = jsonObject.getInt("id");
            this.owner_id = jsonObject.getInt("owner_id");
            this.artist = jsonObject.getString("artist");
            this.title = jsonObject.getString("title");
            this.duration = jsonObject.getInt("duration");
            this.name = this.artist + " - " + this.title;
            this.song_id = encode(this.owner_id) + ":" + encode(this.id);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public String getSongId() {
        return song_id;
    }

    public void setSongId(String song_id) {
        this.song_id = song_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setOwnerId(int owner_id) {
        this.owner_id = owner_id;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}
