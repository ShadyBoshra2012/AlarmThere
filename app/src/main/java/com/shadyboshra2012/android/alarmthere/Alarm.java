package com.shadyboshra2012.android.alarmthere;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Alarm implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    private int mID;
    private String mName;
    private String mPlaceName;
    private String mVicinity;
    private LatLng mLatLng = new LatLng(0, 0);
    private double mRangeDistance;
    private boolean mIsEnable;
    private float mMarkerColor;
    private Date mInsertedDate = new Date();

    public boolean isRinging;
    public boolean isSnoozed;
    public double snoozedRangeDistance;
    public Location userCurrentLatLng = new Location("");

    public Alarm() {}

    public void setID(int ID) {
        this.mID = ID;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public void setPlaceName(String PlaceName) {
        this.mPlaceName = PlaceName;
    }

    public void setVicinity(String Vicinity) {
        this.mVicinity = Vicinity;
    }

    public void setLatLng(LatLng LatLng) {
        this.mLatLng = LatLng;
    }

    public void setRangeDistance(double RangeDistance) {
        this.mRangeDistance = RangeDistance;
    }

    public void setEnable(boolean IsEnable) {
        this.mIsEnable = IsEnable;
    }

    public void setMarkerColor(float MarkerColor) {
        this.mMarkerColor = MarkerColor;
    }

    public void setInsertedDate(Date InsertedDate) {
        this.mInsertedDate = InsertedDate;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public String getVicinity() {
        return mVicinity;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public double getRangeDistance() {
        return mRangeDistance;
    }

    public String getRangeDistanceString() {
        if (getRangeDistance() <= 10000)
            return getRangeDistance() + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_meters);
        else
            return (getRangeDistance() / 1000) + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_kilo_meters);
    }

    public String getSnoozedRangeDistanceString() {
        if (snoozedRangeDistance <= 10000)
            return snoozedRangeDistance + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_meters);
        else
            return (snoozedRangeDistance / 1000) + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_kilo_meters);
    }

    public String getFarDistanceString() {
        Location selectedPlaceLocation = new Location("");
        selectedPlaceLocation.setLatitude(getLatLng().latitude);
        selectedPlaceLocation.setLongitude(getLatLng().longitude);

        float distanceInMetersOne = userCurrentLatLng.distanceTo(selectedPlaceLocation);

        if (distanceInMetersOne <= 10000)
            return (int) distanceInMetersOne + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_meters);
        else
            return (distanceInMetersOne / 1000) + " " + AppController.getInstance().getApplicationContext().getResources().getString(R.string.unit_kilo_meters);
    }

    public boolean isEnable() {
        return mIsEnable;
    }

    public float getMarkerColor() {
        return mMarkerColor;
    }

    public Date getInsertedDate() {
        return mInsertedDate;
    }

    //  Serializeing part
    /*private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(mLatLng.latitude);
        out.writeDouble(mLatLng.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mLatLng = new LatLng(in.readDouble(), in.readDouble());
    }*/

    // Parcelling part
    public Alarm(Parcel in) {
        this.mID = in.readInt();
        this.mName = in.readString();
        this.mPlaceName = in.readString();
        this.mVicinity = in.readString();
        this.mLatLng = in.readParcelable(LatLng.class.getClassLoader());
        this.mRangeDistance = in.readDouble();
        this.mIsEnable = in.readByte() != 0;
        this.mMarkerColor = in.readFloat();
        this.mInsertedDate = new Date(in.readLong());

        this.isRinging = in.readByte() != 0;
        this.isSnoozed = in.readByte() != 0;
        this.snoozedRangeDistance = in.readDouble();
        this.userCurrentLatLng = in.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mID);
        dest.writeString(this.mName);
        dest.writeString(this.mPlaceName);
        dest.writeString(this.mVicinity);
        dest.writeParcelable(this.mLatLng, flags);
        dest.writeDouble(this.mRangeDistance);
        dest.writeByte((byte) (this.mIsEnable ? 1 : 0));
        dest.writeFloat(this.mMarkerColor);
        dest.writeLong(this.mInsertedDate.getTime());

        dest.writeByte((byte) (this.isRinging ? 1 : 0));
        dest.writeByte((byte) (this.isSnoozed ? 1 : 0));
        dest.writeDouble(this.snoozedRangeDistance);
        dest.writeParcelable(this.userCurrentLatLng, flags);
    }
}
