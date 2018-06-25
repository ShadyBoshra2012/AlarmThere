package com.shadyboshra2012.android.alarmthere.newalarm;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shadyboshra2012.android.alarmthere.R;

import java.util.ArrayList;

public class PossibleLocationAdapter extends ArrayAdapter<PossibleLocation> {

    private static final String LOG_TAG = PossibleLocation.class.getSimpleName();
    private Context mContext;
    private ArrayList<PossibleLocation> mPossibleLocation;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param PossibleLocation   A List of AndroidFlavor objects to display in a list
     */
    public PossibleLocationAdapter(Activity context, ArrayList<PossibleLocation> PossibleLocation) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.

        super(context, 0, PossibleLocation);
        this.mContext = context;
        this.mPossibleLocation = PossibleLocation;
    }

    private View listItemView;
    private LinearLayout itemBackgroundLayout;
    private TextView nameTextView;
    private TextView vicinityTextView;

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.location_list_item, parent, false);
        }

        final PossibleLocation currentPossibleLocation = getItem(position);

        itemBackgroundLayout = listItemView.findViewById(R.id.item_background_layout);

        nameTextView = listItemView.findViewById(R.id.name_text_view);
        nameTextView.setText(currentPossibleLocation.getName());

        vicinityTextView = listItemView.findViewById(R.id.vicinity_text_view);
        vicinityTextView.setText(currentPossibleLocation.getVicinity());

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < mPossibleLocation.size(); i++)
                    mPossibleLocation.get(i).isSelected = false;

                currentPossibleLocation.isSelected = true;
                notifyDataSetChanged();
            }
        });

        if (currentPossibleLocation.isSelected)
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item);
        else
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item_disable);

        return listItemView;
    }
}
