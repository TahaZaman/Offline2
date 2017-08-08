package dagger.com.fbgraphapi;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import dagger.com.fbgraphapi.WifiDirect.SendMessage;
import dagger.com.fbgraphapi.WifiDirect.ServerThread;
import dagger.com.fbgraphapi.utils.Contact;

import org.json.JSONObject;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements ServerThread.OnChatRecieved {
    public ListView chatList;
    public ArrayList<String> chat;
    String senderIp;
    int myPort;
    int sendPort;
    Contact contact;
    public ChatListAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        contact = (Contact) getIntent().getSerializableExtra("contact");
        senderIp = contact.getIpAddress();
        myPort = getIntent().getIntExtra("myport", -1);
        sendPort = contact.getPort();
        chat = new ArrayList<>();
        chatList = (ListView) findViewById(R.id.chatList);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(contact.getName());
        setSupportActionBar(myToolbar);
        customAdapter = new ChatListAdapter(this, R.layout.chat_my_list_item, chat);
        chatList.setAdapter(customAdapter);
        chatList.setDivider(null);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String message = ((EditText) findViewById(R.id.messageSend)).getEditableText().toString();
                    JSONObject jon = new JSONObject();
                    jon.put("type", ServerThread.CHAT);
                    jon.put("message", message);
                    chat.add("my:" + message);
                    customAdapter.notifyDataSetChanged();
                    ((EditText) findViewById(R.id.messageSend)).setText("");
                    AsyncTask<Void, Void, Void> my_task = new SendMessage(senderIp, sendPort, jon.toString(), getApplicationContext());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    else
                        my_task.execute((Void[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        if (serverThread == null)
            serverThread = new ServerThread(this, myPort);
        if ( serverThread.getState() == Thread.State.NEW )
            //then we have a brand new thread not started yet, lets start it
            serverThread.start();
    }

    ServerThread serverThread = null;

    @Override
    public void onPause() {
        serverThread.tearDown();
        super.onPause();


        //serverThread.stop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        serverThread.tearDown();
        finish();
    }

    @Override
    public void onChatRecieved(String message) {
        chat.add("to:" + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                customAdapter.notifyDataSetChanged();
            }
        });

    }
}