package com.enes.urgan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class FullscreenImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        Intent intent = getIntent();
        String stringUri = intent.getStringExtra("imgUri");

        ImageView fullscreenImage = findViewById(R.id.fullscreen_image);

        Uri imgUri = Uri.parse(stringUri);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            fullscreenImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("imgEntrySetError", e.toString());
            fullscreenImage.setImageResource(android.R.drawable.ic_dialog_alert);
        }
    }
}
