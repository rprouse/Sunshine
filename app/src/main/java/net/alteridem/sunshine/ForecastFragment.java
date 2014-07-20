package net.alteridem.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> _adapter;
    View _rootView;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.fragment_main, container, false);
        new DownloadForecastTask().execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=Hamilton,ON&mode=json&units=metric&cnt=7");
        return _rootView;
    }

    private void OnForecastDownloaded(String[] forecastArray) {

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        _adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast );

        ListView listView = (ListView)_rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(_adapter);
    }

    private class DownloadForecastTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = DownloadForecastTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(params[0]);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            String[] forecastArray = {
                    "Today - Sunny - 21° / 14°",
                    "Tomorrow - Rainy - 19° / 12°",
                    "Mon - Cloudy - 20° / 18°",
                    "Tue - Drizzle - 21° / 17°",
                    "Weds - Sunny - 28° / 16°",
                    "Thurs - Sunny - 30° / 19°",
                    "Fri - Partly Sunny - 28° / 16°",
                    "Sat - Sunny - 28° / 16°" };

            return forecastArray;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            ForecastFragment.this.OnForecastDownloaded(strings);
        }
    }
}
