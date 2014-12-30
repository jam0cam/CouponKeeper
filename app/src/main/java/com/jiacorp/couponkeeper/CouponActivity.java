package com.jiacorp.couponkeeper;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jiacorp.couponkeeper.exceptions.DBException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class CouponActivity extends ActionBarActivity {

    private static final String TAG = CouponActivity.class.getName();
    private static final String EXTRA_URI = "extra-uri";
    public static final String EXTRA_COUPON = "coupon";

    private static final int PICK_FROM_GALLERY = 1;
    private static final int CAMERA = 2;
    private static final String TOKEN_DELIMITER = "kkk";
    private static final String SPACE_DELIMITER = "xxx";
    private static final String DATE_DELIMITER = "yyy";

    @InjectView(R.id.et_company)
    EditText mCompany;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tv_date)
    TextView mTvDate;

    @InjectView(R.id.et_company)
    EditText mEtCompany;

    @InjectView(R.id.mark_as_used)
    Switch mMarkAsUsed;

    @InjectView(R.id.img_main)
    ImageView mImgMain;

    DateFormat mDateFormat;

    @InjectView(R.id.frame_layout)
    FrameLayout mFrameLayout;

    @InjectView(R.id.relative_layout)
    RelativeLayout mRelativeLayout;

    boolean mImageSelected = false;

    Uri mNewPhotoUri;
    Uri mSelectedImageUri;

    Coupon mCoupon;

    MyDBHander mDbHandler;

    Calendar mDefaultCalendar;
    boolean mIsEditMode = false;
    private ImageLoader mImageLoader;
    boolean isDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_coupon);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mImageLoader = ImageLoader.getInstance();
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);
        Coupon coupon = (Coupon) getIntent().getSerializableExtra(EXTRA_COUPON);
        mDefaultCalendar = Calendar.getInstance();

        if (savedInstanceState != null) {
            mNewPhotoUri = savedInstanceState.getParcelable(EXTRA_URI);
            if (mNewPhotoUri != null) {
                Log.d(TAG, "Reloading uri: " + mNewPhotoUri.getPath());
                displayCapturedImage();
            }
        }

        if (coupon != null) {
            mCoupon = coupon;
            initializeWithCoupon();
            mIsEditMode = true;
            mMarkAsUsed.setChecked(mCoupon.used);

            if (mCoupon.used) {
                showOverlay();
            }

        } else {
            String date = mDateFormat.format(new Date());
            mTvDate.setText(date);
            mImgMain.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
        }

        mDbHandler = ((CouponApplication)getApplication()).getDbHandler();
        mTvDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showCalendar();
                    return true;
                }

                return false;
            }
        });

        mMarkAsUsed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showOverlay();
                } else {
                    hideOverlay();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();


        if (!isDeleted && checkCompletedFields()) {
            save();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void hideOverlay() {
        mImgMain.setColorFilter(null);
    }

    private void showOverlay() {
        mImgMain.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
    }

    private void initializeWithCoupon() {
        mTvDate.setText(mCoupon.expDateString);
        mEtCompany.setText(mCoupon.title);

        Matrix matrix = new Matrix();

        try {
            ExifInterface exif = new ExifInterface(mCoupon.filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.d(TAG, "orientation value: " + orientation);

            if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            } else {
                matrix = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        final Matrix finalMatrix = matrix;

        mImageLoader.displayImage("file:///" + mCoupon.filePath, mImgMain, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                super.onLoadingComplete(imageUri, view, bitmap);

                if (finalMatrix != null) {
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), finalMatrix, true);
                    mImgMain.setImageBitmap(rotatedBitmap);
                } else {
                    mImgMain.setImageBitmap(bitmap);
                }
            }
        });

        List<String> dates = Arrays.asList(mCoupon.expDateString.split("/"));

        mDefaultCalendar.set(Integer.parseInt(dates.get(2)), Integer.parseInt(dates.get(0))-1, Integer.parseInt(dates.get(1)));
    }

    private void showCalendar() {
        int mYear = mDefaultCalendar.get(Calendar.YEAR);
        int mMonth = mDefaultCalendar.get(Calendar.MONTH);
        int mDay = mDefaultCalendar.get(Calendar.DAY_OF_MONTH);


        System.out.println("the selected " + mDay);
        DatePickerDialog dialog = new DatePickerDialog(CouponActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mTvDate.setText(mDateFormat.format(new Date(year-1900, monthOfYear, dayOfMonth)));
                    }
                }, mYear, mMonth, mDay);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coupon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if (item.getItemId() == R.id.action_attach) {
//            //TODO: JIA: enable this option by adding it in the menu
//            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//            photoPickerIntent.setType("image/*");
//            startActivityForResult(photoPickerIntent, PICK_FROM_GALLERY);
//        } else

        if (item.getItemId() == R.id.action_camera) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // create a file to save image
            String imageFileName= UUID.randomUUID().toString() + ".jpg";
            ContentValues contentValues=new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, imageFileName);
            mNewPhotoUri = getOutputMediaFileUri();

            // specifying path to save image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mNewPhotoUri);

            // starting the image capture Intent
            startActivityForResult(intent, CAMERA);
        } else if (item.getItemId() == R.id.action_delete) {
            if (mCoupon == null || TextUtils.isEmpty(mCoupon.id)) {
                //this coupon haven't been saved yet. Just exit activity
                finish();
                return true;
            }

            //delete the actual image saved on the SD card.
            File f = new File(mCoupon.filePath);
            boolean result = f.delete();
            if (!result) {
                Log.d(TAG, getString(R.string.delete_file_error));
                Toast.makeText(this, getResources().getString(R.string.delete_file_error), Toast.LENGTH_SHORT).show();
            }

            if (!mDbHandler.deleteCoupon(mCoupon.id)) {
                Log.d(TAG, getString(R.string.delete_db_error));
                Toast.makeText(this, getResources().getString(R.string.delete_db_error), Toast.LENGTH_SHORT).show();
            }

            isDeleted = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mNewPhotoUri != null) {
            Log.d(TAG, "Saving uri: " + mNewPhotoUri.getPath());
            outState.putParcelable(EXTRA_URI, mNewPhotoUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_GALLERY) {

            mSelectedImageUri = data.getData();
            if (mSelectedImageUri != null) {
                mImgMain.setImageURI(mSelectedImageUri);
                mImageSelected = true;
            }
        } else if (requestCode == CAMERA) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // display it in image view

                displayCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {

                mNewPhotoUri = null;
                Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();

            } else {
                mNewPhotoUri = null;
                // failed to capture image
                Toast.makeText(getApplicationContext(),"Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void displayCapturedImage() {
//        try {
//
//            // bimatp factory
//            BitmapFactory.Options options = new BitmapFactory.Options();
//
//            // downsizing image as it throws OutOfMemory Exception for larger images
//            options.inSampleSize = 8;
//
//            Log.d(TAG, "displaying photo for path: " + mNewPhotoUri.getPath());
//            final Bitmap bitmap = BitmapFactory.decodeFile(mNewPhotoUri.getPath(),options);
//            mImgMain.setImageBitmap(bitmap);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, "displaying photo for path: " + mNewPhotoUri.getPath());
        Picasso.with(this)
                .load(mNewPhotoUri)
                .fit()
                .centerInside()
                .into(mImgMain);
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +  "IMG_"+ timeStamp + ".jpg");
        return mediaFile;
    }

    /**
     * If all fields are filled out, then we will enable the save button. Otherwise, disable it.
     */
    private boolean checkCompletedFields() {
        if (TextUtils.isEmpty(mEtCompany.getText().toString())) {
            return false;
        }

        if (TextUtils.isEmpty(mTvDate.getText().toString())) {
            return false;
        }

        if (!mImageSelected && mNewPhotoUri == null && !mIsEditMode) {
            return false;
        }

        return true;
    }

    public void save() {
        Log.d(TAG, "title:" + mEtCompany.getText().toString());
        Log.d(TAG, "Expiration Date:" + mTvDate.getText().toString());

        String path = "";
        if (mNewPhotoUri != null) {
            path = mNewPhotoUri.getPath();
            Log.d(TAG, "PATH:" + mNewPhotoUri.getPath());
        } else if (mSelectedImageUri != null) {
            path = mSelectedImageUri.getPath();
            Log.d(TAG, "PATH:" + mSelectedImageUri.getPath());
        }

        if (mIsEditMode) {
            String oldPath = null;

            mCoupon.title = mEtCompany.getText().toString();
            mCoupon.expDateString = mTvDate.getText().toString();
            mCoupon.used = mMarkAsUsed.isChecked();

            if (!TextUtils.isEmpty(path)) { //this means a new photo was taken
                //remember this old path, so we can delete that image
                oldPath = mCoupon.filePath;
                //should do this after title and expDateString has been set.
                renameFile(mCoupon, path);
            }

            mDbHandler.updateCoupon(mCoupon);

            if (oldPath != null) {
                //there was a new image taken, delete the old one
                File f = new File(oldPath);
                f.delete();
            }

            finish();
        } else {
            //rename the path to have proper convention, then save
            mCoupon = new Coupon(mEtCompany.getText().toString(), mTvDate.getText().toString(), path, mMarkAsUsed.isChecked());
            //rename the path to have proper convention, then save
            renameFile(mCoupon, path);

            try {
                mDbHandler.addCoupon(mCoupon);
                finish();
            } catch (DBException e) {
                Toast.makeText(this, "Cannot save coupon", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }

    /**
     * Formats a file name to contain meta data about the expiration date and the title
     * @param c The coupon, whose file name is going to change.
     * @param originalPath  the original path that will be renamed.
     */
    private void renameFile(Coupon c, String originalPath) {

        File file = new File(originalPath);

        int idx = originalPath.lastIndexOf("/");
        String prefix = originalPath.substring(0, idx + 1);

        String concatenatedTitle = c.title.replace(" " , SPACE_DELIMITER);
        String formattedDate = c.expDateString.replace("/", DATE_DELIMITER);
        String currentTimeStamp = String.valueOf(System.currentTimeMillis());
        String newFilePath = prefix + concatenatedTitle + TOKEN_DELIMITER + currentTimeStamp + TOKEN_DELIMITER + formattedDate + ".jpg";

        Log.d(TAG, "changing file name from: " + originalPath + " to: " + newFilePath);
        File newFile = new File(newFilePath);

        if (!newFile.exists()) {
            file.renameTo(newFile);
            c.filePath = newFilePath;
            Log.d(TAG, "File Changed to: " + newFilePath);
        } else {
            Log.d(TAG, "Unable to create file. " + newFilePath + " already exists");
            Toast.makeText(this, "Unable to create file. " + newFilePath + " already exists", Toast.LENGTH_LONG).show();
        }
    }
}
