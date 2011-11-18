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