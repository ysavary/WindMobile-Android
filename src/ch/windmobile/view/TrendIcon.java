package ch.windmobile.view;

import ch.windmobile.WindMobile;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TrendIcon extends View {
    private static final int arrowLenghtDp = 6;
    private static final int arrowHeightDp = 3;

    private final float density = getContext().getResources().getDisplayMetrics().density;
    private final float arrowLenght = WindMobile.toPixel(arrowLenghtDp, density);
    private final float arrowHeight = WindMobile.toPixel(arrowHeightDp, density);

    private float angle = 0;
    private float drawWidth;
    private float drawHeight;
    private Paint linePaint;

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
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1);
        linePaint.setAntiAlias(true);
    }

    private void initializeViewForLayout() {
        drawWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
        drawHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (angle != -1) {
            initializeViewForLayout();

            Path arrow = new Path();
            arrow.moveTo(0, -1);
            arrow.lineTo(drawWidth - arrowLenght, -1);
            arrow.lineTo(drawWidth - arrowLenght, -arrowHeight);
            arrow.lineTo(drawWidth, 0);
            arrow.lineTo(drawWidth - arrowLenght, arrowHeight);
            arrow.lineTo(drawWidth - arrowLenght, 1);
            arrow.lineTo(0, 1);
            arrow.lineTo(0, -1);

            Matrix matrix = new Matrix();
            matrix.postTranslate(-drawWidth / 2f, 0);
            matrix.postRotate(-getAngle());
            matrix.postTranslate(getPaddingLeft() + drawWidth / 2f, getPaddingTop() + drawHeight / 2f);
            arrow.transform(matrix);

            canvas.drawPath(arrow, linePaint);
        }
    }
}
