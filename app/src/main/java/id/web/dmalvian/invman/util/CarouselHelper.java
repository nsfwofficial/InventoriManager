package id.web.dmalvian.invman.util;

import android.content.Context;
import android.widget.ImageView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.List;

import id.web.dmalvian.invman.R;

public class CarouselHelper {
    private CarouselView carouselView;
    private final List<ImageView> imagesList;
    private Context context;

    public CarouselHelper(Context context, CarouselView carouselView, int itemCount) {
        this.context = context;
        this.carouselView = carouselView;
        this.carouselView.setPageCount(itemCount);
        this.imagesList = new ArrayList<>();
        this.carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageResource(R.drawable.ic_insert_photo);
                imagesList.add(imageView);
            }
        });
    }

    public List<ImageView> getListImages() {
        return imagesList;
    }

    public ImageView getImageView(int position) {
        return imagesList.get(position);
    }

    public int getCurrentItem() {
        return carouselView.getCurrentItem();
    }
}
