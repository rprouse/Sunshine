package net.alteridem.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.alteridem.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static String formatTemperature(Context context, double temp, boolean isMetric) {
        if (!isMetric) {
            temp = 9 * temp / 5 + 32;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            return context.getString(
                    R.string.format_full_friendly_date,
                    today,
                    getFormattedMonthDay(context, dateStr));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return A friendly formatted date string
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(WeatherContract.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (WeatherContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(WeatherContract.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            return monthDayFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatHumidity(Context context, double humidity) {
        return context.getString(R.string.format_humidity, humidity);
    }

    public static String formatWind(Context context, double speed, boolean isMetric, double angle) {
        String dir = formatDirection(angle);
        if (isMetric) {
            return context.getString(R.string.format_wind_kmh, speed, dir);
        }
        return context.getString(R.string.format_wind_mph, speed / 1.6, dir);
    }

    public static String formatPressure(Context context, double pressure, boolean isMetric) {
        return context.getString(R.string.format_pressure, pressure);
    }

    private static String formatDirection(double angle) {
        if (angle > 337.5 || angle < 22.5) {
            return "N";
        } else if (angle < 67.5) {
            return "NE";
        } else if (angle < 112.5) {
            return "E";
        } else if (angle < 157.5) {
            return "SE";
        } else if (angle < 202.5) {
            return "S";
        } else if (angle < 247.5) {
            return "SW";
        } else if (angle < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    /**
     * Helper method to provide the wearable background resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getBackgroundResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.rain;
        } else if (weatherId == 511) {
            return R.drawable.snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.storm;
        } else if (weatherId == 800) {
            return R.drawable.clear;
        } else if (weatherId == 801) {
            return R.drawable.light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.clouds;
        }
        return -1;
    }
}
