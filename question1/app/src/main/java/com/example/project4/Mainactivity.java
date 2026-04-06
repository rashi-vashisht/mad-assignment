package com.example.project4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    GridView gridView;
    ArrayList<File> imageList = new ArrayList<>();
    File storageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnOpenFolder = findViewById(R.id.btnOpenFolder);

        // 1. Create the dedicated folder
        storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedPhotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Initially hide the grid until "Open Folder" is clicked (Optional, based on your choice)
        gridView.setVisibility(View.GONE);

        // Take Photo Logic
        btnCapture.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                openCamera();
            }
        });

        // 2. Open Folder Logic: When clicked, show the photos
        btnOpenFolder.setOnClickListener(v -> {
            loadImages();
            gridView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Showing photos from MyCapturedPhotos", Toast.LENGTH_SHORT).show();
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra("path", imageList.get(position).getAbsolutePath());
            startActivity(i);
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    private void loadImages() {
        imageList.clear();
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".jpg")) {
                    imageList.add(f);
                }
            }
        }
        gridView.setAdapter(new ImageAdapter(this, imageList));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");

            // Save inside the special folder
            File file = new File(storageDir, "IMG_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Toast.makeText(this, "Saved to Folder!", Toast.LENGTH_SHORT).show();

                // Refresh folder view automatically
                loadImages();
                gridView.setVisibility(View.VISIBLE);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}


