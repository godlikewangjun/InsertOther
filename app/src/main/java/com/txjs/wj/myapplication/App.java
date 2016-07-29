package com.txjs.wj.myapplication;

import android.app.Application;

/**
 * @author wangjun
 * @version 1.0
 * @date 2016/7/25
 */
public class App extends Application{
    public static Application application;
    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
    }
}
