package net.alteridem.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.alteridem.sunshine.data.WeatherContract;
import net.alteridem.sunshine.sync.WeatherSyncAdapter;

import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter mAdapter;
    private View mRootView;
    private boolean mUseTodaylayout;

    private static final int FORECAST_LOADER = 0;
    private static final String LIST_POSITION = "LIST_POSITION";
    private int mPosition = 0;
    private String mLocation;
    private ListView mListView;

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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!getLoaderManager().hasRunningLoaders() && getLoaderManager().getLoader(FORECAST_LOADER) == null)
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        else
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_POSITION))
            mPosition = savedInstanceState.getInt(LIST_POSITION);

        mAdapter = new ForecastAdapter(getActivity(), null, 0);
        mAdapter.setUseTodayLayout(mUseTodaylayout);

        mListView = (ListView) mRootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CursorAdapter adapter = (CursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String date = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
                    IForecastFragmentHost callback = (IForecastFragmentHost) getActivity();
                    callback.onItemSelected(date);
                }
                mPosition = position;
            }
        });
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_view_location) {
            viewLocationOnMap();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewLocationOnMap() {
        String uri;
        if (mAdapter != null && mAdapter.getCursor() != null) {
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToFirst();
            double latitude = cursor.getDouble(COL_COORD_LAT);
            double longitude = cursor.getDouble(COL_COORD_LONG);
            uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", location);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + uri);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(LIST_POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    private void updateWeather() {
        WeatherSyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodaylayout = useTodayLayout;
        if (mAdapter != null)
            mAdapter.setUseTodayLayout(mUseTodaylayout);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Only return data after today
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort ascending by date
        String sort = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());

        updateWeather();

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        Log.d(LOG_TAG, "Uri: " + weatherForLocationUri.toString());

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sort
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION && ((IForecastFragmentHost) getActivity()).isTwoPane()) {
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(mPosition);
                    mListView.setSelection(mPosition);
                    mListView.performItemClick(mListView.getChildAt(0), mPosition, mPosition);

                }
            }, 100);
        }

        if (!mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
