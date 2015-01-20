package com.jiacorp.cwallet;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jiacorp.cwallet.exceptions.DBException;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jitse on 1/2/15.
 */
public class CouponHandler {

    private static final String TAG = CouponHandler.class.getName();

    public static void deleteCoupon(Coupon coupon, MyDBHandler dbHandler, Context context) {

        //delete the actual image saved on the SD card.
        File f = new File(coupon.filePath);
        boolean result = f.delete();
        if (!result) {
            Log.e(TAG, "Cannot delete file from storage: " + coupon.filePath);
        }

        //delete the coupon from the databasae
        if (!dbHandler.deleteCoupon(coupon.id)) {
            Log.d(TAG, "Cannot delete from from db: " + coupon.filePath);
        }

        //delete any notifications that it might have
        deleteAlarm(coupon, context);
    }

    public static void updateCoupon(Coupon coupon, MyDBHandler dbHandler, Context context) {
        dbHandler.updateCoupon(coupon);
        deleteAlarm(coupon, context);
        createAlarm(coupon, context);
    }

    public static void addCoupon(Coupon coupon, MyDBHandler dbHandler, Context context) throws DBException {
        dbHandler.addCoupon(coupon);
        createAlarm(coupon, context);
    }

    private static void deleteAlarm(Coupon coupon, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(coupon.id), intent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "delete alarm for coupon " + coupon.title);
    }

    /**
     * Creates an alarm for this coupon. The alarm date is 3 days before expiration date
     */
    private static void createAlarm(Coupon coupon, Context context) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        Date expiration;
        try {
            expiration = dateFormat.parse(coupon.expDateString);
        } catch (ParseException e) {
            Log.e(TAG, "problem parsing date: " + coupon.expDateString + ". Not creating alarm", e);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, expiration.getYear() + 1900);
        calendar.set(Calendar.MONTH, expiration.getMonth());
        calendar.set(Calendar.DATE, expiration.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.add(Calendar.DATE, -3);

        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
            Intent myIntent = new Intent(context, AlarmReceiver.class);
            myIntent.putExtra(CouponActivity.EXTRA_COUPON, coupon);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(coupon.id), myIntent, 0);

            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

            Log.d(TAG, "setting alarm for time: " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR) + " for coupon : " + coupon.title);
        } else {
            Log.d(TAG, "Not creating alarm because the alarm time has already passed");
        }
    }

}
