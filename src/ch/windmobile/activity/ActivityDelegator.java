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
    public boolean onContextItemSelected(MenuItem item);
}
