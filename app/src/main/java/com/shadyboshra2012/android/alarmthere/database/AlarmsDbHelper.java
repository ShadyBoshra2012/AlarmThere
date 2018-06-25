package com.shadyboshra2012.android.alarmthere.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;
import com.shadyboshra2012.android.alarmthere.Alarm;

import java.util.ArrayList;
import java.util.Date;

public class AlarmsDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alarms.db";

    private static final String SQL_CREATE_Alarms =
            "CREATE TABLE " + AlarmsContract.Alarms.TABLE_NAME + " (" +
                    AlarmsContract.Alarms._ID + " INTEGER PRIMARY KEY," +
                    AlarmsContract.Alarms.ALARM_NAME + " TEXT," +
                    AlarmsContract.Alarms.PLACE_NAME + " TEXT," +
                    AlarmsContract.Alarms.VICINITY + " TEXT," +
                    AlarmsContract.Alarms.LAT + " TEXT," +
                    AlarmsContract.Alarms.LNG + " TEXT," +
                    AlarmsContract.Alarms.RANGE_DISTANCE + " TEXT," +
                    AlarmsContract.Alarms.IS_ENABLE + " BIT DEFAULT 1," +
                    AlarmsContract.Alarms.IMAGE_RESOURCE_ID + " TEXT," +
                    AlarmsContract.Alarms.IS_RINGING + " BIT DEFAULT 0," +
                    AlarmsContract.Alarms.IS_SNOOZED + " BIT DEFAULT 0," +
                    AlarmsContract.Alarms.SNOOZED_RANGE_DISTANCE + " TEXT," +
                    AlarmsContract.Alarms.INSERTED_DATE + " DATETIME)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlarmsContract.Alarms.TABLE_NAME;

    public AlarmsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_Alarms);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_CREATE_Alarms);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private boolean getBoolean(int value) {
        return (value == 1);
    }

    private Date getDate(long value) {
        return new Date(value * 1000);
    }

    public long insertAlarm(AlarmsDbHelper dbHelper, Alarm alarm) {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(AlarmsContract.Alarms.ALARM_NAME, alarm.getName());
        values.put(AlarmsContract.Alarms.PLACE_NAME, alarm.getPlaceName());
        values.put(AlarmsContract.Alarms.VICINITY, alarm.getVicinity());
        values.put(AlarmsContract.Alarms.LAT, alarm.getLatLng().latitude);
        values.put(AlarmsContract.Alarms.LNG, alarm.getLatLng().longitude);
        values.put(AlarmsContract.Alarms.RANGE_DISTANCE, alarm.getRangeDistance());
        values.put(AlarmsContract.Alarms.IS_ENABLE, alarm.isEnable());
        values.put(AlarmsContract.Alarms.IMAGE_RESOURCE_ID, alarm.getMarkerColor());
        values.put(AlarmsContract.Alarms.IS_RINGING, alarm.isRinging);
        values.put(AlarmsContract.Alarms.IS_SNOOZED, alarm.isSnoozed);
        values.put(AlarmsContract.Alarms.SNOOZED_RANGE_DISTANCE, alarm.snoozedRangeDistance);
        values.put(AlarmsContract.Alarms.INSERTED_DATE, new Date().getTime());


        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(AlarmsContract.Alarms.TABLE_NAME, null, values);

        return newRowId;
    }

    public ArrayList<Alarm> getAlarms(AlarmsDbHelper dbHelper, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                AlarmsContract.Alarms.ALARM_NAME,
                AlarmsContract.Alarms.PLACE_NAME,
                AlarmsContract.Alarms.VICINITY,
                AlarmsContract.Alarms.LAT,
                AlarmsContract.Alarms.LNG,
                AlarmsContract.Alarms.RANGE_DISTANCE,
                AlarmsContract.Alarms.IS_ENABLE,
                AlarmsContract.Alarms.IMAGE_RESOURCE_ID,
                AlarmsContract.Alarms.IS_RINGING,
                AlarmsContract.Alarms.IS_SNOOZED,
                AlarmsContract.Alarms.SNOOZED_RANGE_DISTANCE,
                AlarmsContract.Alarms.INSERTED_DATE
        };

        // Filter results WHERE "title" = 'My Title'
        //String selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
        //String[] selectionArgs = {"My Title"};

        // How you want the results sorted in the resulting Cursor
        //String sortOrder = FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                AlarmsContract.Alarms.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<Alarm> Alarms = new ArrayList();

        while (cursor.moveToNext()) {
            Alarm a = new Alarm();

            a.setID(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms._ID)));
            a.setName(cursor.getString(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.ALARM_NAME)));
            a.setPlaceName(cursor.getString(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.PLACE_NAME)));
            a.setVicinity(cursor.getString(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.VICINITY)));

            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.LAT));
            double logitude = cursor.getDouble(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.LNG));
            LatLng latLng = new LatLng(latitude, logitude);
            a.setLatLng(latLng);

            a.setRangeDistance(cursor.getDouble(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.RANGE_DISTANCE)));
            a.setEnable(getBoolean(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.IS_ENABLE))));
            a.setMarkerColor(cursor.getFloat(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.IMAGE_RESOURCE_ID)));
            a.isRinging = getBoolean(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.IS_RINGING)));
            a.isSnoozed = getBoolean(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.IS_SNOOZED)));
            a.snoozedRangeDistance = cursor.getDouble(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.SNOOZED_RANGE_DISTANCE));
            a.setInsertedDate(getDate(cursor.getLong(cursor.getColumnIndexOrThrow(AlarmsContract.Alarms.INSERTED_DATE))));

            Alarms.add(a);
        }
        cursor.close();

        return Alarms;
    }

    public int deleteAlarm(AlarmsDbHelper dbHelper, int alarmID){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define 'where' part of query.
        String selection = AlarmsContract.Alarms._ID + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { alarmID + "" };

        // Issue SQL statement.
        int deletedRows = db.delete(AlarmsContract.Alarms.TABLE_NAME, selection, selectionArgs);

        return deletedRows;
    }

    public int updateAlarm(AlarmsDbHelper dbHelper, Alarm alarm){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(AlarmsContract.Alarms.ALARM_NAME, alarm.getName());
        values.put(AlarmsContract.Alarms.PLACE_NAME, alarm.getPlaceName());
        values.put(AlarmsContract.Alarms.VICINITY, alarm.getVicinity());
        values.put(AlarmsContract.Alarms.LAT, alarm.getLatLng().latitude);
        values.put(AlarmsContract.Alarms.LNG, alarm.getLatLng().longitude);
        values.put(AlarmsContract.Alarms.RANGE_DISTANCE, alarm.getRangeDistance());
        values.put(AlarmsContract.Alarms.IS_ENABLE, alarm.isEnable());
        values.put(AlarmsContract.Alarms.IMAGE_RESOURCE_ID, alarm.getMarkerColor());
        values.put(AlarmsContract.Alarms.IS_RINGING, alarm.isRinging);
        values.put(AlarmsContract.Alarms.IS_SNOOZED, alarm.isSnoozed);
        values.put(AlarmsContract.Alarms.SNOOZED_RANGE_DISTANCE, alarm.snoozedRangeDistance);
        values.put(AlarmsContract.Alarms.INSERTED_DATE, new Date().getTime());;

        // Which row to update, based on the title
        String selection = AlarmsContract.Alarms._ID + " = ?";
        String[] selectionArgs = { alarm.getID() + "" };

        int count = db.update(
                AlarmsContract.Alarms.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count;
    }
}
