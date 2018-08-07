package com.kshem.umd.ui;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kshem.umd.Config;
import com.kshem.umd.R;
import com.kshem.umd.Song;
import com.kshem.umd.UMDSearch;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.ThinDownloadManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public final static String BROADCAST_ACTION = "com.kshem.umdDownload";
    private static ArrayList<DownloadRequest> downloadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        downloadList = new ArrayList<>();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), downloadList);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private EditText query;
        private Button search, download;
        private ListView songsList, downloadListView;
        private UMDSearch umdSearch;
        private ResultList songsAdapter;
        private DownloadList downloadAdapter;
        private ProgressDialog pd;
        private ArrayList<Song> searchResults;
        //private ArrayList<Song> downloadList;
        private ThinDownloadManager downloadManager;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            pd = new ProgressDialog(getContext());
            downloadManager = new ThinDownloadManager();

            if(getArguments().getInt(ARG_SECTION_NUMBER) == 1){
                rootView = inflater.inflate(R.layout.fragment_search, container, false);
                query = (EditText) rootView.findViewById(R.id.searchInput);
                search = (Button) rootView.findViewById(R.id.btnSearch);
                download = (Button) rootView.findViewById(R.id.btnDownload);
                songsList = (ListView) rootView.findViewById(R.id.songList);

                umdSearch = new UMDSearch(getActivity());

                umdSearch.setResultListener(new UMDSearch.SearchListener() {
                    @Override
                    public void onSuccess(ArrayList<Song> songs) {
                        //searchResults.clear();
                        searchResults = songs;
                        songsAdapter = new ResultList(getActivity(), searchResults);
                        songsList.setAdapter(songsAdapter);
                        songsAdapter.notifyDataSetChanged();
                        pd.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        pd.dismiss();
                        new AlertDialog.Builder(getContext())
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
                                        .setDownloadContext(getActivity());

                                downloadList.add(downloadRequest);
                               // Toast.makeText(getContext(), String.valueOf(downloadList.size()), Toast.LENGTH_SHORT).show();
                                downloadManager.add(downloadRequest);
                            }
                        }

                        Intent intent = new Intent(BROADCAST_ACTION);

                        getContext().sendBroadcast(intent);

                        TabLayout tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
                        tabhost.getTabAt(1).select();


                    }
                });

            }else if(getArguments().getInt(ARG_SECTION_NUMBER) == 2){
                rootView = inflater.inflate(R.layout.fragment_downloading, container, false);
                downloadListView = (ListView) rootView.findViewById(R.id.listDownloading);

                BroadcastReceiver br = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        //Toast.makeText(getContext(), String.valueOf(downloadList.size()), Toast.LENGTH_LONG).show();
                        downloadAdapter = new DownloadList(getActivity(), downloadList, downloadManager);
                        downloadListView.setAdapter(downloadAdapter);
                    }
                };

                IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
                getActivity().registerReceiver(br, intFilt);


            }else{
                rootView = inflater.inflate(R.layout.fragment_complete, container, false);
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<DownloadRequest> downloadRequests) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Search";
                case 1:
                    return "Downloading";
                case 2:
                    return "Complete";
            }
            return null;
        }
    }
}
