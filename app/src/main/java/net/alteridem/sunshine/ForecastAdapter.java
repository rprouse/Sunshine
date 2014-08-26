package net.alteridem.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastAdapter extends CursorAdapter {
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = viewType == VIEW_TYPE_TODAY ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        String date = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);

        boolean isMetric = Utility.isMetric(context);

        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        setTextView(view, R.id.list_item_date_textview, Utility.getFriendlyDayString(context, date));
        setTextView(view, R.id.list_item_forecast_textview, desc);
        setTextView(view, R.id.list_item_high_textview, Utility.formatTemperature(high, isMetric));
        setTextView(view, R.id.list_item_low_textview, Utility.formatTemperature(low, isMetric));
    }

    private void setTextView(View view, int id, String text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView != null) {
            textView.setText(text);
        }
    }
}
