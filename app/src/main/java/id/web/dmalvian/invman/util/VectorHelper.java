package id.web.dmalvian.invman.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.widget.Button;

public class VectorHelper {
    public static final int DRAWABLE_TOP = 1;
    public static final int DRAWABLE_BOTTOM = 2;
    public static final int DRAWABLE_LEFT = 3;
    public static final int DRAWABLE_RIGHT = 4;

    public static Drawable setVectorForPreLollipop(int resourceId, Context activity) {
        Drawable icon;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            icon = VectorDrawableCompat.create(activity.getResources(), resourceId, activity.getTheme());
        } else {
            icon = activity.getResources().getDrawable(resourceId, activity.getTheme());
        }

        return icon;
    }


    public static void setVectorForPreLollipop(Button button, int resourceId, Context activity, int position) {
        Drawable icon;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            icon = VectorDrawableCompat.create(activity.getResources(), resourceId,
                    activity.getTheme());
        } else {
            icon = activity.getResources().getDrawable(resourceId, activity.getTheme());
        }
        switch (position) {
            case VectorHelper.DRAWABLE_LEFT:
                button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null,
                        null);
                break;

            case VectorHelper.DRAWABLE_RIGHT:
                button.setCompoundDrawablesWithIntrinsicBounds(null, null, icon,
                        null);
                break;

            case VectorHelper.DRAWABLE_TOP:
                button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null,
                        null);
                break;

            case VectorHelper.DRAWABLE_BOTTOM:
                button.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        icon);
                break;
        }
    }
}
