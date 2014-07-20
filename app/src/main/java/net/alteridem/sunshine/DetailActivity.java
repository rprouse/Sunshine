package net.alteridem.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import java.util.Locale;

public class DetailActivity extends ActionBarActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherDetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewLocationOnMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", location);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(intent.resolveActivity(getPackageManager()) != null ) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WeatherDetailFragment extends Fragment {

        private static final String LOG_TAG = WeatherDetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String _forecast;

        public WeatherDetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                TextView textView = (TextView)rootView.findViewById(R.id.fragment_detail_textview);
                _forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
                textView.setText(_forecast);
            }
            return rootView;
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
            intent.putExtra(Intent.EXTRA_TEXT, _forecast + FORECAST_SHARE_HASHTAG);
            return intent;
        }

    }
}
