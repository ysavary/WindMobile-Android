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
