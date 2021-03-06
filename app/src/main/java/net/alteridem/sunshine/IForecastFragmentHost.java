package net.alteridem.sunshine;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface IForecastFragmentHost {
    /**
     * Callback for when an item has been selected.
     */
    public void onItemSelected(String date);

    /**
     * Returns true if this is a two pane view
     *
     * @return
     */
    public boolean isTwoPane();
}
