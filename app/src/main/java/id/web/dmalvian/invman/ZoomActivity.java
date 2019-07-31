package id.web.dmalvian.invman;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ZoomActivity extends AppCompatActivity {
    public static final String PHOTO_URI = "uri";
    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoView = findViewById(R.id.photo_view);
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Intent intent = getIntent();
        String uri = intent.getStringExtra(PHOTO_URI);
        Glide.with(getApplicationContext())
                .load(uri)
                .into(photoView);
    }

}
