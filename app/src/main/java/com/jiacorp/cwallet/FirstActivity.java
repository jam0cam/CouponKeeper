package com.jiacorp.cwallet;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class FirstActivity extends ActionBarActivity {

    @InjectView(R.id.button)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        ButterKnife.inject(this);


    }

    @OnClick(R.id.button)
    public void clicked() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        File[] fi = mediaStorageDir.listFiles();

        List<File> files = Arrays.asList(fi);

        for (File f : files) {
            Coupon.fromFile(f);
        }

    }
}
