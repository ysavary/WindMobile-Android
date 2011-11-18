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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import ch.windmobile.R;

public class TrendIcon extends View {

    private float angle = 0;
    private float drawWidth;
    private float drawHeight;
    private Paint paint;
    private Bitmap arrowUp;
    private Bitmap arrowDown;
    float arrowWidth;
    float arrowHeight;

    public TrendIcon(Context context) {
        super(context);
        initialize();
    }

    public TrendIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        if (angle != -1) {
            if (angle < -90) {
                angle = -90;
            } else if (angle > 90) {
                angle = 90;
            }
        }
        this.angle = angle;
    }

    private void initialize() {
        paint = new Paint();
        paint.setFilterBitmap(true);
        arrowUp = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_red);
        arrowDown = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_green);
        arrowWidth = arrowUp.getWidth();
        arrowHeight = arrowUp.getHeight();
    }

    private void initializeViewForLayout() {
        drawWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
        drawHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (angle != -1) {
            initializeViewForLayout();

            float scale = drawWidth / arrowWidth;

            Matrix matrix = new Matrix();
            matrix.postRotate(-angle, arrowWidth / 2f, arrowHeight / 2f);
            matrix.postScale(scale, scale);
            matrix.postTranslate(getPaddingLeft() + (drawWidth - arrowWidth * scale) / 2f, getPaddingTop() + (drawHeight - arrowHeight * scale) / 2f);
            if (angle > 0) {
                canvas.drawBitmap(arrowUp, matrix, paint);
            } else {
                canvas.drawBitmap(arrowDown, matrix, paint);
            }
        }
    }
}
