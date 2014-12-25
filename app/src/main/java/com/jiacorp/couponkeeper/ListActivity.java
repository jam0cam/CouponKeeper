package com.jiacorp.couponkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class ListActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = ListActivity.class.getName();

    @InjectView(R.id.lv)
    ListView mListView;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.btn_add)
    ImageButton mBtnAdd;

    MyDBHander mDbHandler;
    ListAdapter mAdapter;
    List<Coupon> mCoupons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mDbHandler = ((CouponApplication)getApplication()).getDbHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCoupons = mDbHandler.getAllCoupons();
        mAdapter = new ListAdapter(this, mCoupons);
        mListView.setAdapter(mAdapter);
    }

    @OnClick(R.id.btn_add)
    public void addClicked() {
        Intent intent = new Intent(this, CouponActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Coupon c = (Coupon) v.getTag();
        Intent intent = new Intent(this, CouponActivity.class);
        intent.putExtra(CouponActivity.EXTRA_COUPON, c);
        startActivity(intent);
    }
}
