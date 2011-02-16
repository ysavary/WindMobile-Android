package ch.windmobile.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.ClientFactory.ChartPoint;
import ch.windmobile.model.StationDataUtils;
import ch.windmobile.model.WindMobileException;
import ch.windmobile.view.ZoomChartView;

import com.artfulbits.aiCharts.ChartView;
import com.artfulbits.aiCharts.Base.ChartArea;
import com.artfulbits.aiCharts.Base.ChartAxis;
import com.artfulbits.aiCharts.Base.ChartAxis.Position;
import com.artfulbits.aiCharts.Base.ChartAxis.ValueType;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.artfulbits.aiCharts.Enums.Alignment;

public class LandscapeDelegate implements ActivityDelegator {
    private final StationBrowsingActivity activity;
    private final ClientFactory clientFactory;

    ZoomChartView chartView;
    ChartArea chartArea;
    ChartSeries windAverageSeries;
    ChartSeries windMaxSeries;
    ChartAxis yAxis;
    ChartAxis xAxis;

    String[] directionLabels;

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

        chartArea = chartView.getAreas().get(0);
        chartArea.setPadding((int) getActivity().getWindMobile().toPixel(10), 0, 0, 0);

        windAverageSeries = chartView.getSeries().get("windAverage");
        windMaxSeries = chartView.getSeries().get("windMax");

        windAverageSeries.setBackDrawable(getActivity().getResources().getDrawable(R.drawable.chart_gradient));

        Paint labelPaint = new Paint();
        labelPaint.setTextSize(labelTextSize);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setAntiAlias(true);
        windMaxSeries.setMarkerPaint(labelPaint);
        windMaxSeries.setVLabelAlignment(Alignment.Near);
        directionLabels = activity.getResources().getStringArray(R.array.directions);

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
        xAxis.setFormat(new SimpleDateFormat("E HH:mm"));
        xAxis.setLabelAlignment(Alignment.Far);
        xAxis.getScale().setMargin(0);
        xAxis.getLabelPaint().setTextSize(labelTextSize);
        // Fix grid line sometime outside the chart area
        xAxis.setGridVisible(false);

        // Zoom
        chartView.setPanning(ChartView.PANNING_HORIZONTAL);
        chartView.enableZooming(chartArea);
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
            new WaitForChart().execute(stationId, WindMobile.readChartDuration(getActivity()));
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

    double round(double value, int step) {
        return Math.floor((value + step) / step) * step;
    }

    void updateChart(List<ChartPoint> chartPoints) {
        // remove old points
        windAverageSeries.getPoints().clear();
        windMaxSeries.getPoints().clear();

        int chartDuration = WindMobile.readChartDuration(getActivity());
        int peakVectorSize;
        if (chartDuration <= 14400) {
            peakVectorSize = 3;
        } else if (chartDuration <= 43200) {
            peakVectorSize = 5;
        } else if (chartDuration <= 86400) {
            peakVectorSize = 7;
        } else {
            peakVectorSize = 9;
        }
        int margin = (peakVectorSize / 2);
        double maxScale = 0;
        int nbPoints = chartPoints.size();
        if (nbPoints > 3) {
            for (int index = 0; index < chartPoints.size(); index++) {
                ChartPoint chartPoint = chartPoints.get(index);

                windAverageSeries.getPoints().addDate(chartPoint.date, chartPoint.averageValue);
                windMaxSeries.getPoints().addDate(chartPoint.date, chartPoint.maxValue);

                maxScale = Math.max(maxScale, chartPoint.maxValue);
                int startIndex = index - margin;
                int stopIndex = index + margin;

                if ((startIndex >= 0) && (stopIndex < chartPoints.size()) && (chartPoint.maxValue > 0)) {
                    List<Double> values = new ArrayList<Double>(peakVectorSize);
                    for (int i = startIndex; i <= stopIndex; i++) {
                        values.add(chartPoints.get(i).maxValue);
                    }

                    if (StationDataUtils.isPeak(values)) {
                        String direction = StationDataUtils.getWindDirectionLabel(directionLabels, (float) chartPoint.direction);
                        com.artfulbits.aiCharts.Base.ChartPoint maxPoint = windMaxSeries.getPoints().get(windMaxSeries.getPoints().size() - 1);
                        maxPoint.setLabel(direction);
                        maxPoint.setShowLabel(true);
                    }
                }
            }

            xAxis.setValueType(ValueType.Date);
        }

        chartArea.refresh();

        // Fix scale boundaries
        if (maxScale < 5) {
            maxScale = 5;
        } else {
            maxScale = round(maxScale, 5);

        }
        yAxis.getScale().setRange(0, maxScale);

        chartView.setZoomFactor(0.5);
    }

    final class WaitForChart extends AsyncTask<Object, Void, List<ChartPoint>> {
        private String stationId;
        private int duration;
        private Exception error;

        @Override
        protected void onPreExecute() {
            getActivity().showProgressDialog();
        }

        @Override
        protected List<ChartPoint> doInBackground(Object... params) {
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
        protected void onPostExecute(List<ChartPoint> chartPoints) {
            if (chartPoints != null) {
                updateChart(chartPoints);
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
