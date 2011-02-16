package ch.windmobile;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class SwipeAndSnapGestureDetector {
    // private final static int TOUCH_STATE_REST = 0;
    // private final static int TOUCH_STATE_SCROLLING = 1;

    // The velocity at which a fling gesture will cause us to snap to the next screen
    private static final float SNAP_VELOCITY = 800;

    // A tracker which to calculate the velocity of a mouvement
    private VelocityTracker mVelocityTracker;

    // The last known values of X
    private float lastMotionX;
    private float orginMotionX;

    public interface OnSwipeAndSnapListener {
        void updateNext();
        void updatePrevious();

        void onScroll(int distance);
        void snapToNext(float velocity);
        void snapToPrevious(float velocity);
        void snapToCurrent();

        void reset();
    }

    private final View view;
    private final OnSwipeAndSnapListener listener;

    public SwipeAndSnapGestureDetector(View view, OnSwipeAndSnapListener listener) {
        this.view = view;
        this.listener = listener;
    }

    /**
     * Track the touch event
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
             */
            listener.reset();

            listener.updateNext();
            listener.updatePrevious();

            // Remember where the motion event started
            orginMotionX = lastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
            // Scroll to follow the motion event
            final int deltaX = (int) (lastMotionX - x);
            lastMotionX = x;
            listener.onScroll(deltaX);
            break;
        case MotionEvent.ACTION_UP:
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000);
            float velocityX = velocityTracker.getXVelocity();

            if (velocityX > SNAP_VELOCITY) {
                // Fling hard enough to move left
                listener.snapToPrevious(Math.abs(velocityX));
            } else if (velocityX < -SNAP_VELOCITY) {
                // Fling hard enough to move right
                listener.snapToNext(Math.abs(velocityX));
            } else {
                @SuppressWarnings("unused")
                final int screenWidth = view.getWidth();
                if (x - orginMotionX < 0) {
                    listener.snapToNext(-1);
                } else if (x - orginMotionX > 0) {
                    listener.snapToPrevious(-1);
                } else {
                    listener.snapToCurrent();
                }
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        }

        return true;
    }
}
