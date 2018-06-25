package com.shadyboshra2012.android.alarmthere.newalarm;

import com.google.android.gms.maps.model.LatLng;

public class PossibleLocation {
    private String mID = "";
    private String mPlaceID = "";
    private String mName = "";
    private String mVicinity = "";
    private LatLng mLatLng;
    private String mReference = "";
    private String mIcon = "";

    public boolean isSelected;

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        this.mID = ID;
    }

    public String getPlaceID() {
        return mPlaceID;
    }

    public void setPlaceID(String PlaceID) {
        this.mPlaceID = PlaceID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public String getVicinity() {
        return mVicinity;
    }

    public void setVicinity(String Vicinity) {
        this.mVicinity = Vicinity;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng LatLng) {
        this.mLatLng = LatLng;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String Reference) {
        this.mReference = Reference;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String Icon) {
        this.mIcon = Icon;
    }
}
