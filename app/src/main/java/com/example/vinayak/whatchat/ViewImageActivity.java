package com.example.vinayak.whatchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    private ImageView Picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Picture=(ImageView)findViewById(R.id.Picture);
        String PICTURE_URL=getIntent().getExtras().getString("ImageURL");
        Picasso.with(ViewImageActivity.this).load(PICTURE_URL).into(Picture);
    }
}
