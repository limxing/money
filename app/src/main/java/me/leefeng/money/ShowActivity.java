package me.leefeng.money;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ShowActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        imageView = (ImageView)findViewById(R.id.show_imageView);
        String imageurl = getIntent().getStringExtra("imageurl");

        Glide.with(this).load(imageurl).into(imageView);
    }
    public void back(View view){
        finish();
    }
}
