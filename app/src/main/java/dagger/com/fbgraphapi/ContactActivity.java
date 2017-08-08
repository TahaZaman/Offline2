package dagger.com.fbgraphapi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import dagger.com.fbgraphapi.WifiDirect.PeerDiscoveryService;
import dagger.com.fbgraphapi.WifiDirect.SendMessage;
import dagger.com.fbgraphapi.WifiDirect.ServerThread;
import dagger.com.fbgraphapi.WifiDirect.WifiP2PReciever;
import dagger.com.fbgraphapi.utils.Contact;
import dagger.com.fbgraphapi.utils.UtilityFunctions;

import org.json.JSONObject;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener, ServerThread.OnHandshake,
        ServerThread.OnHandshakeResponse, ServerThread.OnChatRecieved, WifiP2PReciever.OnPeerListRecieved {

    ListView contactsListView;
    ArrayList<Contact> contacts = new ArrayList<>();
    ContactsListAdapter contactsListAdapter;

    private final IntentFilter intentFilter = new IntentFilter();
    ArrayList<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>();
    WifiP2pManager mManager;
    WifiManager wm;

    public WifiP2pManager getmManager() {
        return mManager;
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    BroadcastReceiver wifiP2PReciever;
    WifiP2pManager.Channel mChannel;
    public String TAG = "run2221";

    int port = 8667;
    int clientPort;
    String groupOwnerAddress;
    String sendToaddr;
    Contact sendToContact;

    PeerDiscoveryService peerDiscoveryService;
    SharedPreferences sharedPreferences;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("run2221", "onServiceConnected: ");
            PeerDiscoveryService.LocalBinder localBinder = (PeerDiscoveryService.LocalBinder) service;
            peerDiscoveryService = localBinder.getService();
            peerDiscoveryService.registerActivity(mManager, mChannel);
            //peerDiscoveryService.startDiscovery();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        contactsListView = (ListView) findViewById(R.id.contacts_listview);
        progressDialog = new ProgressDialog(this);


        contactsListAdapter = new ContactsListAdapter(this, contacts);
        contactsListView.setAdapter(contactsListAdapter);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        wifiP2PReciever = new WifiP2PReciever(mManager, mChannel, this);

        Intent serviceIntent = new Intent(this, PeerDiscoveryService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communicate = true;
                peerDiscoveryService.startDiscovery();
            }
        });


    }

    public int contactSelectedPos = -1;
    public ProgressDialog progressDialog;
    public static boolean communicate = false;

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            Log.d(TAG, "disconnect: " + Build.MANUFACTURER);
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);

                            }
                        });
                    }
                }
            });

            mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "cancelConnect onSuccess -");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "cancelConnect onSuccess -");

                }
            });

          /*  mManager = (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this, getMainLooper(), null);
            wifiP2PReciever = new WifiP2PReciever(mManager, mChannel, this);
            peerDiscoveryService.startDiscovery();*/
        }
    }


    public static final int REQUEST_CHAT_ACT = 101;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHAT_ACT)
            disconnect();
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e("test", "..onConnectionInfoAvailable.." + info);
        if (info != null) {
            try {
                groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
                //WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                Log.d("coninfo: owner address", groupOwnerAddress);


                if (!info.isGroupOwner && info.groupFormed && groupOwnerAddress != null) {
                    sendToaddr = groupOwnerAddress;
                    //sendToContact = new Contact();
                    //sendToContact.setIpAddress(sendToaddr);
                    String ip = UtilityFunctions.getWiFiIPAddress(this);
                    Log.d("coninfo: my address", ip);
                    int portno = UtilityFunctions.getNextFreePort();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", ServerThread.HANDSHAKE);
                    jsonObject.put("ip", ip);
                    jsonObject.put("port", portno);
                    clientPort = UtilityFunctions.getNextFreePort();
                    sharedPreferences = getSharedPreferences("com.slickapp.www.offline", MODE_PRIVATE);
                    jsonObject.put("res_port", clientPort);
                    Contact myContact = new Contact();
                    myContact.setDeviceAddress(sharedPreferences.getString("mydeviceaddr", ""));
                    myContact.setDeviceName(sharedPreferences.getString("mydevicename", ""));
                    myContact.setName(getIntent().getStringExtra("fname") + " " + getIntent().getStringExtra("lname"));
                    Log.d("MYCONTACT", "onConnectionInfoAvailable: " + myContact);
                    jsonObject.put("contact", myContact);
                    sendMessage(jsonObject.toString());

                    ServerThread serverThread = new ServerThread(this, clientPort);
                    serverThread.start();

                } else if (info.groupFormed) {

                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("test", "Owner Info null\n" + e.toString());
            }

        } else {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    public void sendMessage(String message) {

        //Client Peer
        Toast.makeText(this, "client sending message", Toast.LENGTH_SHORT).show();
        if (message == null)
            message = "Unknown";
        AsyncTask<Void, Void, Void> my_task = new SendMessage(sendToaddr, port, message, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        else
            my_task.execute((Void[]) null);


    }

    @Override
    public void onHandshake(String senderIP, int senderPort, int myPort, Contact contact) {
        Intent intent = new Intent(this, ChatActivity.class);
        contact.setIpAddress(senderIP);
        contact.setPort(senderPort);
        contact.setStatus("active");
        intent.putExtra("contact", contact);
        intent.putExtra("myport", myPort);
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        startActivityForResult(intent, REQUEST_CHAT_ACT);
    }

    @Override
    public void onHandshakeResponse(String senderIP, int senderPort, int myPort, Contact contact) {
        Intent intent = new Intent(this, ChatActivity.class);
        contact.setIpAddress(senderIP);
        contact.setPort(senderPort);
        intent.putExtra("contact", contact);
        intent.putExtra("myport", myPort);
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        this.startActivity(intent);
    }

    @Override
    public void onChatRecieved(String message) {

    }

    @Override
    public void onPeerListRecieved(WifiP2pDeviceList peers) {
        this.wifiP2pDeviceList.clear();
        this.wifiP2pDeviceList.addAll(peers.getDeviceList());
        Log.d(TAG, "setWifiP2pDeviceList: " + this.wifiP2pDeviceList);

        for (WifiP2pDevice device :
                wifiP2pDeviceList) {
            Contact contact = new Contact();
            contact.setName(device.deviceName);
            contact.setDeviceAddress(device.deviceAddress);
            contact.setStatus("active");


            if (!contacts.contains(contact))
                contacts.add(contact);
        }

        contactsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        if (serverThreadMain != null)
            serverThreadMain.tearDown();
        serverThreadMain = new ServerThread(this, port);
        serverThreadMain.start();
        super.onResume();

        registerReceiver(wifiP2PReciever, intentFilter);
        contacts.clear();
        contactsListAdapter.notifyDataSetChanged();
        communicate=true;
        if(peerDiscoveryService != null)
            peerDiscoveryService.startDiscovery();
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(wifiP2PReciever);
        serverThreadMain.tearDown();
        serverThreadMain = null;
    }

    ServerThread serverThreadMain = null;

    @Override
    public void onStart() {
        super.onStart();

       /* serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("run2221", "onServiceConnected: ");
                PeerDiscoveryService.LocalBinder localBinder = (PeerDiscoveryService.LocalBinder) service;
                peerDiscoveryService = localBinder.getService();
                peerDiscoveryService.registerActivity(mManager, mChannel);
                //peerDiscoveryService.startDiscovery();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: ");

            }
        };*/

        /*Intent serviceIntent = new Intent(this, PeerDiscoveryService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);*/
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }


}
