package dagger.com.fbgraphapi.WifiDirect;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Taha on 6/22/2017.
 */

public class PeerDiscoveryService extends Service {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private WifiP2pManager wifiP2pManager;
    private WifiP2pDeviceList wifiP2pDeviceList = null;
    private WifiP2pManager.Channel channel = null;
    //private OnDeviceListRecieved activity = null;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public PeerDiscoveryService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PeerDiscoveryService.this;
        }
    }

    public void registerActivity(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel) {
        //this.activity = activity;
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
    }

    Handler handler = new Handler();
    Runnable discoveryRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.d("run2221", "no: no");
            if (wifiP2pManager != null && channel != null) {
                //Log.d("run", "ok: ok");
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Log.d("DiscoverPeers", "onSuccess: onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("DiscoverPeers", "onFailure: failed");

                    }
                });
            }
            //handler.postDelayed(this,3000);
        }
    };

    public void startDiscovery() {
        Log.d("run2221", "startDiscovery: ");
        handler.postDelayed(discoveryRunnable,0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
