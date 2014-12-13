package com.jiacorp.couponkeeper;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiacorp.couponkeeper.exceptions.DBException;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class CouponActivity extends ActionBarActivity {

    private static final int PICK_FROM_GALLERY = 1;
    private static final int CAMERA = 2;
    private static final String TOKEN_DELIMITER = "kkk";
    private static final String SPACE_DELIMITER = "xxx";
    private static final String DATE_DELIMITER = "yyy";

    private static final String TAG = CouponActivity.class.getName();
    @InjectView(R.id.et_company)
    EditText mCompany;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tv_date)
    TextView mTvDate;

    @InjectView(R.id.et_company)
    EditText mEtCompany;

    @InjectView(R.id.image)
    ImageView mImage;

    DateFormat mDateFormat;

    @InjectView(R.id.btn_save)
    Button mBtnSave;

    boolean mImageSelected = false;

    Uri mNewPhotoUri;
    Uri mSelectedImageUri;

    MyDBHander mDbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mDateFormat = android.text.format.DateFormat.getDateFormat(this);
        String date = mDateFormat.format(new Date());
        mTvDate.setText(date);

        mDbHandler = ((CouponApplication)getApplication()).getDbHandler();
        mEtCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkCompletedFields();
            }
        });

        mTvDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showCalendar();
                    return true;
                }

                return false;
            }
        });
    }

    private void showCalendar() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        System.out.println("the selected " + mDay);
        DatePickerDialog dialog = new DatePickerDialog(CouponActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mTvDate.setText(mDateFormat.format(new Date(year-1900, monthOfYear, dayOfMonth)));
                        checkCompletedFields();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_GALLERY) {

            mSelectedImageUri = data.getData();
            if (mSelectedImageUri != null) {
                mImage.setImageURI(mSelectedImageUri);
                mImageSelected = true;
                checkCompletedFields();
            }
        } else if (requestCode == CAMERA) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // display it in image view

                displayCapturedImage();
                checkCompletedFields();

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

        try {

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger images
            options.inSampleSize = 8;

            Log.d(TAG, "photo saved in path: " + mNewPhotoUri.getPath());
            final Bitmap bitmap = BitmapFactory.decodeFile(mNewPhotoUri.getPath(),options);
            mImage.setImageBitmap(bitmap);
        }

        catch (NullPointerException e) {

            e.printStackTrace();

        }

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
    private void checkCompletedFields() {
        if (TextUtils.isEmpty(mEtCompany.getText().toString())) {
            mBtnSave.setEnabled(false);
            return;
        }

        if (TextUtils.isEmpty(mTvDate.getText().toString())) {
            mBtnSave.setEnabled(false);
            return;
        }

        if (!mImageSelected && mNewPhotoUri == null) {
            mBtnSave.setEnabled(false);
            return;
        }

        mBtnSave.setEnabled(true);
    }

    @OnClick(R.id.btn_save)
    public void onClicked() {
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

        //rename the path to have proper convention, then save
        Coupon c = new Coupon(mEtCompany.getText().toString(), mTvDate.getText().toString(), path);

        //rename the path to have proper convention, then save
        renameFile(c, path);

        try {
            mDbHandler.addCoupon(c);
            finish();
        } catch (DBException e) {
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

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
