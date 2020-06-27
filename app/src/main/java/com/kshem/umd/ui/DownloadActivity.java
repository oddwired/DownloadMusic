package com.kshem.umd.ui;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.kshem.umd.Config;
import com.kshem.umd.R;
import com.kshem.umd.Song;
import com.kshem.umd.UMDSearch;
import com.kshem.umd.services.DownloadService;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;

import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {

    private boolean serviceBound = false;
    private DownloadService downloadService;
    private Intent serviceIntent;
    private Context context;

    private EditText query;
    private Button search, download;
    private ListView songsList;
    private UMDSearch umdSearch;
    private ResultList songsAdapter;
    private ProgressDialog pd;
    private ArrayList<Song> searchResults;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.LocalBinder mBinder = (DownloadService.LocalBinder) service;
            downloadService = mBinder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private boolean downloadServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceIntent = new Intent(DownloadActivity.this, DownloadService.class);

        if(!downloadServiceRunning(DownloadService.class)){
            startService(serviceIntent);
        }

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceBound){
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        if(!checkConnection(this)){
            new AlertDialog.Builder(this)
                    .setMessage("Please check your internet connection!")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setCancelable(true).show();
        }

        context = getApplicationContext();
        pd = new ProgressDialog(DownloadActivity.this);

        query = (EditText) findViewById(R.id.searchInput);
        search = (Button) findViewById(R.id.btnSearch);
        download = (Button) findViewById(R.id.btnDownload);
        songsList = (ListView) findViewById(R.id.songList);

        umdSearch = new UMDSearch(this);

        umdSearch.setResultListener(new UMDSearch.SearchListener() {
            @Override
            public void onSuccess(ArrayList<Song> songs) {
                //searchResults.clear();
                searchResults = songs;
                songsAdapter = new ResultList(DownloadActivity.this, searchResults);
                songsList.setAdapter(songsAdapter);
                songsAdapter.notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onFailure() {
                pd.dismiss();
                new AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("An error occurred. Please try again later!")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true).show();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(query.getText())){
                    query.setError("Please enter the artist or a song you are looking for.");
                }else{
                    umdSearch.search(query.getText().toString());
                    pd.setMessage("Please wait...");
                    pd.show();
                }
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(Song song : searchResults){
                    if(song.isSelected()){

                        Uri downloadUri = Uri.parse(Config.SERVER_URL + song.getSongId());
                        Uri destinationUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/UMD/"  + song.getName() + ".mp3");
                        //Toast.makeText(getContext(), destinationUri.toString(), Toast.LENGTH_LONG).show();

                        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                                .setRetryPolicy(new DefaultRetryPolicy())
                                .setDownloadContext(context);


                        downloadService.addDownloadRequest(downloadRequest);

                    }
                }

            }
        });
    }

    public static boolean checkConnection(Context con)
    {
        boolean status = false;
        try
        {
            ConnectivityManager cm = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);

            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);

                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                } else {
                    status = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return status;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(DownloadActivity.this, DownloadListActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
