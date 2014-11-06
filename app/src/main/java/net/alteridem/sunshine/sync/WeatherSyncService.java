package net.alteridem.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WeatherSyncService extends Service {

    private static final String TAG = WeatherSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static WeatherSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate - WeatherSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
