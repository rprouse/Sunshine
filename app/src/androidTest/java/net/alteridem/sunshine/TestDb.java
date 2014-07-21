package net.alteridem.sunshine;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import net.alteridem.sunshine.data.WeatherContract.LocationEntry;
import net.alteridem.sunshine.data.WeatherContract.WeatherEntry;
import net.alteridem.sunshine.data.WeatherDbHelper;

public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        String testName = "North Pole";
        String testLocation = "99999";
        double testLatitude = 89.999;
        double testLongitude = -123.123;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocation);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long rowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        // Read
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        Cursor cursor = db.query( LocationEntry.TABLE_NAME, columns, null, null, null, null, null );

        if (cursor.moveToFirst()) {
            int i = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            assertEquals(testLocation, cursor.getString(i));

            i = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            assertEquals(testName, cursor.getString(i));

            i = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            assertEquals(testLatitude, cursor.getDouble(i));

            i = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            assertEquals(testLongitude, cursor.getDouble(i));
        } else {
            fail("Failed to read location database row");
        }

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, rowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        rowId = db.insert(WeatherEntry.TABLE_NAME, null, values);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        // Read
        String[] columns_w = {
                WeatherEntry._ID,
                WeatherEntry.COLUMN_LOC_KEY,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_WEATHER_ID
        };

        cursor = db.query( WeatherEntry.TABLE_NAME, columns_w, null, null, null, null, null );

        if (cursor.moveToFirst()) {

            int i = cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);
            assertEquals("20141205", cursor.getString(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES);
            assertEquals(1.1, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            assertEquals(1.2, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            assertEquals(1.3, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            assertEquals(75.0, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            assertEquals(65.0, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
            assertEquals("Asteroids", cursor.getString(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            assertEquals(5.5, cursor.getDouble(i));

            i = cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
            assertEquals(321, cursor.getInt(i));
        } else {
            fail("Failed to read weather database row");
        }
        dbHelper.close();
    }
}
