package net.alteridem.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class WeatherAuthenticatorService extends Service {

    private WeatherAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new WeatherAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
