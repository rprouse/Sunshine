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
import android.widget.ImageView;
import android.widget.TextView;

import net.alteridem.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = WeatherDetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String LOCATION_KEY = "location";
    private String mDate;

    private static final int DETAILS_LOADER = 1;
    private String mLocation;
    private String mForecast;

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

    public WeatherDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(LOCATION_KEY)) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mDate = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_KEY, mLocation);
    }



    @Override
    public void onResume() {
        super.onResume();
        if( mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAILS_LOADER, null, this);
        }
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
        intent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, mDate);

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
            ImageView iconView = (ImageView) getView().findViewById(R.id.fragment_detail_icon);
            TextView friendlyDateTextView = (TextView) getView().findViewById(R.id.fragment_detail_friendly_date);
            TextView dateTextView = (TextView)getView().findViewById(R.id.fragment_detail_date);
            TextView descriptionTextView = (TextView)getView().findViewById(R.id.fragment_detail_description);
            TextView maxTextView = (TextView)getView().findViewById(R.id.fragment_detail_max);
            TextView minTextView = (TextView)getView().findViewById(R.id.fragment_detail_min);
            TextView humidityTextView = (TextView)getView().findViewById(R.id.fragment_detail_humidity);
            TextView windTextView = (TextView)getView().findViewById(R.id.fragment_detail_wind);
            TextView pressureTextView = (TextView)getView().findViewById(R.id.fragment_detail_pressure);

            boolean isMetric = Utility.isMetric(getActivity());
            String date = cursor.getString(COL_WEATHER_DATE);
            String desc = cursor.getString(COL_WEATHER_DESC);
            String low = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            String high = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            getActivity().setTitle(cursor.getString(COL_WEATHER_CITY));
            iconView.setImageResource(R.drawable.ic_launcher);
            friendlyDateTextView.setText(Utility.getDayName(getActivity(), date));
            dateTextView.setText(Utility.getFormattedMonthDay(getActivity(), date));
            descriptionTextView.setText(desc);
            maxTextView.setText(high);
            minTextView.setText(low);
            humidityTextView.setText(Utility.formatHumidity(getActivity(), cursor.getDouble(COL_WEATHER_HUMIDITY)));
            windTextView.setText(Utility.formatWind(getActivity(), cursor.getDouble(COL_WEATHER_WIND), isMetric, cursor.getDouble(COL_WEATHER_DIRECTION)));
            pressureTextView.setText(Utility.formatPressure(getActivity(), cursor.getDouble(COL_WEATHER_PRESSURE), isMetric));

            mForecast = String.format("%s - %s - %s/%s", date, desc, high, low);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
