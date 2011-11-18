/*******************************************************************************
 * Copyright (c) 2011 epyx SA.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
