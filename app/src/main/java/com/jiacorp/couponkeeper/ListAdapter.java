package com.jiacorp.couponkeeper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by jitse on 12/21/14.
 */
public class ListAdapter extends ArrayAdapter<Coupon> implements View.OnClickListener,
        View.OnLongClickListener {

    private static final String TAG = ListAdapter.class.getName();
    private List<Coupon> mCoupons;
    private Context mContext;
    private View.OnClickListener mClickListener;
    private View.OnLongClickListener mLongClickListener;

    public ListAdapter(Context context, List<Coupon> objects) {
        super(context, R.layout.list_item, objects);

        Log.d(TAG, "Creating new instance of ListAdapter");
        mClickListener = (View.OnClickListener)context;
        mLongClickListener = (View.OnLongClickListener)context;
        mCoupons = objects;
        mContext = context;
    }

    //TODO: JIA: learn to use the view holder pattern
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        final ImageView imageView = (ImageView) rowView.findViewById(R.id.iv_main);

        CardView cardView = (CardView) rowView.findViewById(R.id.card_view);

        cardView.setTag(mCoupons.get(position));
        cardView.setOnClickListener(this);
//        cardView.setOnLongClickListener(this);

        TextView title = (TextView) rowView.findViewById(R.id.tv_title);
        TextView expiration = (TextView) rowView.findViewById(R.id.tv_expiration);

        final Coupon c = mCoupons.get(position);

        File imgFile = new  File(c.filePath);

        if(imgFile.exists()){
            Picasso.with(mContext)
                    .load(new File(c.filePath))
                    .fit()
                    .centerCrop()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (c.used) {
                                imageView.setColorFilter(mContext.getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            Log.d(TAG, "Cannot load file: " + c.filePath);
        }

        title.setText(c.title);
        expiration.setText(c.expDateString);

        ListView list = (ListView) parent;
        if (list.getCheckedItemPositions() != null && list.isItemChecked(position)) {
            Log.d(TAG, "highlighting at position:" + position);
            highlightItem(cardView);
        } else {
            Log.d(TAG, "CLEARING at position:" + position);
            unHighlightItem(cardView);
        }

        return rowView;
    }

    private void unHighlightItem(View v) {
        v.findViewById(R.id.selection_tint).setVisibility(View.GONE);
    }

    private void highlightItem(View v) {
        v.findViewById(R.id.selection_tint).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
//        ListView list = (ListView) v.getParent();
//        int position = mCoupons.indexOf(v.getTag());
//        if (list.getCheckedItemPositions() != null && list.getCheckedItemPositions().get(position)) {
//            highlightItem(v);
//        }
        mClickListener.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
//        ListView list = (ListView) v.getParent();
//        int position = mCoupons.indexOf(v.getTag());
//        if (list.getCheckedItemPositions() != null && list.getCheckedItemPositions().get(position)) {
//            highlightItem(v);
//        }
        return mLongClickListener.onLongClick(v);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
}

