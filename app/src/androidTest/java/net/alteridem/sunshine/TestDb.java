package net.alteridem.sunshine;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import net.alteridem.sunshine.data.WeatherContract.LocationEntry;
import net.alteridem.sunshine.data.WeatherContract.WeatherEntry;
import net.alteridem.sunshine.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert
        ContentValues values = getLocationContentValues();

        long rowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        Cursor cursor = db.query( LocationEntry.TABLE_NAME, null, null, null, null, null, null );

        validateCursor(cursor, values);

        // Fantastic.  Now that we have a location, add some weather!
        values = getWeatherContentValues(rowId);

        rowId = db.insert(WeatherEntry.TABLE_NAME, null, values);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        cursor = db.query( WeatherEntry.TABLE_NAME, null, null, null, null, null, null );

        validateCursor(cursor, values);

        dbHelper.close();
    }

    private ContentValues getWeatherContentValues(long rowId) {
        ContentValues values = new ContentValues();
        values.put(WeatherEntry.COLUMN_LOC_KEY, rowId);
        values.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        values.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        values.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        values.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        values.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        values.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        values.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        values.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        values.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return values;
    }

    private ContentValues getLocationContentValues() {
        ContentValues values = new ContentValues();
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);
        return values;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
