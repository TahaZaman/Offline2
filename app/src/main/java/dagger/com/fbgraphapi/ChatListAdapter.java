package dagger.com.fbgraphapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Taha on 8/5/2017.
 */
public class ChatListAdapter extends ArrayAdapter<String> {

    public ChatListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ChatListAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        String p = getItem(position);
        if (p != null) {
            String myOrTo = p.split(":")[0];
            p = p.split(":")[1];
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                if (myOrTo.equals("my")) {
                    v = vi.inflate(R.layout.chat_my_list_item, null);
                }
                else
                    v = vi.inflate(R.layout.chat_to_list_item, null);
            }

            TextView tt1 = (TextView) v.findViewById(R.id.message);

            if (tt1 != null) {
                tt1.setText(p);
            }

        }

        return v;
    }

}