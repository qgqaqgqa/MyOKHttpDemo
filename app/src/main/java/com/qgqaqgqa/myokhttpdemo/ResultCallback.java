package com.qgqaqgqa.myokhttpdemo;

import okhttp3.Request;
import okhttp3.Response;

/**
 * User: Created by 钱昱凯
 * Date: 2017/10/20 0020
 * Time: 17:38
 */
public abstract class ResultCallback<T>
{
    public abstract void onError(Request request, Exception e);

    public abstract void onResponse(Response response);
}