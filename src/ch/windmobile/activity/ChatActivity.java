package ch.windmobile.activity;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ch.windmobile.ImageLoader;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.Message;
import ch.windmobile.model.WindMobileException;
import ch.windmobile.view.ChatScrollView;

public class ChatActivity extends ClientFactoryActivity implements OnClickListener, ChatScrollView.OverScrollListener {

    private View view;
    private String chatRoom;
    private ImageLoader imageLoader;
    private ChatScrollView scrollView;
    private LinearLayout lastMessages;
    private View refreshView;
    private RefreshTask refreshTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = View.inflate(this, R.layout.chat, null);
        setContentView(view);
        scrollView = (ChatScrollView) view.findViewById(R.id.scrollView);
        scrollView.setOverScrollListener(this);
        lastMessages = (LinearLayout) view.findViewById(R.id.lastMessages);

        // Refresh view on overscroll
        refreshView = new TextView(this);
        ((TextView) refreshView).setText("Refreshing...");

        if (savedInstanceState != null) {
            chatRoom = savedInstanceState.getString(StationInfosActivity.SELECTED_STATION);
        }
        if (chatRoom == null) {
            chatRoom = getIntent().getStringExtra(StationInfosActivity.SELECTED_STATION);
        }

        imageLoader = new ImageLoader(this, R.drawable.mystery_man, 80);

        Button send = (Button) view.findViewById(R.id.send);
        send.setOnClickListener(this);
    }

    protected String computeGravatarLink(String emailHash) {
        return "http://www.gravatar.com/avatar/" + emailHash + ".jpg" + "?d=retro";
    }

    public void refresh() {
        lastMessages.removeAllViews();

        try {
            List<Message> messages = getClientFactory().getLastMessages(chatRoom, 10);

            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);

                RelativeLayout cell = (RelativeLayout) View.inflate(this, R.layout.chat_cell, null);

                ImageView avatar = (ImageView) cell.findViewById(R.id.avatar);
                TextView pseudo = (TextView) cell.findViewById(R.id.pseudo);
                TextView date = (TextView) cell.findViewById(R.id.date);
                TextView text = (TextView) cell.findViewById(R.id.text);

                imageLoader.displayImage(computeGravatarLink(message.getEmailHash()), this, avatar);
                pseudo.setText(message.getPseudo());
                date.setText(DateUtils.getRelativeTimeSpanString(message.getDate().getTime()));
                text.setText(message.getText());

                lastMessages.addView(cell);
                scrollView.showLastMessage();
            }
        } catch (Exception e) {
            WindMobileException clientException = WindMobile.createException(this, e);
            WindMobile.buildErrorDialog(this, clientException).show();
        }
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

    @Override
    public void onOverScroll(int scrollY) {
        if ((scrollY > 0) && (refreshTask == null)) {
            refreshTask = new RefreshTask();
            refreshTask.execute();
        }
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            refresh();
            lastMessages.addView(refreshView);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            lastMessages.removeView(refreshView);
            refreshTask = null;
        }
    }
}
