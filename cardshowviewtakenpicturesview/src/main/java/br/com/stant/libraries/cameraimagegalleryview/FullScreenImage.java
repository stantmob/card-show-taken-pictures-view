package br.com.stant.libraries.cameraimagegalleryview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;

public class FullScreenImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        String fullImageUrl = getIntent().getStringExtra("fullImageUrl");
        ImageView fullImageView = findViewById(R.id.full_image_view);

        Glide.with(this).load(fullImageUrl).into(fullImageView);
    }
}