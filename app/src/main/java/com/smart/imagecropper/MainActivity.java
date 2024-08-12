package com.smart.imagecropper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.smart.cropperlibrary.CropActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_CROP_IMAGE = 1001;
    private ImageView imageView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.croppedImage);
        Button selectGalleryBtn = findViewById(R.id.selectGalleryBtn);
        Button selectCameraBtn = findViewById(R.id.selectCameraBtn);

        selectGalleryBtn.setOnClickListener(v -> checkPermissionsAndPickImage());
        selectCameraBtn.setOnClickListener(v -> checkCameraPermissionAndCapture());
    }

    private void checkPermissionsAndPickImage() {
        // Check permissions and pick image (code remains the same)
        if (Build.VERSION.SDK_INT >= 33) {  // Android 13 (API level 33)
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_IMAGE_PICK);
            } else {
                dispatchPickIntent();
            }
        } else if (Build.VERSION.SDK_INT >= 30) {  // Android 11 to 12
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_IMAGE_PICK);
            } else {
                dispatchPickIntent();
            }
        } else {  // Android 10 and below
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_IMAGE_PICK);
            } else {
                dispatchPickIntent();
            }
        }
    }

    private void dispatchPickIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Capture successful, start cropping
            if (currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                Uri capturedImageUri = Uri.fromFile(f);

                if (capturedImageUri != null) {
                    // Start CropActivity
                    Intent cropIntent = new Intent(MainActivity.this, CropActivity.class);
                    cropIntent.putExtra("imageUri", capturedImageUri.toString());
                    startActivityForResult(cropIntent, REQUEST_CROP_IMAGE);
                }
            }

        }

        else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(bitmap);

                // Start CropActivity
                Intent cropIntent = new Intent(MainActivity.this, CropActivity.class);
                cropIntent.putExtra("imageUri", selectedImageUri.toString());
                startActivityForResult(cropIntent, REQUEST_CROP_IMAGE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CROP_IMAGE && resultCode == RESULT_OK && data != null) {
            Bitmap croppedBitmap = data.getParcelableExtra("croppedBitmap");
            imageView.setImageBitmap(croppedBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with capturing image
                dispatchTakePictureIntent();
            } else {
                // Camera permission denied, handle accordingly (show a message, disable functionality, etc.)
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_PICK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with capturing image
                dispatchPickIntent();
            } else {
                // Camera permission denied, handle accordingly (show a message, disable functionality, etc.)
                Toast.makeText(this, "Camera permission 2 denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with capturing image
            dispatchTakePictureIntent();
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.smart.imagecropper.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


}