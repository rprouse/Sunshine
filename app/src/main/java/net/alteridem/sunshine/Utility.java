package net.alteridem.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.alteridem.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

public class Utility {
    public static String getPreferredLocation(Context context) {
        return getSharedPreference(context, R.string.pref_location_key, R.string.pref_location_default);
    }

    public static Boolean isMetric(Context context) {
        String units = getSharedPreference(context, R.string.pref_units_key, R.string.pref_units_default);
        return units.equalsIgnoreCase("metric");
    }

    public static String getSharedPreference(Context context, int resourceId, int defaultValueId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(resourceId), context.getString(defaultValueId));
    }

    public static String formatTemperature(double temp, boolean isMetric) {
        if(!isMetric) {
            temp = 9 * temp / 5 + 32;
        }
        return String.format("%.0f°", temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDbDateString(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
