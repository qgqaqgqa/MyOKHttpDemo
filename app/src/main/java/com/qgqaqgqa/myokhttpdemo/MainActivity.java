package com.qgqaqgqa.myokhttpdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private OkHttpClient mOkHttpClient;
    private String TAG = this.getClass().getName();
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getAsynHttp();
//        postAsynHttp();
//        postAsynFile();
//        downAsynFile();
        sendMultipart();
    }

    /**
     * 异步GET请求
     */
    private void getAsynHttp() {
        mOkHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url("http://www.baidu.com");
        //可以省略，默认是GET请求
        requestBuilder.method("GET", null);
        Request request = requestBuilder.build();
        Call mcall = mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i(TAG, "cache---" + str);
                } else {
                    response.body().string();
                    String str = response.networkResponse().toString();
                    Log.i(TAG, "network---" + str);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 异步POST请求
     */
    private void postAsynHttp() {
        mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("size", "10")
                .build();
        Request request = new Request.Builder()
                .url("http://api.1-blog.com/biz/bizserver/article/list.do")
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(TAG, str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 异步上传文件
     */
    private void postAsynFile() {
        mOkHttpClient=new OkHttpClient();
        File file = new File("/sdcard/okhttpdemo.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG,response.body().string());
            }
        });
    }

    /**
     * 异步下载
     */
    private void downAsynFile() {
        mOkHttpClient = new OkHttpClient();
        String url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg";
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File("/sdcard/okhttpdemo.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i(TAG, "IOException");
                    e.printStackTrace();
                }
                Log.d(TAG, "文件下载成功");
            }
        });
    }

    /**
     * 异步上传Multipart文件
     */
    private void sendMultipart(){
        mOkHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "okhttp")
                .addFormDataPart("image", "okhttp.jpg",
                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/okhttp.jpg")))
                .build();
        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + "...")
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, response.body().string());
            }
        });
    }
    /**
     * 设置超时时间
     */
    private void sendTime(){
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
        OkHttpClient mOkHttpClient=builder.build();
    }


    /**
     * 取消请求
     */
    private  void cancel(){
        final Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        Call call=null;
        call = mOkHttpClient.newCall(request);
        final Call finalCall = call;
        //100毫秒后取消call
        executor.schedule(new Runnable() {
            @Override public void run() {
                finalCall.cancel();
            }
        }, 100, TimeUnit.MILLISECONDS);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i(TAG, "cache---" + str);
                } else {
                    try {
                        response.body().string();

                    } catch (IOException e) {
                        Log.i(TAG, "IOException");
                        e.printStackTrace();
                    }
                    String str = response.networkResponse().toString();
                    Log.i(TAG, "network---" + str);
                }
            }
        });
        Log.i(TAG, "是否取消成功"+call.isCanceled());
    }
}
