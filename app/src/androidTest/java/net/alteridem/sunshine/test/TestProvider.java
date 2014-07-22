package net.alteridem.sunshine.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import net.alteridem.sunshine.data.WeatherContract.LocationEntry;
import net.alteridem.sunshine.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();
    private static final String LOCATION_ONE = "location 1";
    private static final String LOCATION_TWO = "location 2";
    private static final String LOCATION_THREE = "location 3";

    public void testInsertReadProvider() throws Throwable {

        ContentValues testValues = TestDb.getLocationContentValues(LOCATION_ONE);

        Uri returnUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(returnUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null  // columns to group by
        );

        assertTrue(cursor.moveToFirst());
        cursor.close();

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.getWeatherContentValues(locationRowId);

        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        ContentUris.parseId(weatherUri);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        assertTrue(weatherCursor.moveToFirst());
        weatherCursor.close();

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(LOCATION_ONE),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(LOCATION_ONE, TestDb.DATETEXT),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(LOCATION_ONE, TestDb.DATETEXT),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    public void testDelete() {
        // Add a record
        ContentValues testValues = TestDb.getLocationContentValues(LOCATION_TWO);

        Uri returnUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(returnUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Now see if we can successfully query if we include the row id
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.getWeatherContentValues(locationRowId);

        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        ContentUris.parseId(weatherUri);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(LOCATION_TWO),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // Now delete the records
        mContext.getContentResolver().delete(
                WeatherEntry.buildWeatherLocation(LOCATION_TWO),
                null, // cols for "where" clause
                null // values for "where" clause
        );

        // Now try to fetch it again
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(LOCATION_TWO),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        // We shouldn't find it
        assertFalse(weatherCursor.moveToFirst());
        weatherCursor.close();

        mContext.getContentResolver().delete(
                LocationEntry.buildLocationUri(locationRowId),
                null, // cols for "where" clause
                null // values for "where" clause
        );

        // Now try to fetch it again
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // We shouldn't find it
        assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    public void testUpdate() {
        // Add a record
        ContentValues testValues = TestDb.getLocationContentValues(LOCATION_THREE);

        Uri returnUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(returnUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.getWeatherContentValues(locationRowId);

        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        ContentUris.parseId(weatherUri);

        ContentValues locationUpdate = new ContentValues();
        locationUpdate.put(LocationEntry.COLUMN_CITY_NAME, "South Pole");

        // Now update the records
        mContext.getContentResolver().update(
                WeatherEntry.buildWeatherLocation(LOCATION_THREE),
                locationUpdate,
                null, // cols for "where" clause
                null // values for "where" clause
        );

        ContentValues weatherUpdate = new ContentValues();
        weatherUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, "Meteors");

        mContext.getContentResolver().update(
                LocationEntry.buildLocationUri(locationRowId),
                weatherUpdate,
                null, // cols for "where" clause
                null // values for "where" clause
        );

        // Test the updates
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        assertTrue(cursor.moveToFirst());
        int i = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
        String city = cursor.getString(i);
        assertEquals("South Pole", city);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(LOCATION_THREE),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        assertTrue(cursor.moveToFirst());
        i = cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
        String desc = cursor.getString(i);
        assertEquals("Meteors", desc);
        cursor.close();
    }

    public void testGetType() {
        // content://net.alteridem.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/net.alteridem.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://net.alteridem.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/net.alteridem.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://net.alteridem.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/net.alteridem.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://net.alteridem.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/net.alteridem.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://net.alteridem.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/net.alteridem.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
