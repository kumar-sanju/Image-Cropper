package com.smart.cropperlibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CropActivity extends AppCompatActivity {

    private ImageView imageView;
    private CropImageView cropImageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageView = findViewById(R.id.imageView);
        cropImageView = findViewById(R.id.cropImageView);
        Button cropButton = findViewById(R.id.cropButton);

        // Load the selected image into the ImageView
        String imageUriString = getIntent().getStringExtra("imageUri");
        Uri imageUri = Uri.parse(imageUriString);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropButton.setOnClickListener(v -> cropImage());
    }

    private void cropImage() {
        Rect cropRect = cropImageView.getCropRect();
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());

        // Pass the cropped bitmap back to the original activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("croppedBitmap", croppedBitmap);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}