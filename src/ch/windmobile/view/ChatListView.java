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

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

public class ChatListView extends ListView {
    private static final int MAX_Y_OVERSCROLL_DISTANCE = 30;

    private int mMaxYOverscrollDistance;
    private OverScrollListener overScrollListener;

    public interface OverScrollListener {
        public void onOverScroll(int scrollY);
    }

    public ChatListView(Context context) {
        this(context, null);
    }

    public ChatListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    public void setOverScrollListener(OverScrollListener overScrollListener) {
        this.overScrollListener = overScrollListener;
    }

    private void notifyOverScroll(int scrollY) {
        if (overScrollListener != null) {
            overScrollListener.onOverScroll(scrollY);
        }
    }

    /*
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (clampedY) {
            notifyOverScroll(scrollY);
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX,
        int maxOverScrollY, boolean isTouchEvent) {

        return super
            .overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
    }
    */
}
