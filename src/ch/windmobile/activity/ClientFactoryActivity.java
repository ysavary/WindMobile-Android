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

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;

public abstract class ClientFactoryActivity extends Activity implements IClientFactoryActivity {
    private ClientFactory clientFactory;
    private boolean isLandscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientFactory = getWindMobile().getClientFactory();
        isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public WindMobile getWindMobile() {
        return (WindMobile) getApplication();
    }
}
