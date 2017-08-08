package dagger.com.fbgraphapi.WifiDirect;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Taha on 6/22/2017.
 */
public class SendMessage extends AsyncTask {

    String message;
    String destIP;
    int destPort;
    Context mContext;
    public SendMessage(String destIP, int destPort,String message, Context mContext)
    {
        this.message = message;
        this.destIP = destIP;
        this.destPort = destPort;
        this.mContext = mContext;
    }


    @Override
    protected Object doInBackground(Object... arg0) {
        // TODO Auto-generated method stub
        try {
            Socket socket = new Socket(destIP, destPort);
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(message);
            bw.flush();
            os.close();
            socket.close();
            Log.e("test", "Message sent to the server : " + message);

        } catch (IOException e) {
            Log.e("test", "CLIENT to SERVER message sending exception: " + e.getMessage() + "\n"
            + destIP + "\n" + destPort);
        } finally {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if (result != null) {
            Toast.makeText(mContext, "MESSAGE: ", Toast.LENGTH_SHORT).show();
            Log.e("test", "CLIENT to SERVER message sending exception: " + result.toString());
        }
    }

}