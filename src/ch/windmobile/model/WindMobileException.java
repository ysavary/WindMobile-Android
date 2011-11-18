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
package ch.windmobile.model;

public abstract class WindMobileException extends Exception {
    private static final long serialVersionUID = 1L;

    private final CharSequence localizedName;
    private final boolean isFatal;

    public WindMobileException(CharSequence localizedName, CharSequence message) {
        super(message.toString());
        this.localizedName = localizedName;
        this.isFatal = false;
    }

    public WindMobileException(CharSequence localizedName, CharSequence message, boolean isFatal) {
        super(message.toString());
        this.localizedName = localizedName;
        this.isFatal = isFatal;
    }

    public CharSequence getLocalizedName() {
        return localizedName;
    }

    public boolean isFatal() {
        return isFatal;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
