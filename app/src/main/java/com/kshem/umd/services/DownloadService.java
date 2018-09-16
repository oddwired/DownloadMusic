package com.kshem.umd.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kshem.umd.Config;
import com.kshem.umd.R;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.util.ArrayList;

public class DownloadService extends Service {

    private final static String TAG = DownloadService.class.getSimpleName();

    private Handler handler;

    private ArrayList<DownloadRequest> downloadList;
    private Context context;
    private ThinDownloadManager downloadManager;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public DownloadService getService(){
            return DownloadService.this;
        }
    }

    private void createNotification(DownloadRequest downloadRequest, final int id){

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentTitle("Music Download")
                .setContentText("Download in progress")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        final int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        mBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(id, mBuilder.build());

        downloadRequest.setStatusListener(new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                mBuilder.setContentText("Download complete");
                notificationManager.notify(id, mBuilder.build());
            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                mBuilder.setContentText("Download failed");
                notificationManager.notify(id, mBuilder.build());
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                mBuilder.setProgress(PROGRESS_MAX, progress, false);
                notificationManager.notify(id, mBuilder.build());
            }
        });


        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(id, mBuilder.build());
    }

    public void addDownloadRequest(final DownloadRequest downloadRequest){
        this.downloadList.add(downloadRequest);
        downloadManager.add(downloadRequest);

        createNotification(downloadRequest, downloadList.size());
    }

    public ArrayList<DownloadRequest> getDownloadList() {
        return downloadList;
    }

    public ThinDownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadList = new ArrayList<>();
        context = getApplicationContext();
        downloadManager = new ThinDownloadManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(downloadList.size() == 0){
            stopSelf();
        }
        return super.onUnbind(intent);
    }
}
