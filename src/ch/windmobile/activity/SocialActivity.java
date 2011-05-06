package ch.windmobile.activity;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.model.StationInfo;

public class SocialActivity extends ClientFactoryActivity implements OnClickListener {

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = View.inflate(this, R.layout.social, null);
        setContentView(view);

        Button send = (Button) view.findViewById(R.id.send);
        send.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView lastMessages = (TextView) view.findViewById(R.id.lastMessages);
        try {
            lastMessages.setText(getClientFactory().getLastMessages());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        TextView reply = (TextView) view.findViewById(R.id.reply);
        try {
            getClientFactory().postMessage(reply.getText().toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public WaitForStationInfos getWaitForStationInfos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStationInfos(List<StationInfo> stationInfos) {
        // TODO Auto-generated method stub

    }
}
