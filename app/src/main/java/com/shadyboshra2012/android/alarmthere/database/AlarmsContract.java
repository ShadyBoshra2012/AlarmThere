package com.shadyboshra2012.android.alarmthere.database;

import android.provider.BaseColumns;

public final class AlarmsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private AlarmsContract() {
    }

    /* Inner class that defines the table contents */
    public static class Alarms implements BaseColumns {
        public static final String TABLE_NAME = "Alarms";
        public static final String ALARM_NAME = "AlarmName";
        public static final String PLACE_NAME = "PlaceName";
        public static final String VICINITY = "Vicinity";
        public static final String LAT = "Lat";
        public static final String LNG = "Lng";
        public static final String RANGE_DISTANCE = "RangeDistance";
        public static final String IS_ENABLE = "IsEnable";
        public static final String IMAGE_RESOURCE_ID = "ImageResourceId";
        public static final String IS_RINGING = "IsRinging";
        public static final String IS_SNOOZED = "IsSnoozed";
        public static final String SNOOZED_RANGE_DISTANCE = "SnoozedRangeDistance";
        public static final String INSERTED_DATE = "InsertedDate";
    }
}
