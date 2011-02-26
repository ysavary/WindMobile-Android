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
    private final float density = getContext().getResources().getDisplayMetrics().density;

    private float angle = 0;
    private float drawWidth;
    private float drawHeight;
    private Paint paint;
    private Bitmap arrow;
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
        arrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
        arrowWidth = arrow.getWidth();
        arrowHeight = arrow.getHeight();
    }

    private void initializeViewForLayout() {
        drawWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
        drawHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (angle != -1) {
            initializeViewForLayout();

            // Fix "visible" size of the arrow to 22dp
            float scale = 22 * density / arrowWidth;

            Matrix matrix = new Matrix();
            matrix.postRotate(-angle, arrowWidth / 2f, arrowHeight / 2f);
            matrix.postScale(scale, scale);
            matrix.postTranslate(getPaddingLeft() + (drawWidth - arrowWidth * scale) / 2f, getPaddingTop() + (drawHeight - arrowHeight * scale) / 2f);
            canvas.drawBitmap(arrow, matrix, paint);
        }
    }
}
