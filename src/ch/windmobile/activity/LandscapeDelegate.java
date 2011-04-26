package ch.windmobile.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.ClientFactory.WindChartData;
import ch.windmobile.model.StationDataUtils;
import ch.windmobile.model.WindMobileException;
import ch.windmobile.view.ZoomChartView;

import com.artfulbits.aiCharts.ChartView;
import com.artfulbits.aiCharts.Base.ChartArea;
import com.artfulbits.aiCharts.Base.ChartAxis;
import com.artfulbits.aiCharts.Base.ChartAxis.Position;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.artfulbits.aiCharts.Enums.Alignment;

public class LandscapeDelegate implements ActivityDelegator, OnClickListener {
    public static final int chart_duration_6_hours = 21600;
    public static final int chart_duration_12_hours = 43200;
    public static final int chart_duration_1_day = 86400;
    public static final int chart_duration_2_days = 172800;

    private final StationBrowsingActivity activity;
    private final ClientFactory clientFactory;

    // 6 hours by default
    int chartDuration = 21600;

    ZoomChartView chartView;
    ChartArea chartArea;
    ChartSeries windAverageSeries;
    ChartSeries windMaxSeries;
    ChartAxis yAxis;
    ChartAxis xAxis;

    String[] directionLabels;

    ImageView info;
    PopupWindow chartDurationButton;

    public LandscapeDelegate(StationBrowsingActivity activity, ClientFactory clientFactory) {
        this.activity = activity;
        this.clientFactory = clientFactory;
    }

    @Override
    public StationBrowsingActivity getActivity() {
        return activity;
    }

    @Override
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        chartView = new ZoomChartView(getActivity(), R.xml.windchart, WindMobile.readControlsShowDelay(getActivity()));
        getActivity().setContentView(chartView);

        float labelTextSize = getActivity().getWindMobile().toPixel(14);
        float axisTitleSize = getActivity().getWindMobile().toPixel(12);

        windAverageSeries = chartView.getSeries().get("windAverage");
        windMaxSeries = chartView.getSeries().get("windMax");

        windAverageSeries.setBackDrawable(getActivity().getResources().getDrawable(R.drawable.chart_gradient));

        Paint labelPaint = new Paint();
        labelPaint.setTextSize(labelTextSize);
        labelPaint.setColor(WindMobile.whiteTextColor);
        labelPaint.setAntiAlias(true);
        windMaxSeries.setMarkerPaint(labelPaint);
        windMaxSeries.setVLabelAlignment(Alignment.Near);
        directionLabels = activity.getResources().getStringArray(R.array.directions);

        chartArea = chartView.getAreas().get(0);
        yAxis = chartArea.getDefaultYAxis();
        StringBuffer title = new StringBuffer();
        title.append(getActivity().getText(R.string.chart_yaxis_title));
        title.append(" ");
        title.append(getActivity().getText(R.string.kmh_unit));
        yAxis.setTitle(title.toString());
        yAxis.getTitlePaint().setTextSize(axisTitleSize);
        yAxis.getLabelPaint().setTextSize(labelTextSize);
        yAxis.setPosition(Position.Right);

        xAxis = chartArea.getDefaultXAxis();
        xAxis.setLabelsMode(ChartAxis.LabelsMode.HybridLabels);
        xAxis.setFormat(new SimpleDateFormat("E HH:mm"));
        xAxis.setLabelAlignment(Alignment.Center);
        xAxis.getScale().setMargin(0);
        xAxis.getLabelPaint().setTextSize(labelTextSize);
        // Fix grid line sometime outside the chart area
        xAxis.setGridVisible(false);

        // Zoom
        chartView.setPanning(ChartView.PANNING_HORIZONTAL);
        chartView.enableZooming(chartArea);

