package com.shadyboshra2012.android.alarmthere;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.shadyboshra2012.android.alarmthere.database.AlarmsDbHelper;

import java.util.ArrayList;

public class AlarmAdapter extends ArrayAdapter<Alarm> {

    private static final String LOG_TAG = Alarm.class.getSimpleName();
    private static Context mContext;
    private static AlarmsDbHelper mDbHelper;
    private static ArrayList<Alarm> mAlarms;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param Alarm   A List of AndroidFlavor objects to display in a list
     */
    public AlarmAdapter(Activity context, ArrayList<Alarm> Alarm) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.

        super(context, 0, Alarm);
        this.mContext = context;
        mDbHelper = new AlarmsDbHelper(mContext);
        mAlarms = Alarm;
    }

    View listItemView;
    LinearLayout itemBackgroundLayout;
    TextView nameTextView;
    TextView placeTextView;
    TextView rangeTextView;
    TextView distanceTextView;
    ImageView markerImage;
    Switch isEnableSwitch;
    ImageView moreImage;

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.alarm_list_item, parent, false);
        }

        final Alarm currentAlarm = getItem(position);

        itemBackgroundLayout = (LinearLayout) listItemView.findViewById(R.id.item_background_layout);

        nameTextView = (TextView) listItemView.findViewById(R.id.name_text_view);
        nameTextView.setText(currentAlarm.getName());

        placeTextView = (TextView) listItemView.findViewById(R.id.place_name_text_view);
        placeTextView.setText(currentAlarm.getPlaceName());

        rangeTextView = (TextView) listItemView.findViewById(R.id.range_text_view);
        if (!currentAlarm.isSnoozed)
            rangeTextView.setText(mContext.getString(R.string.range_text_view, currentAlarm.getRangeDistanceString()));
        else
            rangeTextView.setText(mContext.getString(R.string.snoozed_range_text_view, currentAlarm.getSnoozedRangeDistanceString()));

        distanceTextView = (TextView) listItemView.findViewById(R.id.distance_text_view);
        distanceTextView.setText(mContext.getString(R.string.distance_text_view, currentAlarm.getFarDistanceString()));

        markerImage = (ImageView) listItemView.findViewById(R.id.marker_image);

        String colorFloat = Float.toString(currentAlarm.getMarkerColor());
        markerImage.setColorFilter(ResourcesCompat.getColor(mContext.getResources(), getColorResourceId(colorFloat), null));

        isEnableSwitch = (Switch) listItemView.findViewById(R.id.is_enable_switch);
        isEnableSwitch.setChecked(currentAlarm.isEnable());

        isEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPreferences.getInstance(mContext).showAd();

                currentAlarm.setEnable(isChecked);

                currentAlarm.isSnoozed = false;
                currentAlarm.snoozedRangeDistance = currentAlarm.getRangeDistance();

                mDbHelper.updateAlarm(mDbHelper, currentAlarm);

                setItemsBackground(buttonView, isChecked);

                notifyDataSetChanged();
            }
        });

        setItemsBackground(currentAlarm.isEnable());

        moreImage = (ImageView) listItemView.findViewById(R.id.more_image);
        moreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_edit:
                                AppPreferences.getInstance(mContext).showAd();

                                editAlarm(currentAlarm);
                                return true;
                            case R.id.action_delete:
                                AppPreferences.getInstance(mContext).showAd();

                                AlertDialog dialog = new AlertDialog.Builder(mContext)
                                        .setPositiveButton(mContext.getResources().getString(R.string.delete_dialog_delete), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                                                mDbHelper.deleteAlarm(mDbHelper, currentAlarm.getID());
                                                remove(currentAlarm);
                                                notifyDataSetChanged();
                                            }
                                        }).setNegativeButton(mContext.getResources().getString(R.string.delete_dialog_cancel), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                                            }
                                        }).setMessage(mContext.getResources().getString(R.string.delete_dialog_message))
                                        .setTitle(mContext.getResources().getString(R.string.delete_dialog_title)).create();
                                dialog.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.inflate(R.menu.menu_alarm);
                popup.show();
            }
        });

        return listItemView;
    }

    private void editAlarm(Alarm alarm) {
        Intent editAlarmIntent = new Intent(mContext, EditAlarmActivity.class);
        editAlarmIntent.putExtra("alarm", alarm);
        mContext.startActivity(editAlarmIntent);
    }

    private int getColorResourceId(String colorFloat) {
        switch (colorFloat) {
            case "0.0": //Red
                return R.color.RedMarker;
            case "210.0": //Azure
                return R.color.AzureMarker;
            case "240.0": //Blue
                return R.color.BlueMarker;
            case "180.0": //Cyan
                return R.color.CyanMarker;
            case "120.0": //Green
                return R.color.GreenMarker;
            case "300.0": //Magenta
                return R.color.MagentaMarker;
            case "30.0": //Orange
                return R.color.OrangeMarker;
            case "330.0": //Rose
                return R.color.RoseMarker;
            case "270.0": //Violet
                return R.color.VioletMarker;
            case "60.0": //Yellow
                return R.color.YellowMarker;
            default:
                return R.color.RedMarker;
        }
    }

    private void setItemsBackground(boolean isEnable) {
        if (isEnable) {
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item);
            nameTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));
            placeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorPrimary, null));
            rangeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));
            distanceTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));

            rangeTextView.setVisibility(View.VISIBLE);
            distanceTextView.setVisibility(View.VISIBLE);
        } else {
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item_disable);
            nameTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            placeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            rangeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            distanceTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));

            rangeTextView.setVisibility(View.GONE);
            distanceTextView.setVisibility(View.GONE);
        }
    }

    private void setItemsBackground(CompoundButton buttonView, boolean isEnable) {
        View v = (View) buttonView.getParent().getParent().getParent();

        itemBackgroundLayout = (LinearLayout) v.findViewById(R.id.item_background_layout);
        nameTextView = (TextView) v.findViewById(R.id.name_text_view);
        placeTextView = (TextView) v.findViewById(R.id.place_name_text_view);
        rangeTextView = (TextView) v.findViewById(R.id.range_text_view);
        distanceTextView = (TextView) v.findViewById(R.id.distance_text_view);

        if (isEnable) {
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item);
            nameTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));
            placeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorPrimary, null));
            rangeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));
            distanceTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.textColorSecondary, null));

            rangeTextView.setVisibility(View.VISIBLE);
            distanceTextView.setVisibility(View.VISIBLE);
        } else {
            itemBackgroundLayout.setBackgroundResource(R.drawable.background_shape_list_item_disable);
            nameTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            placeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            rangeTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));
            distanceTextView.setTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.actionBarDivider, null));

            rangeTextView.setVisibility(View.GONE);
            distanceTextView.setVisibility(View.GONE);
        }
    }
}
