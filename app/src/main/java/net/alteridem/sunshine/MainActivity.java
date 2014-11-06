package net.alteridem.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.alteridem.sunshine.sync.WeatherSyncAdapter;

public class MainActivity extends ActionBarActivity implements IForecastFragmentHost {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, new WeatherDetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        if (forecastFragment != null)
            forecastFragment.setUseTodayLayout(!mTwoPane);

        WeatherSyncAdapter.initializeSyncAdapter(this);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(WeatherDetailFragment.DATE_KEY, date);

            WeatherDetailFragment fragment = new WeatherDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(WeatherDetailFragment.DATE_KEY, date);
            startActivity(intent);
        }
    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }
}
