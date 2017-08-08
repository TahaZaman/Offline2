package dagger.com.fbgraphapi.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.nio.ByteOrder;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Taha on 8/5/2017.
 */

public class UtilityFunctions {
    public static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }


    public static String getDottedDecimalIP(int ipAddr) {

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddr = Integer.reverseBytes(ipAddr);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddr).toByteArray();

        //convert to dotted decimal notation:
        String ipAddrStr = getDottedDecimalIP(ipByteArray);
        return ipAddrStr;
    }
    public static int getNextFreePort() {
        int localPort = -1;
        try {
            ServerSocket s = new ServerSocket(0);
            localPort = s.getLocalPort();


            //closing the port
            if (s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v("DXDXD", Build.MANUFACTURER + ": free port requested: " + localPort);

        return localPort;
    }

    public static String getWiFiIPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ip = getDottedDecimalIP(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

}
