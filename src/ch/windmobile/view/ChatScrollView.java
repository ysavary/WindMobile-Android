package ch.windmobile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ScrollView;

public class ChatScrollView extends ScrollView {
    private static final int MAX_Y_OVERSCROLL_DISTANCE = 30;

    private int mMaxYOverscrollDistance;
    private OverScrollListener overScrollListener;

    public interface OverScrollListener {
        public void onOverScroll(int scrollY);
    }

    public ChatScrollView(Context context) {
        this(context, null);
    }

    public ChatScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    public void showLastMessage() {
        int height = getHeight();
        int bottom = height;

        int count = getChildCount();
        if (count > 0) {
            View view = getChildAt(count - 1);
            bottom = view.getBottom();
        }
        scrollTo(0, bottom);
    }

    public void setOverScrollListener(OverScrollListener overScrollListener) {
        this.overScrollListener = overScrollListener;
    }

    private void notifyOverScroll(int scrollY) {
        if (overScrollListener != null) {
            overScrollListener.onOverScroll(scrollY);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        showLastMessage();

        super.onSizeChanged(w, h, oldw, oldh);
    }

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
}
