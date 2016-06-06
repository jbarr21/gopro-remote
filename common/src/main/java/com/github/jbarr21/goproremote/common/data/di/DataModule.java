package com.github.jbarr21.goproremote.common.data.di;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v4.util.Pair;

import com.github.jbarr21.goproremote.common.data.api.GoProApi;
import com.github.jbarr21.goproremote.common.data.api.PasswordSigningInterceptor;
import com.github.jbarr21.goproremote.common.data.storage.ConfigStorage;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.ryanharter.auto.value.moshi.AutoValueMoshiAdapterFactory;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.Observable;

@Module
public class DataModule {
    private static final String GO_PRO_ENDPOINT_URL = "http://10.5.5.9/";

    private static final long  MAX_CACHE_SIZE = 1024 * 1024 * 100L;
    private static final String OKHTTP_CACHE_DIR = "okhttpcache";

    private final boolean debug;

    public DataModule(final boolean debug) {
        this.debug = debug;
    }

    @Provides
    @Singleton
    public ConnectivityManager provideConnectivityManager(Application app) {
        return (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    @Provides
    @Singleton
    public Cache provideDiskCache(Context context) {
        File cacheDir = new File(context.getCacheDir(), OKHTTP_CACHE_DIR);
        return new Cache(cacheDir, MAX_CACHE_SIZE);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Application app, Cache diskCache, ConnectivityManager connectivityManager) {
        ConfigStorage configStorage = new ConfigStorage(app);
        PasswordSigningInterceptor signingInterceptor = new PasswordSigningInterceptor(configStorage);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(debug ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(diskCache)
                .connectTimeout(5, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .addInterceptor(signingInterceptor);
                //.addInterceptor(loggingInterceptor)
                //.addInterceptor(new CurlLoggingInterceptor())
                //.addNetworkInterceptor(new StethoInterceptor())
                //.cookieJar(CookieHandler.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Observable.from(connectivityManager.getAllNetworks())
                    .map(network -> Pair.create(network, connectivityManager.getNetworkInfo(network)))
                    .filter(networkPair -> networkPair.second.getType() == ConnectivityManager.TYPE_WIFI && networkPair.second.isConnected())
                    .map(networkPair -> networkPair.first)
                    .subscribe(network -> builder.socketFactory(network.getSocketFactory()));
        }

        return builder.build();
    }

    @Provides
    @Singleton
    public GoProApi provideGoProApi(OkHttpClient okHttpClient, Moshi moshi) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(HttpUrl.parse(GO_PRO_ENDPOINT_URL))
                .build()
                .create(GoProApi.class);
    }

    @Provides
    @Singleton
    public Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new AutoValueMoshiAdapterFactory())
                .build();
    }

    @Provides
    @Singleton
    public GoogleApiClient provideGoogleApiClient(Application app) {
        return new GoogleApiClient.Builder(app.getApplicationContext())
                .addApi(Wearable.API)
                .build();
    }


    // Override the single okHttpClient if necessary?
    public static void setOkHttpClient(OkHttpClient okHttpClient) {
//        Apis.okHttpClient = okHttpClient;
//        Apis.goProApi = null;
//        Apis.goProApi = getGoProApi();
    }
}
