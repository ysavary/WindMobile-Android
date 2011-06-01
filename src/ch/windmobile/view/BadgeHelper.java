package ch.windmobile.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BadgeHelper {

    public static Drawable drawBadge(Context context, Integer count, int iconWidth, int iconHeight, int sizePercent) {
        Bitmap bitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = iconWidth * sizePercent / 100 / 2;
        float strokeWidth = 1.5f;

        paint.setStyle(Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle(iconWidth - strokeWidth - radius, iconHeight - strokeWidth - radius, radius, paint);

        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(iconWidth - strokeWidth - radius, iconHeight - strokeWidth - radius, radius, paint);

        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(2 * radius * 0.7f);
        paint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        paint.setTextAlign(Align.LEFT);
        String badge = count.toString();
        Rect textBound = new Rect();
        paint.getTextBounds(badge, 0, badge.length(), textBound);
        canvas.drawText(badge, iconWidth - strokeWidth - 2 * radius + (2 * radius - textBound.right - textBound.left) / 2, iconHeight - strokeWidth
            - radius + (textBound.bottom - textBound.top) / 2, paint);

        return new BitmapDrawable(bitmap);
    }
}
