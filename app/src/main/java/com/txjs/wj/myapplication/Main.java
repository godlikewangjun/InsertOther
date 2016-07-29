package com.txjs.wj.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * @author wangjun
 * @version 1.0
 * @date 2016/7/25
 */
public class Main extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.main);
        this.finish();
    }
}
