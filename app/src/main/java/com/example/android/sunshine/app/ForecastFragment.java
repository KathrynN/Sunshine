package com.example.android.sunshine.app;

import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
/**
 * Created by Kathr on 14/01/2016.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ForecastAdapter mForecastAdapter;
    private String location = "";
    private static final String LOG_TAG = "Sunshine Forecast";
    private static int Weather_Loader_ID = 100;
        public ForecastFragment() {
        }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public void onStart(){
        super.onStart();
        updateWeather();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    private void updateWeather(){

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id== R.id.action_refresh){
            updateWeather();
            return true;
        }else if(id==R.id.viewmap){
            // location = (PreferenceManager.getDefaultSharedPreferences(getActivity())).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            PackageManager packageManager = getActivity().getPackageManager();
            Uri uriMap = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            mapIntent.setData(uriMap);
            if(mapIntent.resolveActivity(packageManager)!=null){
                startActivity(mapIntent);
            }else{
                Log.d(LOG_TAG, "No intent able to handle location query");
            }
        }
        return super.onOptionsItemSelected(item);
    }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            String locationSetting = Utility.getPreferredLocation(getActivity());

            // Sort order:  Ascending, by date.
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                    null, null, null, sortOrder);

            mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);


            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            // Get a reference to the ListView, and attach this adapter to it.
            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    //Toast toast = Toast.makeText(getActivity(), mForecastAdapter.getItem(i), Toast.LENGTH_SHORT);
//                    //toast.show();
//                    Intent intent = new Intent(getActivity(), DetailActivity.class);
//                    intent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(i));
//                    startActivity(intent);
//                }
//            });


            return rootView;
        }
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        Uri weatherContentWithLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (Utility.getPreferredLocation(getActivity()), System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherContentWithLocation,
                FORECAST_COLUMNS, null, null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
    }
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor){
        mForecastAdapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader){
        mForecastAdapter.swapCursor(null);
    }

}
