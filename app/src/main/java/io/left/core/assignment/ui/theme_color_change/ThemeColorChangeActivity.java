package io.left.core.assignment.ui.theme_color_change;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.left.core.assignment.data.helper.UtilityForProfileImage;
import io.left.core.assignment.ui.base.BaseActivity;
import io.left.core.util.R;
import io.left.core.util.helper.GetColorUtil;


public class ThemeColorChangeActivity extends BaseActivity<ThemeColorChangeMvpView, ThemeColorChangePresenter> implements ThemeColorChangeMvpView, View.OnClickListener {

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    @BindView(R.id.imageview_user)
    CircleImageView imageviewUser;
    Bitmap bm;

    @BindView(R.id.relative_layout)
    RelativeLayout relativeLayout;
    String userChoosenTask;
    Bitmap thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_color_change);
        ButterKnife.bind(this);
        imageviewUser.setOnClickListener(this);


    }

    @Override
    protected ThemeColorChangePresenter initPresenter() {
        return new ThemeColorChangePresenter();
    }

    //take image from camera and device

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case UtilityForProfileImage.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    /*
        * select image from camera and mobile device
        * */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ThemeColorChangeActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = UtilityForProfileImage.checkPermission(ThemeColorChangeActivity.this);


                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }


    //onCaptureImageResult

    public void onCaptureImageResult(Intent data) {
         thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(this)
                .load(bytes.toByteArray())
                .asBitmap()
                .into(imageviewUser);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                relativeLayout.setBackgroundColor(GetColorUtil.PalletColorFromImage(thumbnail));
            }
        }, 500);
    }


    //onSelectFromGalleryResult

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
         bm = null;
        if (data != null) {
            try {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File destination = new File(Environment.getExternalStorageDirectory(),
                      System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Glide.with(this)
                        .load(bytes.toByteArray())
                        .asBitmap()
                        .into(imageviewUser);



                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        relativeLayout.setBackgroundColor(GetColorUtil.PalletColorFromImage(bm));
                    }
                }, 500);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    @Override
    public void onClick(View view) {
        selectImage();
    }
}
