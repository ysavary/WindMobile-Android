package ch.windmobile.activity;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ch.windmobile.ImageLoader;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.Message;
import ch.windmobile.model.WindMobileException;

public class ChatActivity extends ClientFactoryActivity implements OnClickListener {

    private View view;
    private String chatRoom;
    private ImageLoader imageLoader;
    private ListView messagesList;
    private ArrayAdapter<Message> messagesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = View.inflate(this, R.layout.chat, null);
        setContentView(view);
        messagesList = (ListView) view.findViewById(R.id.messages);
        messagesAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messagesAdapter);

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

    public int refresh() throws Exception {
        messagesAdapter.clear();

        List<Message> messages = getClientFactory().getLastMessages(chatRoom, 10);
        for (int i = messages.size() - 1; i >= 0; i--) {
            messagesAdapter.add(messages.get(i));
        }
        return messagesAdapter.getCount();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            int nbMessages = refresh();
            messagesAdapter.notifyDataSetChanged();
            messagesList.smoothScrollToPosition(nbMessages);
        } catch (Exception e) {
            WindMobileException clientException = WindMobile.createException(this, e);
            WindMobile.buildErrorDialog(this, clientException).show();
        }
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

    private class MessageAdapter extends ArrayAdapter<Message> {
        public MessageAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View cell = convertView;
            if (cell == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                cell = inflater.inflate(R.layout.chat_cell, parent, false);
            }
            Message message = getItem(position);
            try {
                ImageView avatar = (ImageView) cell.findViewById(R.id.avatar);
                TextView pseudo = (TextView) cell.findViewById(R.id.pseudo);
                TextView date = (TextView) cell.findViewById(R.id.date);
                TextView text = (TextView) cell.findViewById(R.id.text);

                imageLoader.displayImage(computeGravatarLink(message.getEmailHash()), ChatActivity.this, avatar);
                pseudo.setText(message.getPseudo());
                date.setText(DateUtils.getRelativeTimeSpanString(message.getDate().getTime()));
                text.setText(message.getText());
            } catch (Exception e) {
            }

            return cell;
        }
    };

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                refresh();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            messagesAdapter.notifyDataSetChanged();
        }
    }
}
