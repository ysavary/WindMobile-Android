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

import java.util.List;
import java.util.Vector;

import ch.windmobile.R;
import ch.windmobile.WindMobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

public class WindDirection extends View {
    private final float density = getContext().getResources().getDisplayMetrics().density;

    private final String[] directionLabels;
    private List<Float> directions = new Vector<Float>();
    private double drawWidth;
    private double drawHeight;
    private double lineRadius;

    public WindDirection(Context context) {
        super(context);
        directionLabels = context.getResources().getStringArray(R.array.directions);
    }

    public WindDirection(Context context, AttributeSet attrs) {
        super(context, attrs);
        directionLabels = context.getResources().getStringArray(R.array.directions);
    }

    public void addDirection(float direction) {
        if (direction < 0) {
            direction = 0;
        } else if (direction > 359) {
            direction = 0;
        }
        directions.add(direction);
    }

    public void clearDirections() {
        directions.clear();
    }

    private float getAngleInDegree(float degree) {
        return degree + 90;
    }

    private double getAngleInRadian(float degree) {
        return Math.toRadians(getAngleInDegree(degree));
    }

    private void initializeViewForLayout() {
        drawWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
        drawHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
    }

    protected void drawCompass(Canvas canvas) {
        Paint labelPaint = new Paint();
        labelPaint.setTextAlign(Align.LEFT);
        labelPaint.setTextSize(WindMobile.toPixel(10, density));
        labelPaint.setColor(Color.WHITE);
        labelPaint.setStrokeWidth(1);
        labelPaint.setAntiAlias(true);
        FontMetrics labelFontMetrics = labelPaint.getFontMetrics();
        double textHeight = labelFontMetrics.bottom - labelFontMetrics.top;
        double labelRadius = Math.min(drawWidth, drawHeight) / 2.0 - textHeight * 0.3;
        double labelOffsetX = getPaddingLeft() + (drawWidth - 2.0 * labelRadius) / 2.0;
        double labelOffsetY = getPaddingTop() + (drawHeight - 2.0 * labelRadius) / 2.0;
        float directionAngle = 0;
        for (int directionIndex = 0; directionIndex < directionLabels.length; directionIndex++) {
            double x = Math.cos(getAngleInRadian(directionAngle)) * labelRadius;
            double y = Math.sin(getAngleInRadian(directionAngle)) * labelRadius;
            String text = directionLabels[directionIndex];
            Rect textRect = new Rect();
            labelPaint.getTextBounds(text, 0, text.length(), textRect);
            canvas.drawText(text, 0, text.length(), (float) (labelOffsetX + labelRadius - x - textRect.width() / 2.0), (float) (labelOffsetY
                + labelRadius - y + textRect.height() / 2.0), labelPaint);

            /*
             * Paint pointPaint = new Paint(); pointPaint.setColor(Color.RED);
             * pointPaint.setStrokeWidth(1); canvas.drawPoint((float)
             * (labelOffsetX + labelRadius - x), (float) (labelOffsetY +
             * labelRadius - y), pointPaint);
             */
            directionAngle = directionAngle + 360 / directionLabels.length;
        }

        lineRadius = Math.min(drawWidth, drawHeight) / 2.0 - textHeight;
        double lineOffsetX = getPaddingLeft() + (drawWidth - 2.0 * lineRadius) / 2.0;
        double lineOffsetY = getPaddingTop() + (drawHeight - 2.0 * lineRadius) / 2.0;
        Paint linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setAntiAlias(true);
        canvas.drawCircle((float) (lineOffsetX + lineRadius), (float) (lineOffsetY + lineRadius), (float) (lineRadius), linePaint);
    }

    protected void drawDirections(Canvas canvas) {
        if (directions != null && (directions.size() > 0)) {
            Paint pointPaint = new Paint();
            pointPaint.setTextAlign(Align.LEFT);
            pointPaint.setColor(Color.YELLOW);
            pointPaint.setStrokeWidth(2);
            pointPaint.setAntiAlias(true);

            double radius = 0;

            // The center
            float lastX = (float) (getPaddingLeft() + drawWidth / 2.0);
            float lastY = (float) (getPaddingTop() + drawHeight / 2.0);

            for (int i = 0; i < directions.size(); i++) {
                float direction = directions.get(i);
                radius = radius + lineRadius / directions.size();

                double pointOffsetX = getPaddingLeft() + (drawWidth - 2.0 * radius) / 2.0;
                double pointOffsetY = getPaddingTop() + (drawHeight - 2.0 * radius) / 2.0;

                double circleX = Math.cos(getAngleInRadian(direction)) * radius;
                double circleY = Math.sin(getAngleInRadian(direction)) * radius;

                float x = (float) (pointOffsetX + radius - circleX);
                float y = (float) (pointOffsetY + radius - circleY);

                canvas.drawLine(lastX, lastY, x, y, pointPaint);
                lastX = x;
                lastY = y;
            }
            canvas.drawCircle(lastX, lastY, 4, pointPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initializeViewForLayout();
        drawCompass(canvas);
        drawDirections(canvas);
    }
}
