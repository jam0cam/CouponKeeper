package com.jiacorp.cwallet;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by jitse on 1/19/15.
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = getResources().getColor(R.color.light_gray);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, null, color);
            setTaskDescription(td);
        }

    }
}
