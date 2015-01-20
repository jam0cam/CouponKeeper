package com.jiacorp.cwallet;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

/**
 * Created by jitse on 1/2/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        final Coupon coupon = (Coupon) intent.getSerializableExtra(CouponActivity.EXTRA_COUPON);
        Log.d(TAG, "AlarmReceiver received an alarm for " + coupon.title);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "Bitmap loaded");
                activateNotification(context, coupon, bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "Failed loading bitmap");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d(TAG, "Preparing loading bitmap");
            }
        };

        Log.d(TAG, "loading file: " + coupon.filePath);
        Picasso.with(context)
                .load(new File(coupon.filePath))
                .resize(100, 100)
                .into(target);
    }

    private void activateNotification(Context context, Coupon coupon, Bitmap bitmap) {
        Log.d(TAG, "Displaying nofitication for coupon: " + coupon.title);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification)
                        .setColor(context.getResources().getColor(R.color.orange))
                        .setLargeIcon(bitmap)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.coupon_expiring_alert, coupon.title));

        Intent intent1 = new Intent(context, CouponActivity.class);
        intent1.putExtra(CouponActivity.EXTRA_COUPON, coupon);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr = (NotificationManager)context.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(0, mBuilder.build());
    }
}
