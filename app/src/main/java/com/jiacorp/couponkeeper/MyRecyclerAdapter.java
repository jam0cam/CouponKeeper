package com.jiacorp.couponkeeper;


import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.RippleDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
    private static final String TAG = MyRecyclerAdapter.class.getName();

    private List<Coupon> mCoupons;
    public int mFullPadding;
    public int mHalfPadding;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTvTitle;
        public TextView mTvExpiration;
        public ImageView mImageView;
        public CardView mCardView;

        public ViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            mTvTitle = (TextView) v.findViewById(R.id.tv_title);
            mTvExpiration = (TextView) v.findViewById(R.id.tv_expiration);
            mImageView = (ImageView) v.findViewById(R.id.iv_main);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyRecyclerAdapter(List<Coupon> coupons, int fullPadding, int halfPadding) {
        mCoupons = coupons;
        mFullPadding = fullPadding;
        mHalfPadding = halfPadding;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Coupon c = mCoupons.get(position);

        holder.mTvTitle.setText(c.title);
        holder.mTvExpiration.setText(c.expDateString);

        File imgFile = new  File(c.filePath);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.mImageView.setImageBitmap(myBitmap);

            Palette.generateAsync(myBitmap, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    int rippleColor = Integer.MIN_VALUE;

                    if (palette.getLightVibrantSwatch() != null) {
                        holder.mCardView.setBackgroundColor(palette.getLightVibrantSwatch().getRgb());
                        rippleColor = palette.getLightVibrantSwatch().getRgb();
                    } else if (palette.getLightMutedSwatch() != null) {
                        holder.mCardView.setBackgroundColor(palette.getLightMutedSwatch().getRgb());
                        rippleColor = palette.getLightMutedSwatch().getRgb();
                    } else if (palette.getVibrantSwatch() != null) {
                        holder.mCardView.setBackgroundColor(palette.getVibrantSwatch().getRgb());
                        rippleColor = palette.getVibrantSwatch().getRgb();
                    }

                    if (rippleColor > Integer.MIN_VALUE) {
                        RippleDrawable ripple = (RippleDrawable) holder.mCardView.getForeground();
                        ColorStateList myColorStateList = new ColorStateList(
                                new int[][]{
                                        new int[]{android.R.attr.state_pressed}
                                },
                                new int[] {
                                        rippleColor
                                }
                        );

                        ripple.setColor(myColorStateList);
                    }

                }
            });
        } else {
            Log.d(TAG, "Cannot load file: " + c.filePath);
        }


        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.mCardView.getLayoutParams();
        if (position == 0) {
            params.setMargins(mFullPadding, mFullPadding, mFullPadding, mHalfPadding);
        } else if (position == mCoupons.size() -1) {
            params.setMargins(mFullPadding, mHalfPadding, mFullPadding, mFullPadding);
        } else {
            params.setMargins(mFullPadding, mHalfPadding, mFullPadding, mHalfPadding);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCoupons == null ? 0 : mCoupons.size();
    }
}