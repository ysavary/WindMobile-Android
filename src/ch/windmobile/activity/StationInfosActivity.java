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

import java.util.List;

import android.os.AsyncTask;
import ch.windmobile.WindMobile;
import ch.windmobile.model.StationInfo;

public abstract class StationInfosActivity extends ClientFactoryActivity implements IStationInfosActivity {

    @Override
    protected void onResume() {
        super.onResume();

        if (getClientFactory().needStationInfosUpdate()) {
            getWaitForStationInfos().execute();
        } else {
            setStationInfos(getClientFactory().getStationInfosCache());
        }
    }

    protected abstract class WaitForStationInfos extends AsyncTask<Void, Void, List<StationInfo>> {
        protected Exception error;

        @Override
        protected List<StationInfo> doInBackground(Void... params) {
            try {
                return getClientFactory().getStationInfos(WindMobile.readOperationalStationOnly(StationInfosActivity.this));
            } catch (Exception e) {
                error = e;
                return null;
            }
        }
    }

    @Override
    public abstract WaitForStationInfos getWaitForStationInfos();

    @Override
    public abstract void setStationInfos(List<StationInfo> stationInfos);
}
