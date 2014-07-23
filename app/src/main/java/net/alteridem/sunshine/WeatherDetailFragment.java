package net.alteridem.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.alteridem.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = WeatherDetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mDate;

    private static final int DETAILS_LOADER = 1;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_CITY = 5;
    public static final int COL_WEATHER_HUMIDITY = 6;
    public static final int COL_WEATHER_WIND = 7;
    public static final int COL_WEATHER_DIRECTION = 8;
    public static final int COL_WEATHER_PRESSURE = 9;

    private TextView mDateTextView;
    private TextView mDescriptionTextView;
    private TextView mMin;
    private TextView mMax;
    private TextView mHumidity;
    private TextView mWind;
    private TextView mPressure;

    public WeatherDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDateTextView = (TextView)rootView.findViewById(R.id.fragment_detail_date);
        mDescriptionTextView = (TextView)rootView.findViewById(R.id.fragment_detail_description);
        mMax = (TextView)rootView.findViewById(R.id.fragment_detail_max);
        mMin = (TextView)rootView.findViewById(R.id.fragment_detail_min);
        mHumidity = (TextView)rootView.findViewById(R.id.fragment_detail_humidity);
        mWind = (TextView)rootView.findViewById(R.id.fragment_detail_wind);
        mPressure = (TextView)rootView.findViewById(R.id.fragment_detail_pressure);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mDate = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DETAILS_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if( shareProvider != null ) {
            shareProvider.setShareIntent(createShareWeatherIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createShareWeatherIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, mDate + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String location = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, mDate);

        Log.d(LOG_TAG, "Uri: " + weatherForLocationUri.toString());

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor.moveToFirst()) {
            boolean isMetric = Utility.isMetric(getActivity());
            getActivity().setTitle(cursor.getString(COL_WEATHER_CITY));
            mDateTextView.setText(Utility.formatDate(cursor.getString(COL_WEATHER_DATE)));
            mDescriptionTextView.setText(cursor.getString(COL_WEATHER_DESC));
            mMax.setText(Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
            mMin.setText(Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
            mHumidity.setText(Utility.formatHumidity(cursor.getDouble(COL_WEATHER_HUMIDITY)));
            mWind.setText(Utility.formatWind(cursor.getDouble(COL_WEATHER_WIND), isMetric, cursor.getDouble(COL_WEATHER_DIRECTION)));
            mPressure.setText(Utility.formatPressure(cursor.getDouble(COL_WEATHER_PRESSURE), isMetric));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getActivity().setTitle("");
        mDateTextView.setText("");
        mDescriptionTextView.setText("");
        mMax.setText("");
        mMin.setText("");
        mHumidity.setText("");
        mWind.setText("");
        mPressure.setText("");
    }
}
