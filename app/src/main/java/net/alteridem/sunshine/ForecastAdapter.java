package net.alteridem.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ForecastItemViewHolder(view));
        return view;
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

        ForecastItemViewHolder viewHolder = (ForecastItemViewHolder) view.getTag();

        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, date));
        viewHolder.descriptionView.setText(desc);
        viewHolder.highView.setText(Utility.formatTemperature(context, high, isMetric));
        viewHolder.lowView.setText(Utility.formatTemperature(context, low, isMetric));
    }
}
