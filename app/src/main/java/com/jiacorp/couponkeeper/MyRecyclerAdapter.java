package com.jiacorp.couponkeeper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private static final String TAG = MyRecyclerAdapter.class.getName();
    private List<Coupon> items;
    private SparseBooleanArray selectedItems;

    private View.OnLongClickListener mLongClickListener;
    private View.OnClickListener mClickListener;


    public MyRecyclerAdapter(List<Coupon> items, Context context) {
        this.items = items;
        this.mLongClickListener = (View.OnLongClickListener) context;
        this.mClickListener = (View.OnClickListener) context;
        selectedItems = new SparseBooleanArray();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, int position) {
        final Coupon item = items.get(position);
        holder.mTvTitle.setText(item.title);
        holder.mTvExpiration.setText(item.expDateString);
        holder.mImageView.setImageBitmap(null);
        holder.mCardView.setOnLongClickListener(mLongClickListener);
        holder.mCardView.setOnClickListener(mClickListener);

        Picasso.with(holder.mImageView.getContext()).cancelRequest(holder.mImageView);

        Log.d(TAG, "Loading file: " + item.filePath);
        Picasso.with(holder.mImageView.getContext())
                .load(new File(item.filePath))
                .resize(200, 200)
                .centerCrop()
                .into(holder.mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (item.used) {
                            holder.mImageView.setColorFilter(holder.mImageView.getContext().getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        holder.itemView.setTag(item);

        if (selectedItems.get(position, false)) {
            holder.mSelectionTint.setVisibility(View.VISIBLE);
        } else {
            holder.mSelectionTint.setVisibility(View.GONE);
        }

    }

    @Override public int getItemCount() {
        return items.size();
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Removes the item that currently is at the passed in position from the
     * underlying data set.
     *
     * @param position The index of the item to remove.
     */
    public void removeData(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTvTitle;
        public TextView mTvExpiration;
        public CardView mCardView;
        public View mSelectionTint;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView;
            mImageView = (ImageView) itemView.findViewById(R.id.iv_main);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTvExpiration = (TextView) itemView.findViewById(R.id.tv_expiration);
            mSelectionTint = itemView.findViewById(R.id.selection_tint);
        }
    }
}