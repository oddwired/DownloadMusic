package com.kshem.umd.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kshem.umd.Config;
import com.kshem.umd.R;
import com.kshem.umd.Song;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by kshem on 4/16/18.
 */

public class DownloadList extends BaseAdapter {

    private Activity activity;
    private ArrayList<DownloadRequest> requests;
    private ThinDownloadManager downloadManager;
    private String TAG = this.getClass().getSimpleName();

    public DownloadList(Activity activity, ArrayList<DownloadRequest> requests, ThinDownloadManager downloadManager){
        this.activity = activity;
        this.requests = requests;
        this.downloadManager = downloadManager;
    }

    @Override
    public int getCount() {
        return requests.size();
    }

    @Override
    public DownloadRequest getItem(int position) {
        return requests.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getDownloadId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final TextView file_name, status;
        final ProgressBar progressBar;
        Button cancel;

        if(convertView == null){
            convertView = activity.getLayoutInflater().inflate(R.layout.download_item, parent, false);
        }

        final DownloadRequest downloadRequest = getItem(position);

        file_name = (TextView) convertView.findViewById(R.id.fileName);
        status = (TextView) convertView.findViewById(R.id.status);
        progressBar = (ProgressBar) convertView.findViewById(R.id.downloadProgress);
        cancel = (Button) convertView.findViewById(R.id.cancel);

        file_name.setText(downloadRequest.getDestinationURI().toString());
        setProgressColor(progressBar, 0);

        downloadRequest.setStatusListener(new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                status.setText("Download complete!");
            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                setProgressColor(progressBar, 1);
                status.setText(errorMessage);
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                setProgressColor(progressBar, 0);
                progressBar.setProgress(progress);

                String totalSize;
                if(totalBytes < 1048576){
                    totalSize = String.valueOf(totalBytes / 1024) + " KB";
                }else{
                    totalSize = String.valueOf(totalBytes / 1048576) + " MB";
                }

                String downloadedSize;
                if(downloadedBytes < 1048576){
                    downloadedSize = String.valueOf(downloadedBytes / 1024) + " KB";
                }else{
                    downloadedSize = String.valueOf(downloadedBytes / 1048576) + " MB";
                }

                status.setText("Downloaded " + downloadedSize + " of " + totalSize);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadManager.cancel(downloadRequest.getDownloadId()) == 1){
                    setProgressColor(progressBar, 0);
                    status.setText("Download canceled");
                }
            }
        });

        Log.i(TAG, "Created an entry" + downloadRequest.getUri());
        return convertView;
    }


    private void setProgressColor(ProgressBar progressBar, int color){
        if(color == 0){  //Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP -- use this if it doesn't work
            progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        }else if(color == 1){
            progressBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        }
    }
}
