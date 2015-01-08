package com.jiacorp.couponkeeper;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jiacorp.couponkeeper.exceptions.DBException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;


public class CouponActivity extends ActionBarActivity {

    private static final String TAG = CouponActivity.class.getName();
    public static final String EXTRA_URI = "extra-uri";
    public static final String EXTRA_COUPON = "coupon";
    public static final String EXTRA_DB_ACTION = "db-action";

    private static final int SELECT_PICTURE = 3;

    private static final String TOKEN_DELIMITER = "kkk";
    private static final String SPACE_DELIMITER = "xxx";
    private static final String DATE_DELIMITER = "yyy";

    @InjectView(R.id.scroll_view)
    ScrollView mScrollView;

    @InjectView(R.id.et_company)
    EditText mCompany;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tv)
    TextView mTv;

    @InjectView(R.id.tv_date)
    TextView mTvDate;

    @InjectView(R.id.et_company)
    EditText mEtTitle;

    @InjectView(R.id.mark_as_used)
    Switch mMarkAsUsed;

    @InjectView(R.id.img_main)
    ImageView mImgMain;

    DateFormat mDateFormat;

    @InjectView(R.id.frame_layout)
    FrameLayout mFrameLayout;

    @InjectView(R.id.relative_layout)
    RelativeLayout mRelativeLayout;

    Uri mNewPhotoUri;
    Uri mSelectedImageUri;
    Uri mDisplayedUri;

    PhotoViewAttacher mAttacher;

    Coupon mCoupon;

    MyDBHandler mDbHandler;

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
                displayCapturedImage(mNewPhotoUri);
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
            getSupportActionBar().setTitle(coupon.title);
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.new_coupon));
            mCoupon = new Coupon();
            String date = mDateFormat.format(new Date());
            mTvDate.setText(date);
            mImgMain.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
            mDisplayedUri = null;
            mScrollView.setVisibility(View.VISIBLE);
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

        mImgMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //only show the chooser if currently not displaying any photos
                if (mDisplayedUri == null) {
                    showImagePicker();
                }
            }
        });

        mAttacher = new PhotoViewAttacher(mImgMain);
        mAttacher.update();
    }

    private void showImagePicker() {

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");


        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mNewPhotoUri = getOutputMediaFileUri();

        // specifying path to save image
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mNewPhotoUri);

        String pickTitle = "Select or take a new Picture";
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent });

        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (!isDeleted && checkCompletedFields()) {
            save();
        } else if (mDisplayedUri == null && TextUtils.isEmpty(mEtTitle.getText().toString())) {
            //if it doesn't have an image, and it doesn't have a title, then we can just let it go back.
        } else if (!isDeleted && !checkCompletedFields()) {
            showAlert();
            return;
        }

        super.onBackPressed();
    }

    private void showAlert() {

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(R.string.alert_changes_not_saved)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAfterCancel();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
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
        mImgMain.setColorFilter(getResources().getColor(R.color.transparent_green), PorterDuff.Mode.SRC_ATOP);
    }

    private void initializeWithCoupon() {
        mTvDate.setText(mCoupon.expDateString);
        mEtTitle.setText(mCoupon.title);

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

        //TODO: JIA: convert to picasso
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

                mDisplayedUri = Uri.parse("file:///" + mCoupon.filePath);
                mScrollView.setVisibility(View.VISIBLE);
            }
        });

        List<String> dates = Arrays.asList(mCoupon.expDateString.split("/"));

        mDefaultCalendar.set(Integer.parseInt(dates.get(2)), Integer.parseInt(dates.get(0)) - 1, Integer.parseInt(dates.get(1)));
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
        if (item.getItemId() == R.id.action_delete) {
            if (TextUtils.isEmpty(mCoupon.id)) {
                //this coupon haven't been saved yet. Just exit activity
                finish();
                return true;
            }

            CouponHandler.deleteCoupon(mCoupon, mDbHandler, this);
            isDeleted = true;
            finishAfterDelete();
        } else if (item.getItemId() == R.id.action_paper_clip) {
            showImagePicker();
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

        if (resultCode == RESULT_OK) {
            if (data == null) {
                displayCapturedImage(mNewPhotoUri);
                mSelectedImageUri = null;
            } else {
                //this is when the user attaches an image already saved on the phone.
                mSelectedImageUri = data.getData();
                if (mSelectedImageUri != null) {
                    displayCapturedImage(mSelectedImageUri);
                }

                mNewPhotoUri = null;
            }
        } else {
            mNewPhotoUri = null;
            mSelectedImageUri = null;
            Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.tv)
    public void tvClicked() {
        showCalendar();
    }

    private void scrollToTitle() {
        mEtTitle.requestFocus();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0, mTvDate.getBottom());
            }
        });
    }

    private void displayCapturedImage(Uri uri) {
        Log.d(TAG, "displaying photo for path: " + uri.getPath());
        Picasso.with(this)
                .load(uri)
                .fit()
                .centerInside()
                .into(mImgMain, new Callback() {
                    @Override
                    public void onSuccess() {
                        scrollToTitle();
                    }

                    @Override
                    public void onError() {

                    }
                });

        mDisplayedUri = uri;
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
        if (TextUtils.isEmpty(mEtTitle.getText().toString())) {
            return false;
        }

        if (TextUtils.isEmpty(mTvDate.getText().toString())) {
            return false;
        }

        if (mDisplayedUri == null) {    //there is no image being displayed
            return false;
        }
        return true;
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * This method gets the file into the correct directory, and return that path. This assumes
     * there is an image being displayed
     */
    private String finalizedFileAndGetPath() {
        if (mDisplayedUri.getPath().contains(getResources().getString(R.string.app_name))) {
            //this is already in the correct place.
            return mDisplayedUri.getPath();
        } else {
            //This is likely a photo that was attached. We need to move it to the correct place.
            Uri newUri = getOutputMediaFileUri();
            String fullPath = getPath(mDisplayedUri);
            Log.d(TAG, "Attachment is actually located:" + fullPath);
            File src = new File(fullPath);

            try {
                Util.copy(src, new File(newUri.getPath()));
            } catch (IOException e) {
                Log.e(TAG, "Cannot copy attachment file from " + src.getPath() + " to " + newUri.getPath());
                e.printStackTrace();
            }
            return newUri.getPath();
        }
    }

    private void finishAfterUpdate() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COUPON, mCoupon);
        intent.putExtra(EXTRA_DB_ACTION, DbAction.UPDATE);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void finishAfterAdd() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COUPON, mCoupon);
        intent.putExtra(EXTRA_DB_ACTION, DbAction.ADD);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void finishAfterDelete() {
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_DB_ACTION, DbAction.DELETE);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void finishAfterCancel() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void save() {
        Log.d(TAG, "title:" + mEtTitle.getText().toString());
        Log.d(TAG, "Expiration Date:" + mTvDate.getText().toString());

        String pathToSave = finalizedFileAndGetPath();
        mCoupon.title = mEtTitle.getText().toString().trim();
        mCoupon.expDateString = mTvDate.getText().toString();
        mCoupon.used = mMarkAsUsed.isChecked();

        if (mIsEditMode) {
            //if the new path is not the same as the path that was originally loaded,
            //then delete the old item, and remember the new path

            if (!mCoupon.filePath.equalsIgnoreCase(pathToSave)) {
                //If the new path is not the same as the existing path, then remember the old path,
                //so we can delete that image
                String oldPath = mCoupon.filePath;
                renameFileWithMetaData(mCoupon, pathToSave);
                File f = new File(oldPath);
                f.delete();
            }

            CouponHandler.updateCoupon(mCoupon, mDbHandler, this);
            finishAfterUpdate();

        } else {
            //rename the path to have proper convention, then save
            renameFileWithMetaData(mCoupon, pathToSave);
            try {
                CouponHandler.addCoupon(mCoupon, mDbHandler, this);
                finishAfterAdd();
            } catch (DBException e) {
                Toast.makeText(this, "Cannot save coupon", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot save coupon");
                e.printStackTrace();
            }
        }
    }

    /**
     * Formats a file name to contain meta data about the expiration date and the title
     * @param c The coupon, whose file name is going to change.
     * @param originalPath  the original path that will be renamed.
     */
    private void renameFileWithMetaData(Coupon c, String originalPath) {

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
