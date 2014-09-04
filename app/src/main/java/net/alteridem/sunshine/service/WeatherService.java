package net.alteridem.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeatherService extends IntentService {

    private static final String TAG = WeatherService.class.getSimpleName();
    public static final String LOCATION_QUERY_EXTRA = "location_query_extra";

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    static public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String location = intent.getStringExtra(LOCATION_QUERY_EXTRA);
            Intent i = new Intent(context, WeatherService.class);
            i.putExtra(WeatherService.LOCATION_QUERY_EXTRA, location);
            context.startService(i);
        }
    }
}
