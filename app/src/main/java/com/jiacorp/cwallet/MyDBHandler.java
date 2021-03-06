package com.jiacorp.cwallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jiacorp.cwallet.exceptions.DBException;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jitse on 11/23/14.
 */
public class MyDBHandler extends SQLiteOpenHelper {
    private static final String TAG = MyDBHandler.class.getName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "jiacorp.coupon";
    public static final String TABLE_COUPONS = "coupon";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_EXP_DATE = "exp_date";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_IS_USED = "is_used";
    DateFormat mDateFormat;

    private String[] allColumns = {
            COLUMN_ID,
            COLUMN_TITLE,
            COLUMN_EXP_DATE,
            COLUMN_PATH,
            COLUMN_IS_USED
    };

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating DB");
        String CREATE_COUPONS_TABLE = "CREATE TABLE " +
                TABLE_COUPONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_EXP_DATE + " INTEGER, "
                + COLUMN_PATH + " TEXT, "
                + COLUMN_IS_USED + " INTEGER"
                + ")";
        db.execSQL(CREATE_COUPONS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "upgrading DB");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUPONS);
        onCreate(db);
    }

    public void updateCoupon(Coupon coupon) {
        Log.d(TAG, "update coupon " + coupon.title + "  path:" + coupon.filePath);
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, coupon.title);

        long date = 0;
        try {
             date = mDateFormat.parse(coupon.expDateString).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "unable to parse date: " + coupon.expDateString);
        }

        values.put(COLUMN_EXP_DATE, date);
        values.put(COLUMN_PATH, coupon.filePath);
        values.put(COLUMN_ID, coupon.id);

        if (coupon.used) {
            values.put(COLUMN_IS_USED, 1);
        } else {
            values.put(COLUMN_IS_USED, 0);
        }

        SQLiteDatabase db = this.getWritableDatabase();

        int rowsAffected = db.update(TABLE_COUPONS, values, COLUMN_ID +  "=" + coupon.id, null);
        db.close();

        if (rowsAffected == 0) {
            Log.d(TAG, "Issue saving coupon, probably wasn't able to save");
        } else if (rowsAffected > 1 || rowsAffected < 0) {
            Log.d(TAG, "Something went wrong with updating the coupon. " + rowsAffected + " was affected");
        } else {
            Log.d(TAG, "Successfully updated coupon");
        }

    }

    public void addCoupon(Coupon coupon) throws DBException {
        Log.d(TAG, "adding coupon " + coupon.title + "  path:" + coupon.filePath);
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, coupon.title);


        long date = 0;
        try {
            date = mDateFormat.parse(coupon.expDateString).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "unable to parse date: " + coupon.expDateString);
        }


        values.put(COLUMN_EXP_DATE, date);
        values.put(COLUMN_PATH, coupon.filePath);

        if (coupon.used) {
            values.put(COLUMN_IS_USED, 1);
        } else {
            values.put(COLUMN_IS_USED, 0);
        }

        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.insert(TABLE_COUPONS, null, values);
        db.close();

        if (result == -1) {
            Log.d(TAG, "Issue adding coupon");
            throw new DBException("There was an issue inserting the record.");
        } else {
            coupon.id = String.valueOf(result);
        }
    }

    public List<Coupon> getAllCoupons(ListActivity.Sort sort) {
        Log.d(TAG, "getting coupon");
        SQLiteDatabase db = this.getWritableDatabase();

        String orderBy = null;

        if (sort != null) {
            if (sort == ListActivity.Sort.EXP_DATE_ASC) {
                Log.d(TAG, "Sorting by date");
                orderBy = COLUMN_EXP_DATE + " ASC";
            } else if (sort == ListActivity.Sort.NAME_ASC) {
                orderBy = "lower(" + COLUMN_TITLE + ")  ASC";
            }
        }

        Cursor cursor = db.query(TABLE_COUPONS,
                allColumns, null, null, null, null, orderBy);


        List<Coupon> coupons = new ArrayList<Coupon>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Coupon coupon = cursorToCoupon(cursor);
            coupons.add(coupon);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        Log.d(TAG, "There are " + coupons.size() + " coupons");
        return coupons;
    }

    private Coupon cursorToCoupon(Cursor cursor) {
        Coupon coupon = new Coupon();

        coupon.id = cursor.getString(0);
        coupon.title =cursor.getString(1);
        long date = Long.parseLong(cursor.getString(2));
        coupon.expDateString = mDateFormat.format(new Date(date));
        coupon.filePath =cursor.getString(3);

        int isUsed = cursor.getInt(4);
        if (isUsed == 1) {
            coupon.used = true;
        } else {
            coupon.used = false;
        }

        return coupon;
    }

    public Coupon findCoupon(String path) {
        String query = "Select * FROM " + TABLE_COUPONS + " WHERE " + COLUMN_PATH + " =  \"" + path + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Coupon coupon;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            coupon = cursorToCoupon(cursor);
            cursor.close();
        } else {
            coupon = null;
        }
        db.close();
        return coupon;
    }

    public boolean deleteCoupon(String id) {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_COUPONS, COLUMN_ID + " = ?", new String[] { id });

        if (rowsDeleted == 1) {
            result = true;
        } else {
            Log.d(TAG, rowsDeleted + " were deleted when trying to delete coupon id: " + id);
        }

        db.close();
        return result;
    }
}
