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

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDbDateString(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static String formatHumidity(double humidity) {
        return String.format("%.0f%%", humidity);
    }

    public static String formatWind(double speed, boolean isMetric, double angle) {
        String dir = formatDirection(angle);
        if(isMetric){
            return String.format("%.0f km/h %s", speed, dir);
        }
        return String.format("%.0f mph %s", speed / 1.6, dir);
    }

    public static String formatPressure(double pressure, boolean isMetric) {
        return String.format("%.0f hPa", pressure);
    }

    private static String formatDirection(double angle) {
        if (angle > 337.5 || angle < 22.5) {
            return "N";
        } else if (angle < 67.5) {
            return "NE";
        } else if ( angle < 112.5) {
            return "E";
        } else if ( angle < 157.5) {
            return "SE";
        } else if ( angle < 202.5) {
            return "S";
        } else if ( angle < 247.5) {
            return "SW";
        } else if ( angle < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }
}
