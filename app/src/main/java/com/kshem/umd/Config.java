package com.kshem.umd;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.kshem.umd.okhttphack.Tls12SocketFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Created by kshem on 4/15/18.
 */

public class Config {
    public static final String APP_URL = "https://myfreemp3cc.com/api/search.php";
    public static final String SERVER_URL = "https://sharethisurls.com/api/get_song.php?id=%s";
    public static final String DOWNLOAD_DIRECTORY = Environment.getExternalStorageDirectory() + File.separator + "UMD/";

    public static String STARTFOREGROUND_ACTION = "com.kshem.umd.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.kshem.umd.action.stopforeground";
    public static String PAUSE_ACTION = "com.kshem.umd.action.pausedownload";
    public static String CANCEL_ACTION = "com.kshem.umd.action.canceldownload";

    public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }

    public static OkHttpClient getNewHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS);

        return enableTls12OnPreLollipop(client).build();
    }
}
