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

    static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
    static final String KALAMAZOO_WEATHER_START_DATE = "20140625";

    long locationRowId;

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

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, updatedValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    static ContentValues createKalamazooWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, KALAMAZOO_WEATHER_START_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

        return weatherValues;
    }

    static ContentValues createKalamazooLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, KALAMAZOO_LOCATION_SETTING);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "Kalamazoo");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 42.2917);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -85.5872);

        return testValues;
    }


    // Inserts both the location and weather data for the Kalamazoo data set.
    public void insertKalamazooData() {
        ContentValues kalamazooLocationValues = createKalamazooLocationValues();
        Uri locationInsertUri = mContext.getContentResolver()
                .insert(LocationEntry.CONTENT_URI, kalamazooLocationValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);

        ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
        assertTrue(weatherInsertUri != null);
    }

    public void testUpdateAndReadWeather() {
        insertKalamazooData();
        String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

        // Make an update to one value.
        ContentValues kalamazooUpdate = new ContentValues();
        kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        mContext.getContentResolver().update(
                WeatherEntry.CONTENT_URI, kalamazooUpdate, null, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
    }

    public void testRemoveHumidityAndReadWeather() {
        insertKalamazooData();

        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
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
