package com.qgqaqgqa.myokhttpdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 封装OKHttp
 * User: Created by 钱昱凯
 * Date: 2017/10/20 0020
 * Time: 17:29
 */
public class MyOKHttp3Engine {

    private static MyOKHttp3Engine mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;

    public static MyOKHttp3Engine getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MyOKHttp3Engine.class) {
                if (mInstance == null) {
                    mInstance = new MyOKHttp3Engine(context);
                }
            }
        }
        return mInstance;
    }

    private MyOKHttp3Engine(Context context) {
        File sdcache = context.getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
        mOkHttpClient = builder.build();
        mHandler = new Handler();

    }

    /**
     * 异步get请求
     *
     * @param url
     * @param callback
     */
    public void getAsynHttp(String url, ResultCallback callback) {

        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        dealResult(call, callback);

    }


    private void dealResult(Call call, final ResultCallback callback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedCallback(call.request(), e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sendSuccessCallback(response, callback);
            }


            private void sendSuccessCallback(final Response object, final ResultCallback callback) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onResponse(object);
                        }
                    }
                });
            }

            private void sendFailedCallback(final Request request, final Exception e, final ResultCallback callback) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onError(request, e);
                    }
                });
            }

        });
    }
}
