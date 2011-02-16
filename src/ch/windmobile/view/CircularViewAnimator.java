package ch.windmobile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewAnimator;

public class CircularViewAnimator extends ViewAnimator {

    public CircularViewAnimator(Context context) {
        super(context);
    }

    public CircularViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getNextChild() {
        int child = getDisplayedChild() + 1;
        if (child == getChildCount()) {
            return 0;
        }
        return child;
    }

    protected int getPreviousChild() {
        int child = getDisplayedChild() - 1;
        if (child == -1) {
            return getChildCount() - 1;
        }
        return child;
    }

    public View getNextView() {
        return getChildAt(getNextChild());
    }

    public View getPreviousView() {
        return getChildAt(getPreviousChild());
    }

    @Override
    public void showNext() {
        setDisplayedChild(getNextChild());
    }

    @Override
    public void showPrevious() {
        setDisplayedChild(getPreviousChild());
    }
}