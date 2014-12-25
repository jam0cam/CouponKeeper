package com.jiacorp.couponkeeper;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by jitse on 12/21/14.
 */
public class ListAdapter extends ArrayAdapter<Coupon> {

    private static final String TAG = ListAdapter.class.getName();
    private List<Coupon> mCoupons;
    private Context mContext;
    private View.OnClickListener mListener;

    public ListAdapter(Context context, List<Coupon> objects) {
        super(context, R.layout.list_item, objects);

        mListener = (View.OnClickListener)context;
        mCoupons = objects;
        mContext = context;
    }

    //TODO: JIA: learn to use the view holder pattern
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.iv_main);
        CardView cardView = (CardView) rowView.findViewById(R.id.card_view);

        cardView.setTag(mCoupons.get(position));
        cardView.setOnClickListener(mListener);

        TextView title = (TextView) rowView.findViewById(R.id.tv_title);
        TextView expiration = (TextView) rowView.findViewById(R.id.tv_expiration);

        Coupon c = mCoupons.get(position);


        File imgFile = new  File(c.filePath);

        if(imgFile.exists()){
            Log.d(TAG, "Loading index: " + position + ", file: " + c.filePath);
            Picasso.with(mContext)
                    .load(new File(c.filePath))
                    .fit()
                    .centerCrop()
                    .into(imageView);
        } else {
            Log.d(TAG, "Cannot load file: " + c.filePath);
        }

        title.setText(c.title);
        expiration.setText(c.expDateString);

        return rowView;
    }
}

