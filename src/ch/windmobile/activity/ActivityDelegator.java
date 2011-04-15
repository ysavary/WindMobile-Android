package ch.windmobile.activity;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import ch.windmobile.model.ClientFactory;

public interface ActivityDelegator {
    public StationBrowsingActivity getActivity();
    public ClientFactory getClientFactory();
    
    public void updateView();
    public void refreshView();

    public void onCreate(Bundle savedInstanceState);
    public void onAttachedToWindow();
    public void onPause();
    public void onResume();
    
    public boolean onTouchEvent(MotionEvent event);
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo);
    public boolean onMenuItemSelected(int featureId, MenuItem item);
}
