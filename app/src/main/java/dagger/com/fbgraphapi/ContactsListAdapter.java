package dagger.com.fbgraphapi;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import dagger.com.fbgraphapi.utils.Contact;

import java.util.ArrayList;

/**
 * Created by Taha on 8/5/2017.
 */

public class ContactsListAdapter extends ArrayAdapter<Contact> {

    //private ArrayList<File> files;

    public ContactsListAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
        //this.files = files;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Contact contact = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_list_item, parent, false);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WifiP2pDevice device;

                final ContactActivity contactActivity = (ContactActivity) getContext();
                contactActivity.progressDialog.show();
                ContactActivity.communicate = true;
                WifiP2pConfig config = new WifiP2pConfig();
                if (!contact.getDeviceAddress().isEmpty() && !contact.getDeviceAddress().equals(null)) {
                    config.deviceAddress = contact.getDeviceAddress();
                    config.wps.setup = WpsInfo.PBC;
                    config.groupOwnerIntent = 4;
                    contactActivity.contactSelectedPos = position;
                    contactActivity.getmManager().connect(contactActivity.getmChannel(), config, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            //success logic

                            Log.d("ConnectPeers", "Success: connected");
                            Toast.makeText(getContext(), "Success: connected", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            //failure logic

                            contactActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(contactActivity.progressDialog.isShowing())
                                    contactActivity.progressDialog.dismiss();
                                }
                            });
                            Log.d("ConnectPeers", "onFailure: failed" + reason);
                            Toast.makeText(getContext(), "DiscoverPeeers Failure", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.contact_name_tv);
        // Populate the data into the template view using the data object
        tvName.setText(contact.getName());

        ((TextView) convertView.findViewById(R.id.contact_status_tv)).setText(contact.getStatus());

        // Return the completed view to render on screen
        return convertView;
    }

}