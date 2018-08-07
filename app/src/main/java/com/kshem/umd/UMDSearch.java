package com.kshem.umd;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by kshem on 4/15/18.
 */

public class UMDSearch {
    public interface SearchListener{
        void onSuccess(ArrayList<Song> songs);
        void onFailure();
    }

    private Context context;
    private OkHttpClient client;
    private SearchListener listener;
    private boolean status = false;
    private ArrayList<Song> songs;

    public UMDSearch(Activity activity){
        this.context = activity;
        this.songs = new ArrayList<>();
        this.client = Config.getNewHttpClient();
    }

    public void setResultListener(SearchListener listener){
        this.listener = listener;
    }

    public void search(String query){

        songs.clear();
        // Set up the post  data
        RequestBody data = new FormBody.Builder()
                .add("q", query)
                .add("sort", "2")
                .add("count", "300")
                .add("performer_only", "0")
                .build();

        Log.d("UMD : request url:",Config.APP_URL);
        final Request request = new Request.Builder()
                .url(Config.APP_URL)
                .post(data)
                .build();

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Response response = client.newCall(request).execute();
                    status = response.isSuccessful();

                    if(status){
                        String responseString = response.body().string();

                        //The response string has a "(" at the beginning and ");" at the end,
                        //therefore will cause an error if we try to create a JSON object from
                        //the string

                        responseString = responseString.substring(1, responseString.length() - 2);

                        JSONObject responseObject = new JSONObject(responseString);

                        JSONArray responseData = responseObject.getJSONArray("response");

                        // Remember: The first item in the json array is not an object
                        // and we don't need it.
                        for(int i = 1; i < responseData.length(); i++){
                            Song song = new Song(responseData.getJSONObject(i));
                            songs.add(song);
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(status){
                    listener.onSuccess(songs);
                }else{
                    listener.onFailure();
                }
            }
        };

        task.execute();
    }
}
