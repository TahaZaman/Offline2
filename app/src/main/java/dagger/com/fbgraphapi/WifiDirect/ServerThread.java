package dagger.com.fbgraphapi.WifiDirect;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import dagger.com.fbgraphapi.utils.Contact;
import dagger.com.fbgraphapi.utils.UtilityFunctions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Taha on 6/22/2017.
 */

public class ServerThread extends Thread {

    private int mPort;
    private Context mContext;
    private ServerSocket mServer;
    public static int HANDSHAKE = 1;
    public static int HANDSHAKE_REPSONSE = 2;
    public static int CHAT = 3;


    private boolean acceptRequests = true;

    public ServerThread(Context context, int port) {
        this.mContext = context;
        this.mPort = port;

    }

    @Override
    public void run() {
        try {
            Log.d("DXDX", Build.MANUFACTURER + ": conn listener: " + mPort);
            mServer = new ServerSocket(mPort);
            mServer.setReuseAddress(true);

            if (mServer != null && !mServer.isBound()) {
                mServer.bind(new InetSocketAddress(mPort));
            }

            Log.d("DDDD", "Inet4Address: " + Inet4Address.getLocalHost().getHostAddress());

            Socket socket = null;
            while (acceptRequests) {
                // this is a blocking operation
                socket = mServer.accept();
                handleData(socket.getInetAddress().getHostAddress(), socket.getInputStream(), socket.getPort());
            }
            Log.e("DXDX", Build.MANUFACTURER + ": Connection listener terminated. " +
                    "acceptRequests: " + acceptRequests);
            socket.close();
            mServer.close();
            socket = null;

        } catch (IOException e) {
            Log.e("DXDX", Build.MANUFACTURER + ": Connection listener EXCEPTION. " + e.toString());
            e.printStackTrace();
        }
    }

    private void handleData(String senderIP, InputStream inputStream, int port) {
        byte[] input;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            input = baos.toByteArray();
            baos.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            input = new byte[10];
        }
        try {
            JSONObject jsonObject = new JSONObject(new String(input));
            Log.d("Serverthread", "handleData: " + jsonObject);


            if (jsonObject.getInt("type") == HANDSHAKE) {
                tearDown();
                OnHandshake onHandshake = (OnHandshake) mContext;
                //intent.putExtra("sendtoip", senderIP);
                //intent.putExtra("senderport", jsonObject.getInt("port"));
                int myport = UtilityFunctions.getNextFreePort();
                //intent.putExtra("myport", myport);
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("com.slickapp.www.offline", MODE_PRIVATE);
                Contact myContact = new Contact();
                myContact.setDeviceAddress(sharedPreferences.getString("mydeviceaddr", ""));
                myContact.setDeviceName(sharedPreferences.getString("mydevicename", ""));
                Activity act = (Activity) mContext;
                myContact.setName(act.getIntent().getStringExtra("fname") + " " + act.getIntent().getStringExtra("lname"));
                JSONObject jon = new JSONObject();
                jon.put("type", HANDSHAKE_REPSONSE);
                jon.put("port", myport);
                jon.put("contact", myContact);
                jon.put("senderport", jsonObject.getInt("port"));


                AsyncTask<Void, Void, Void> my_task = new SendMessage(senderIP, jsonObject.getInt("res_port"), jon.toString(), mContext);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                else
                    my_task.execute((Void[]) null);
                //mContext.startActivity(intent);
                onHandshake.onHandshake(senderIP, jsonObject.getInt("port"), myport, Contact.fromString(jsonObject.getString("contact")));
            } else if (jsonObject.getInt("type") == HANDSHAKE_REPSONSE) {
                tearDown();
               /* Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("sendtoip", senderIP);
                intent.putExtra("senderport", jsonObject.getInt("port"));
                intent.putExtra("myport", jsonObject.getInt("senderport"));
                mContext.startActivity(intent);*/


                OnHandshakeResponse onHandshakeResponse = (OnHandshakeResponse) mContext;
                onHandshakeResponse.onHandshakeResponse(senderIP, jsonObject.getInt("port"), jsonObject.getInt("senderport")
                        , Contact.fromString(jsonObject.getString("contact")));


            } else if (jsonObject.getInt("type") == CHAT) {
                /*final ChatActivity activity = (ChatActivity) mContext;
                final String message = "to:" + jsonObject.getString("message");
                //message = "to:" + message;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.chat.add(message);
                        activity.customAdapter.notifyDataSetChanged();
                    }
                });*/


                OnChatRecieved onChatRecieved = (OnChatRecieved) mContext;
                onChatRecieved.onChatRecieved(jsonObject.getString("message"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tearDown() {
        acceptRequests = false;
        try {
            if (mServer != null)
                mServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface OnHandshake {
        void onHandshake(String senderIP, int senderPort, int myPort, Contact contact);
    }

    public interface OnHandshakeResponse {
        void onHandshakeResponse(String senderIP, int senderPort, int myPort, Contact contact);
    }

    public interface OnChatRecieved {
        void onChatRecieved(String message);
    }


}
