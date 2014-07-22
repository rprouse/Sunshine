package net.alteridem.sunshine.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import net.alteridem.sunshine.data.WeatherContract.LocationEntry;
import net.alteridem.sunshine.data.WeatherContract.WeatherEntry;
import net.alteridem.sunshine.data.WeatherDbHelper;

public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }


    public void testInsertReadProvider() throws Throwable {

        ContentValues testValues = TestDb.getLocationContentValues();

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

        TestDb.validateCursor(cursor, testValues);

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

        TestDb.validateCursor(weatherCursor, weatherValues);

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.LOCATION_SETTING),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TestDb.LOCATION_SETTING, TestDb.DATETEXT),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // A cursor is your primary interface to the query results.
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TestDb.LOCATION_SETTING, TestDb.DATETEXT),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);
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
