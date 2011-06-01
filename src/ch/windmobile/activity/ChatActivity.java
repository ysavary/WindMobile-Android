package ch.windmobile.activity;

import java.util.List;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.Message;
import ch.windmobile.model.WindMobileException;

public class ChatActivity extends ClientFactoryActivity implements OnClickListener {

    private View view;
    private String chatRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = View.inflate(this, R.layout.chat, null);
        setContentView(view);

        if (savedInstanceState != null) {
            chatRoom = savedInstanceState.getString(StationInfosActivity.SELECTED_STATION);
        }
        if (chatRoom == null) {
            chatRoom = getIntent().getStringExtra(StationInfosActivity.SELECTED_STATION);
        }

        Button send = (Button) view.findViewById(R.id.send);
        send.setOnClickListener(this);
    }

    public void refresh() {
        final ScrollView scroller = (ScrollView) view.findViewById(R.id.scrollView);
        final LinearLayout lastMessages = (LinearLayout) view.findViewById(R.id.lastMessages);

        lastMessages.removeAllViews();

        try {
            List<Message> messages = getClientFactory().getLastMessages(chatRoom, 10);

            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);

                RelativeLayout cell = (RelativeLayout) View.inflate(this, R.layout.chat_cell, null);

                TextView pseudo = (TextView) cell.findViewById(R.id.pseudo);
                TextView date = (TextView) cell.findViewById(R.id.date);
                TextView text = (TextView) cell.findViewById(R.id.text);

                pseudo.setText(message.getPseudo());
                date.setText(DateUtils.getRelativeTimeSpanString(message.getDate().getTime()));
                text.setText(message.getText());

                lastMessages.addView(cell);
            }
        } catch (Exception e) {
            WindMobileException clientException = WindMobile.createException(this, e);
            WindMobile.buildErrorDialog(this, clientException).show();
        }

        scroller.post(new Runnable() {
            public void run() {
                scroller.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onClick(View v) {
        TextView reply = (TextView) view.findViewById(R.id.reply);
        try {
            getClientFactory().postMessage(chatRoom, reply.getText().toString());
            reply.setText(null);
            refresh();
        } catch (Exception e) {
            WindMobileException clientException = WindMobile.createException(this, e);
            WindMobile.buildErrorDialog(this, clientException).show();
        }
    }
}
