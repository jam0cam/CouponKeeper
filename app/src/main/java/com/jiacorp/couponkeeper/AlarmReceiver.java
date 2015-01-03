package com.jiacorp.couponkeeper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jitse on 1/2/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getName();

    private NotificationManager mManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        Coupon coupon = (Coupon) intent.getSerializableExtra(CouponActivity.EXTRA_COUPON);
        mManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, CouponActivity.class);
        intent1.putExtra(CouponActivity.EXTRA_COUPON, coupon);

        Notification notification = new Notification(R.drawable.ic_launcher,
                context.getString(R.string.coupon_expiring_alert, coupon.title),
                System.currentTimeMillis());

        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity( context,0, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(context, context.getString(R.string.app_name),
                context.getString(R.string.coupon_expiring_alert, coupon.title),
                pendingNotificationIntent);

        mManager.notify(0, notification);
    }
}
