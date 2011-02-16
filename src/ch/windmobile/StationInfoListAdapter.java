package ch.windmobile;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.model.StationInfo;

public class StationInfoListAdapter extends BaseAdapter {

    private Context context;
    private final List<StationInfo> stationInfos;
    private int rowResource;
    private boolean isLandscape;
    private LayoutInflater inflater;

    public StationInfoListAdapter(Context context, List<StationInfo> stationInfos, int rowResource, boolean isLandscape) {
        this.context = context;
        this.stationInfos = stationInfos;
        this.rowResource = rowResource;
        this.isLandscape = isLandscape;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<StationInfo> getStationInfos() {
        return stationInfos;
    }

    @Override
    public int getCount() {
        return stationInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return stationInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, rowResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        bindView(position, view, isLandscape);
        return view;
    }

    private void bindView(int position, View view, boolean isLandscape) {
        final CheckBox favorite = (CheckBox) view.findViewById(R.id.row_favorite);
        final TextView name = (TextView) view.findViewById(R.id.row_name);
        final TextView altitude = (TextView) view.findViewById(R.id.row_altitude);

        final StationInfo stationInfo = (StationInfo) getItem(position);
        // Optional favorite checkbox
        if (favorite != null) {
            favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    stationInfo.setFavorite(isChecked);
                }
            });
            favorite.setChecked(stationInfo.isFavorite());
        }

        name.setText(stationInfo.getName());
        if (isLandscape) {
            name.setText(stationInfo.getName());
        } else {
            name.setText(stationInfo.getShortName());
        }
        altitude.setText(stationInfo.getAltitude() + " " + context.getText(R.string.meters_text));
    }
}
