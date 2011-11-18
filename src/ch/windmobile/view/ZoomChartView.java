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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import ch.windmobile.R;
import ch.windmobile.WindMobile;

import com.artfulbits.aiCharts.ChartView;
import com.artfulbits.aiCharts.Base.ChartArea;
import com.artfulbits.aiCharts.Base.ChartAxisScale;

public class ZoomChartView extends ChartView implements OnClickListener {
    private static final double zoomMin = 1d;
    private static final double zoomMax = 0.05d;

    private final float density = getResources().getDisplayMetrics().density;

    private double touchDistance1 = Double.NaN;
    private double touchDistance2 = Double.NaN;
    private double zoomFactor = 1;
    private ChartArea chartArea;

    final Timer timer = new Timer();
    final Handler handler = new Handler();
    TimerTask zoomControlsTask;
    int zoomControlsShowDelay;
    ImageButton zoomDownButton;
    ImageButton zoomUpButton;
    PopupWindow zoomControls;

    public ZoomChartView(Context context, int chartId, int zoomControlsShowDelay) {
        super(context, chartId);

        // Zoom controls
        this.zoomControlsShowDelay = zoomControlsShowDelay;

        View zoomControlsView = LayoutInflater.from(context).inflate(R.layout.chart_zoom_controls, (ViewGroup) getParent(), false);
        zoomDownButton = (ImageButton) zoomControlsView.findViewById(R.id.zoom_down);
        zoomDownButton.setOnClickListener(this);
        zoomUpButton = (ImageButton) zoomControlsView.findViewById(R.id.zoom_up);
        zoomUpButton.setOnClickListener(this);
        zoomControlsView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        zoomControls = new PopupWindow(zoomControlsView, zoomControlsView.getMeasuredWidth(), zoomControlsView.getMeasuredHeight());
    }

    public void enableZooming(ChartArea chartArea) {
        this.chartArea = chartArea;
    }

    public void disableZooming() {
        chartArea = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        showZoomControls();
        if (chartArea != null && event.getPointerCount() > 1) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                double dx = event.getX(0) - event.getX(1);
                double dy = event.getY(0) - event.getY(1);

                // compute distance between pointers
                touchDistance2 = Math.sqrt(dx * dx + dy * dy);

                if (Double.isNaN(touchDistance1)) {
                    touchDistance1 = touchDistance2;
                } else {
                    // compute current zoom factor
                    double currentZoomFactor = zoomFactor * touchDistance1 / touchDistance2;
                    zoomToFactor(currentZoomFactor);
                }
            }

            return true;
        } else if (!Double.isNaN(touchDistance1)) {
            // save current zoom factor
            zoomFactor *= touchDistance1 / touchDistance2;
            zoomFactor = validateFactor(zoomFactor);

            // reset distances
            touchDistance1 = Double.NaN;
            touchDistance2 = Double.NaN;

            return true;
        }
        return super.onTouchEvent(event);
    }

    private double validateFactor(double factor) {
        factor = Math.min(factor, zoomMin);
        factor = Math.max(factor, zoomMax);

        return factor;
    }

    public void setZoomFactor(double currentZoomFactor) {
        zoomFactor = validateFactor(currentZoomFactor);
        zoomToFactor(zoomFactor);
    }

    private void zoomToFactor(double currentZoomFactor) {
        currentZoomFactor = validateFactor(currentZoomFactor);

        if (chartArea != null) {
            ChartAxisScale xAxisScale = chartArea.getDefaultXAxis().getScale();

            double size = currentZoomFactor * (xAxisScale.getRealMaximum() - xAxisScale.getRealMinimum());
            xAxisScale.setZoom(xAxisScale.getVisibleMaximum() - size, size);

            zoomDownButton.setEnabled(currentZoomFactor < zoomMin);
            zoomUpButton.setEnabled(currentZoomFactor > zoomMax);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == zoomUpButton) {
            setZoomFactor(zoomFactor - 0.2);
            showZoomControls();
        } else if (view == zoomDownButton) {
            setZoomFactor(zoomFactor + 0.2);
            showZoomControls();
        }
    }

    public synchronized void showZoomControls() {
        if (zoomControlsTask != null) {
            zoomControlsTask.cancel();
        } else {
            View parent = (View) getParent();

            zoomControls.setAnimationStyle(R.style.fade_out);
            zoomControls.showAtLocation(parent, Gravity.NO_GRAVITY, (int) WindMobile.toPixel(20, density),
                parent.getHeight() - (int) WindMobile.toPixel(80, density));
        }

        zoomControlsTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dissmissZoomControls(true);
                    }
                });
            }
        };
        timer.schedule(zoomControlsTask, zoomControlsShowDelay);
    }

    public synchronized void dissmissZoomControls(boolean fade) {
        if ((zoomControls.getContentView() != null) && (zoomControls.getContentView().getParent() != null)) {
            if (!fade) {
                zoomControls.setAnimationStyle(0);
                zoomControls.update();
            }
            zoomControls.dismiss();
        }

        if (zoomControlsTask != null) {
            zoomControlsTask.cancel();
        }
        zoomControlsTask = null;
    }
}
