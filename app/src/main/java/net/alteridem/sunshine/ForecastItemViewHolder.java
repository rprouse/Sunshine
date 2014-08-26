package net.alteridem.sunshine;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastItemViewHolder {
    public final ImageView iconView;
    public final TextView dateView;
    public final TextView descriptionView;
    public final TextView highView;
    public final TextView lowView;

    public ForecastItemViewHolder(View view) {
        iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
    }
}
