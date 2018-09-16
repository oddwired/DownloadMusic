package com.kshem.umd.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.kshem.umd.R;
import com.kshem.umd.services.DownloadService;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.ThinDownloadManager;

import java.util.ArrayList;

public class DownloadListActivity extends AppCompatActivity {

    private ListView downloadListView;
    private DownloadList downloadAdapter;
    private ArrayList<DownloadRequest> downloadRequests;
    private ThinDownloadManager downloadManager;

    private boolean serviceBound = false;
    private DownloadService downloadService;
    private Intent serviceIntent;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.LocalBinder mBinder = (DownloadService.LocalBinder) service;
            downloadService = mBinder.getService();
            downloadRequests = downloadService.getDownloadList();
            downloadManager = downloadService.getDownloadManager();

            setUpUI();
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
        serviceIntent = new Intent(DownloadListActivity.this, DownloadService.class);
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
        setContentView(R.layout.activity_download_list);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    private void setUpUI(){
        downloadListView = (ListView) findViewById(R.id.listDownloading);
        downloadAdapter = new DownloadList(this, downloadRequests, downloadManager);
        downloadListView.setAdapter(downloadAdapter);
    }
}
