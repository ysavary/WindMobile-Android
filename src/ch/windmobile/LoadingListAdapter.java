package ch.windmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LoadingListAdapter extends BaseAdapter {

    private Context context;
    private int loadingRowResID;
    private View loadingRow;

    public LoadingListAdapter(Context context, int loadingRowResID) {
        this.context = context;
        this.loadingRowResID = loadingRowResID;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            if (loadingRow == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                loadingRow = inflater.inflate(loadingRowResID, parent, false);
            }
            return loadingRow;
        }
        return null;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
