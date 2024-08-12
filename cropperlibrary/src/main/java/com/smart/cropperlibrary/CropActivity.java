package com.smart.cropperlibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CropActivity extends AppCompatActivity {

    private ImageView imageView;
    private Rect cropRect;  // Rect to define the crop area
    private CropImageView customCropView;  // Assume this is your custom view for the cropping UI


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageView = findViewById(R.id.imageView);
        customCropView = findViewById(R.id.cropImageView);  // Reference to your custom crop view

        Uri imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        imageView.setImageURI(imageUri);

        // Assuming customCropView provides the cropping Rect
        cropRect = customCropView.getCropRect();

        findViewById(R.id.cropButton).setOnClickListener(v -> cropImage());
    }

    private void cropImage() {
        if (cropRect == null) {
            Toast.makeText(this, "Invalid crop area", Toast.LENGTH_SHORT).show();
            return;
        }

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        // Ensure the cropRect is within the bounds of the bitmap
        int x = Math.max(cropRect.left, 0);
        int y = Math.max(cropRect.top, 0);
        int width = Math.min(cropRect.width(), bitmap.getWidth() - x);
        int height = Math.min(cropRect.height(), bitmap.getHeight() - y);

        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "Invalid crop dimensions", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        imageView.setImageBitmap(croppedBitmap);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("croppedBitmap", croppedBitmap);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}