        // Chart duration selection
        info = new ImageView(getActivity());
        info.setImageResource(R.drawable.info);
        info.setOnClickListener(this);
        chartDurationButton = new PopupWindow(info, info.getDrawable().getMinimumWidth(), info.getDrawable().getMinimumHeight());
        getActivity().registerForContextMenu(chartView);
    }

    @Override
    public void onAttachedToWindow() {
        chartDurationButton.showAtLocation(chartView, Gravity.LEFT | Gravity.TOP, (int) getActivity().getWindMobile().toPixel(10),
            (int) getActivity().getWindMobile().toPixel(5) + info.getDrawable().getMinimumHeight());
    }

    @Override
    public void onPause() {
        chartView.dissmissZoomControls(false);
    }

    @Override
    public void onResume() {
    }

    @Override
    public void updateView() {
        try {
            String stationId = getActivity().getController().getCurrentStationId();
            new WaitForChart().execute(stationId, chartDuration);
        } catch (Exception e) {
            Log.e("LandscapeDelegate", "updateView()", e);
        }
    }

    @Override
    public void refreshView() {
        updateView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == info) {
            getActivity().openContextMenu(chartView);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.chart_duration, menu);
        menu.setHeaderTitle(R.string.menu_chart_duration_title);

        // Context menu is not persistent, set the selected chart duration
        switch (chartDuration) {
        case chart_duration_6_hours:
            menu.getItem(0).setChecked(true);
            break;

        case chart_duration_12_hours:
            menu.getItem(1).setChecked(true);
            break;

        case chart_duration_1_day:
            menu.getItem(2).setChecked(true);
            break;

        case chart_duration_2_days:
            menu.getItem(3).setChecked(true);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.chart_duration_6_hours:
            if (chartDuration != chart_duration_6_hours) {
                chartDuration = chart_duration_6_hours;
                refreshView();
            }
            return true;

        case R.id.chart_duration_12_hours:
            if (chartDuration != chart_duration_12_hours) {
                chartDuration = chart_duration_12_hours;
                refreshView();
            }
            return true;

        case R.id.chart_duration_1_day:
            if (chartDuration != chart_duration_1_day) {
                chartDuration = chart_duration_1_day;
                refreshView();
            }
            return true;

        case R.id.chart_duration_2_days:
            if (chartDuration != chart_duration_2_days) {
                chartDuration = chart_duration_2_days;
                refreshView();
            }
            return true;

        default:
            return false;
        }
    }

    double round(double value, int step) {
        return Math.floor((value + step) / step) * step;
    }

    void updateChart(WindChartData windChartData) {
        // remove old points
        windAverageSeries.getPoints().clear();
        windMaxSeries.getPoints().clear();

        double maxScale = 0;

        if (windChartData.windAverage.length() > 3) {
            try {
                for (int index = 0; index < windChartData.windAverage.length(); index++) {
                    long date = windChartData.windAverage.getJSONObject(index).getLong("date");
                    double averageValue = windChartData.windAverage.getJSONObject(index).getDouble("value");
                    windAverageSeries.getPoints().addDate(date, averageValue);
                }
            } catch (JSONException e) {
                Log.w("LandscapeDelegate", "Unable to display 'windAverage' chart", e);
            }
        } else {
            Log.w("LandscapeDelegate", "Not enough points to display 'windAverage' chart");
        }

        // Log.i("LandscapeDelegate", "Start wind max chart");
        if (windChartData.windMax.length() > 3) {
            try {
                int windMaxLength = windChartData.windMax.length();
                int windDirectionLength = windChartData.windDirection.length();

                int maxNumberOfLabels = 50;
                // Round to the near odd number
                int peakVectorSize = (int) Math.round((double) windMaxLength / maxNumberOfLabels * 2) * 2 - 1;
                peakVectorSize = Math.max(peakVectorSize, 3);
                int margin = (peakVectorSize / 2);

                // In case of windDirection.length != windMax.length
                double windDirectionScale = windDirectionLength / windMaxLength;

                for (int maxIndex = 0; maxIndex < windMaxLength; maxIndex++) {
                    long date = windChartData.windMax.getJSONObject(maxIndex).getLong("date");
                    double maxValue = windChartData.windMax.getJSONObject(maxIndex).getDouble("value");
                    windMaxSeries.getPoints().addDate(date, maxValue);

                    maxScale = Math.max(maxScale, maxValue);

                    // Add windDirection labels
                    int startIndex = maxIndex - (peakVectorSize - 1);
                    if ((startIndex >= 0) && (maxValue > 0)) {
                        List<Double> values = new ArrayList<Double>(peakVectorSize);
                        for (int i = startIndex; i <= maxIndex; i++) {
                            values.add(windMaxSeries.getPoints().get(i).getY(0));
                        }

                        if (StationDataUtils.isPeak(values)) {
                            int middleIndex = maxIndex - margin;
                            int windDirectionIndex = (int) Math.round(middleIndex * windDirectionScale);
                            double direction = windChartData.windDirection.getJSONObject(windDirectionIndex).getDouble("value");
                            String label = StationDataUtils.getWindDirectionLabel(directionLabels, (float) direction);
                            ChartPoint maxPoint = windMaxSeries.getPoints().get(middleIndex);
                            maxPoint.setLabel(label);
                            maxPoint.setShowLabel(true);
                        }
                    }

                }
            } catch (JSONException e) {
                Log.w("LandscapeDelegate", "Unable to display 'winMax' chart", e);
            }
        } else {
            Log.w("LandscapeDelegate", "Not enough points to display 'winMax' chart");
        }
        // Log.i("LandscapeDelegate", "End wind max chart");

        chartArea.refresh();

        // Fix scale boundaries
        if (maxScale < 5) {
            maxScale = 5;
        } else {
            maxScale = round(maxScale, 5);

        }
        yAxis.getScale().setRange(0, maxScale);

        if (maxScale >= 80) {
            yAxis.getScale().setInterval(20d);
        } else if (maxScale >= 20) {
            yAxis.getScale().setInterval(10d);
        } else {
            yAxis.getScale().setInterval(5d);
        }

        chartView.setZoomFactor(0.5);
    }

    final class WaitForChart extends AsyncTask<Object, Void, WindChartData> {
        private String stationId;
        private int duration;
        private Exception error;

        @Override
        protected void onPreExecute() {
            getActivity().showProgressDialog();
        }

        @Override
        protected WindChartData doInBackground(Object... params) {
            stationId = (String) params[0];
            duration = (Integer) params[1];
            try {
                return getClientFactory().getWindChart(stationId, duration);
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(WindChartData data) {
            if (data != null) {
                updateChart(data);
                getActivity().dismissProgressDialog();
            } else {
                getActivity().dismissProgressDialog();
                if (!getActivity().isFinishing()) {
                    WindMobileException clientException = WindMobile.createException(getActivity(), error);
                    if (clientException.isFatal()) {
                        WindMobile.buildFatalErrorDialog(getActivity(), clientException).show();
                    } else {
                        WindMobile.buildErrorDialog(getActivity(), clientException).show();
                    }
                }
            }
        }
    }
}
