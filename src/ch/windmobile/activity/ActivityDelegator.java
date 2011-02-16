package ch.windmobile.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import ch.windmobile.model.ClientFactory;

public interface ActivityDelegator {
    public StationBrowsingActivity getActivity();
    public ClientFactory getClientFactory();
    
    public void updateView();
    public void refreshView();

    public void onCreate(Bundle savedInstanceState);
    public void onPause();
    public void onResume();
    public boolean onTouchEvent(MotionEvent event);
}
