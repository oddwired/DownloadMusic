package com.kshem.umd;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
    private SearchListener listener;
    private String TAG = this.getClass().getSimpleName();

    public UMDSearch(Activity activity){
        this.context = activity;

    }

    public void setResultListener(SearchListener listener){
        this.listener = listener;
    }

    public void search(String query){

        // Set up the post  data
        RequestBody data = new FormBody.Builder()
                .add("q", query)
                .add("sort", "2")
                .add("count", "300")
                .add("performer_only", "0")
                .build();

        Log.d("UMD : request url:",Config.APP_URL);
        Request request = new Request.Builder()
                .url(Config.APP_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-agent", "Mozilla/5.0 (X11; Linux i686; rv:10.0) Gecko/20100101 Firefox/10.0")
                .post(data)
                .build();

        Log.i(TAG, "search: "+ request.headers());

        new SearchTask(context, request, listener).execute();
    }

    private static class SearchTask extends AsyncTask<Void, Void, ArrayList<Song>> {

        private WeakReference<Context> contextWeakReference;
        private OkHttpClient client;
        private Request request;
        private ArrayList<Song> songs;
        private SearchListener callback;

        private String TAG = "AsyncTask";

        SearchTask(Context context, Request request, SearchListener callback){
            this.contextWeakReference = new WeakReference<>(context);
            this.client = Config.getNewHttpClient();
            this.request = request;
            this.callback = callback;
        }

        @Override
        protected ArrayList<Song> doInBackground(Void... voids) {

            try {
                Response response = client.newCall(request).execute();

                Log.i(TAG, "doInBackground: "+ response.toString());

                if(response.isSuccessful()){
                    songs = new ArrayList<>();

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
                response.body().close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return songs;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            super.onPostExecute(songs);

            if(songs != null){
                callback.onSuccess(songs);
            }else{
                callback.onFailure();
            }
        }
    }
}
