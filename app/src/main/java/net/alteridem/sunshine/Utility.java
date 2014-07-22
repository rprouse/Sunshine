package net.alteridem.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    public static String getPreferredLocation(Context context) {
        return getSharedPreference(context, R.string.pref_location_key, R.string.pref_location_default);
    }

    public static String getPreferredUnits(Context context) {
        return getSharedPreference(context, R.string.pref_units_key, R.string.pref_units_default);
    }

    public static String getSharedPreference(Context context, int resourceId, int defaultValueId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(resourceId), context.getString(defaultValueId));
    }
}
