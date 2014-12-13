package com.jiacorp.couponkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class ListActivity extends ActionBarActivity {

    @InjectView(R.id.rv)
    RecyclerView mRecyclerView;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.btn_add)
    ImageButton mBtnAdd;

    private LinearLayoutManager mLayoutManager;

    MyDBHander mDbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDbHandler = ((CouponApplication)getApplication()).getDbHandler();

        List<Coupon> coupons2 = mDbHandler.getAllCoupons();

        MyRecyclerAdapter mAdapter = new MyRecyclerAdapter(coupons2, getResources().getDimensionPixelSize(R.dimen.default_outer_padding),
                getResources().getDimensionPixelOffset(R.dimen.half_padding));
        mRecyclerView.setAdapter(mAdapter);
    }

    @OnClick(R.id.btn_add)
    public void addClicked() {
        Intent intent = new Intent(this, CouponActivity.class);
        startActivity(intent);
    }
}
