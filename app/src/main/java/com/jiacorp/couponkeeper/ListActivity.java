package com.jiacorp.couponkeeper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
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

    @InjectView(R.id.lv)
    ListView mListView;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.btn_add)
    ImageButton mBtnAdd;

    MyDBHandler mDbHandler;
    ListAdapter mAdapter;
    List<Coupon> mCoupons;
    ActionMode mActionMode;

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
        mListView.setAlpha(0);
        mListView.setVisibility(View.GONE);
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
            mActionMode.setTitle(mListView.getCheckedItemCount() + " Selected to mark as used.");
        } else if (item.getItemId() == R.id.action_delete) {
            mMode = Mode.DELETE;
            mActionMode = mToolbar.startActionMode(ListActivity.this);
            mActionMode.setTitle(mListView.getCheckedItemCount() + " Selected For delete");
        } else if (item.getItemId() == R.id.action_sort_exp_soonest) {
            mSort = Sort.EXP_DATE_ASC;
            fadeOutAndReloadCoupons(mListView);
        } else if (item.getItemId() == R.id.action_sort_coupon_name) {
            mSort = Sort.NAME_ASC;
            fadeOutAndReloadCoupons(mListView);
        }

        return super.onOptionsItemSelected(item);
    }



    @OnClick(R.id.btn_add)
    public void addClicked() {
        Intent intent = new Intent(this, CouponActivity.class);
        startActivity(intent);
    }

    private void reloadCoupons() {
        mCoupons = mDbHandler.getAllCoupons(mSort);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mAdapter = new ListAdapter(this, mCoupons);
        mListView.setAdapter(mAdapter);
        fadeIn(mListView);
    }

    private void deleteCheckedItems() {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems != null) {
            for (int i=0; i<checkedItems.size(); i++) {
                if (checkedItems.valueAt(i)) {
                    Coupon c = mCoupons.get(checkedItems.keyAt(i));
                    Log.d(TAG, "deleting " + c.title + " file");
                    mDbHandler.deleteCoupon(c.id);

                    File f = new File(c.filePath);
                    f.delete();
                }
            }

            fadeOutAndReloadCoupons(mListView);
        }
    }

    private void markCheckedItems() {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems != null) {
            for (int i=0; i<checkedItems.size(); i++) {
                if (checkedItems.valueAt(i)) {
                    Coupon c = mCoupons.get(checkedItems.keyAt(i));
                    c.used = true;
                    Log.d(TAG, "Marking " + c.title + " as used.");
                    mDbHandler.updateCoupon(c);
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if (mActionMode != null) {
            v.setSelected(true);

            Coupon c = (Coupon) v.getTag();
            int selectedItem = mCoupons.indexOf(c);
            boolean toVal = !mListView.isItemChecked(selectedItem);
            mListView.setItemChecked(selectedItem, toVal);
            Log.d(TAG, "Setting check to " + toVal + "  for index:" + selectedItem + "  There are " + mListView.getCheckedItemCount() + " checked items");
            mActionMode.setTitle(mListView.getCheckedItemCount() + " Selected");
        } else {
            Coupon c = (Coupon) v.getTag();
            Intent intent = new Intent(this, CouponActivity.class);
            intent.putExtra(CouponActivity.EXTRA_COUPON, c);
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick");
        Coupon c = (Coupon) v.getTag();
        int selectedItem = mCoupons.indexOf(c);

        if (mActionMode == null) {
            // Start the CAB using the ActionMode.Callback defined above
            mMode = Mode.BOTH;
            mActionMode = mToolbar.startActionMode(ListActivity.this);
            Log.d(TAG, "started action mode");
        }

        v.setSelected(true);

        boolean toVal = !mListView.isItemChecked(selectedItem);
        mListView.setItemChecked(selectedItem, toVal);
        Log.d(TAG, "Setting check to " + toVal + "  for index:" + selectedItem + "  There are " + mListView.getCheckedItemCount() + " checked items");
        mActionMode.setTitle(mListView.getCheckedItemCount() + " Selected");

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
        mListView.clearChoices();
        mAdapter.notifyDataSetChanged();
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
