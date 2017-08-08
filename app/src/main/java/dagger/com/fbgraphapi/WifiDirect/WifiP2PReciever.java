package dagger.com.fbgraphapi.WifiDirect;

/**
 * Created by Taha on 6/22/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import dagger.com.fbgraphapi.ContactActivity;

import static android.content.Context.MODE_PRIVATE;

public class WifiP2PReciever extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Context mContext;


    public WifiP2PReciever(WifiP2pManager manager, WifiP2pManager.Channel channel,
                           Context mContext) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mContext = mContext;
    }

    String TAG =  "WIFIP2PRECVR";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION onReceive: " + state);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d("PeerLIst", "onPeersAvailable: " + peers.getDeviceList());
                        //Toast.makeText(activity,"onPeersAvailable: " + peers.getDeviceList(), Toast.LENGTH_SHORT).show();
                        ((OnPeerListRecieved) mContext).onPeerListRecieved(peers);

                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION onReceive: " + state);
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d("PeerLIst", "onPeersAvailable: " + peers.getDeviceList());
                        //Toast.makeText(activity,"onPeersAvailable: " + peers.getDeviceList(), Toast.LENGTH_SHORT).show();
                        ((OnPeerListRecieved) mContext).onPeerListRecieved(peers);
                    }
                });
            }


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION onReceive: " + state);
            if (ContactActivity.communicate){
                mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) mContext);
                ContactActivity.communicate = false;
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION onReceive: " + state);

            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d("MYDEV", "onReceive: " + device.deviceName + " " + device.deviceAddress);
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("com.slickapp.www.offline", MODE_PRIVATE);
            sharedPreferences.edit().putString("mydevicename", device.deviceName).commit();
            sharedPreferences.edit().putString("mydeviceaddr", device.deviceAddress).commit();
        }
    }

    public interface OnPeerListRecieved {
        void onPeerListRecieved(WifiP2pDeviceList peers);
    }

}
