package com.jiacorp.couponkeeper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class ListActivity extends ActionBarActivity implements
        View.OnClickListener,
        View.OnLongClickListener, android.view.ActionMode.Callback {

    private static final String TAG = ListActivity.class.getName();

    enum Mode {
        CHECK, DELETE, BOTH
    }

    enum Sort {
        EXP_DATE_ASC, NAME_ASC
    }

    Mode mMode;
    Sort mSort;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @InjectView(R.id.btn_add)
    ImageButton mBtnAdd;

    MyDBHandler mDbHandler;
    MyRecyclerAdapter mAdapter;
    List<Coupon> mCoupons;
    ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mDbHandler = ((CouponApplication)getApplication()).getDbHandler();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.setAlpha(0);
        mRecyclerView.setVisibility(View.GONE);
        reloadCoupons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_check) {
            mMode = Mode.CHECK;
            mActionMode = mToolbar.startActionMode(ListActivity.this);
            mActionMode.setTitle(mAdapter.getItemCount() + " Selected to mark as used.");
        } else if (item.getItemId() == R.id.action_delete) {
            mMode = Mode.DELETE;
            mActionMode = mToolbar.startActionMode(ListActivity.this);
            mActionMode.setTitle(mAdapter.getItemCount() + " Selected For delete");
        } else if (item.getItemId() == R.id.action_sort_exp_soonest) {
            mSort = Sort.EXP_DATE_ASC;
            fadeOutAndReloadCoupons(mRecyclerView);
        } else if (item.getItemId() == R.id.action_sort_coupon_name) {
            mSort = Sort.NAME_ASC;
            fadeOutAndReloadCoupons(mRecyclerView);
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_add)
    public void addClicked() {
        Intent intent = new Intent(this, CouponActivity.class);
        startActivity(intent);
    }

    private void myToggleSelection(int idx) {
        mAdapter.toggleSelection(idx);
        String title = mAdapter.getSelectedItemCount() + " Selected.";
        mActionMode.setTitle(title);
    }

    private void reloadCoupons() {
        mCoupons = mDbHandler.getAllCoupons(mSort);
        mAdapter = new MyRecyclerAdapter(mCoupons, this);
        mRecyclerView.setAdapter(mAdapter);

        fadeIn(mRecyclerView);
    }

    private void deleteCheckedItems() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            Coupon c = mCoupons.get(selectedItemPositions.get(i));
            mAdapter.removeData(selectedItemPositions.get(i));
            mDbHandler.deleteCoupon(c.id);
        }
    }

    private void markCheckedItems() {
        List<Integer> selectedItems = mAdapter.getSelectedItems();

        for (int i : selectedItems) {
            Coupon c = mCoupons.get(i);
            c.used = true;
            Log.d(TAG, "Marking " + c.title + " as used.");
            mDbHandler.updateCoupon(c);
        }
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if (mActionMode != null) {
            int idx = mRecyclerView.getChildPosition(v);
            myToggleSelection(idx);
        } else {
            Coupon c = (Coupon) v.getTag();
            Intent intent = new Intent(this, CouponActivity.class);
            intent.putExtra(CouponActivity.EXTRA_COUPON, c);
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mActionMode != null) {
            return false;
        }

        mMode = Mode.BOTH;

        mActionMode = mToolbar.startActionMode(ListActivity.this);
        int idx = mRecyclerView.getChildPosition(v);
        myToggleSelection(idx);
        return true;
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        // Assumes that you have "contexual.xml" menu resources

        if (mMode == Mode.BOTH) {
            inflater.inflate(R.menu.menu_action_both, menu);
        } else if (mMode == Mode.CHECK) {
            inflater.inflate(R.menu.menu_action_check, menu);
        } else if (mMode == Mode.DELETE) {
            inflater.inflate(R.menu.menu_action_delete, menu);
        } else {
            throw new UnsupportedOperationException("Unsupported method");
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deleteCheckedItems();
        } else if (item.getItemId() == R.id.action_check) {
            markCheckedItems();
        }

        mActionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
        Log.d(TAG, "Clearing all the choices");

        mActionMode = null;
        mAdapter.clearSelections();
    }

    private void fadeIn(final View v) {
        Log.d(TAG, "Fading in,  visibility: " + v.getVisibility());
        v.animate().alpha(1).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
                Log.d(TAG, "Done Fading in");
            }
        }).start();
    }

    private void fadeOutAndReloadCoupons(final View v) {
        Log.d(TAG, "Fading out");
        v.animate().alpha(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                Log.d(TAG, "Done Fading out");
                reloadCoupons();
            }
        }).start();
    }
}
