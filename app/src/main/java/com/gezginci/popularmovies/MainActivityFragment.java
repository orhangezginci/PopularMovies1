package com.gezginci.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    static private ArrayList<Movie> movieArrayList = new ArrayList<>();
    final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"; //REPLACE THIS KEY
    final String TMDB_POPULAR_URL = "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
    final String TMDB_RATING_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
    final String MOVIES_KEY = "MOVIES";
    final String MOVIE_INTENT_OBJECT = "MOVIE";
    MovieAdapter movieAdapter;
    String orderBy;

    /*Check if user is connected with the internet*/
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public MainActivityFragment() {
    }

    /* if preferences is changed check if internet is connected */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences!=null) {
            if (key.equals(getString(R.string.pref_order_key))) {
                String orderBy = sharedPreferences.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_default));
                Button refresh = (Button) getActivity().findViewById(R.id.refresh);

                if(isOnline()) {
                    getMovies(orderBy);
                    refresh.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_internet , Toast.LENGTH_SHORT).show();
                    refresh.setVisibility(View.VISIBLE);
                    movieArrayList.clear();
                    movieAdapter.clear();
                }

                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                String popularity = getString(R.string.pref_order_popularity);

                if (orderBy != null && popularity != null) {
                    if (orderBy.equals(popularity) && actionBar != null) {
                        actionBar.setSubtitle(getString(R.string.subtitle_popularity));
                    } else if (actionBar != null) {
                        actionBar.setSubtitle(R.string.subtitle_rating);
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIES_KEY, movieArrayList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        orderBy = preferences.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_default));
        preferences.registerOnSharedPreferenceChangeListener(this);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null) {
            movieArrayList = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
        }

        movieAdapter = new MovieAdapter(getActivity(), R.layout.grid_cover_item, movieArrayList);

        GridView coverGrid;
        coverGrid = (GridView) rootView.findViewById(R.id.gridView);
        coverGrid.setAdapter(movieAdapter);

        coverGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                intent.putExtra(MOVIE_INTENT_OBJECT, movieAdapter.getItem(i));
                startActivity(intent);
            }
        });


        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        String popularity = getString(R.string.pref_order_popularity);

        if (orderBy != null && popularity != null) {
            if (orderBy.equals(popularity) && actionBar != null) {
                actionBar.setSubtitle(getString(R.string.subtitle_popularity));
            } else if (actionBar != null) {
                actionBar.setSubtitle(R.string.subtitle_rating);
            }
        }

        /*If there is no internet connection Toast message appears and and NO INTERNET - Refresh button*/
        if (movieArrayList.isEmpty())
            if(isOnline()) {
                getMovies(orderBy);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                Button refresh = (Button) rootView.findViewById(R.id.refresh);
                refresh.setVisibility(View.VISIBLE);
            }
        return rootView;
    }

    /*user can tap on the no internet icon and try to fetch data again*/
    public void onTryAgain(View view) {
        if(isOnline()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            orderBy = preferences.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_default));

            getMovies(orderBy);
            Button refresh = (Button) view.getRootView().findViewById(R.id.refresh);
            refresh.setVisibility(View.INVISIBLE);
        }
        else
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_internet , Toast.LENGTH_SHORT).show();
    }


    /* Start async task for searching*/
    public void getMovies(String order_by) {
        FetchMovieDataFromTMDB artistTask = new FetchMovieDataFromTMDB();
        artistTask.execute(order_by);
    }

    public class MovieAdapter extends ArrayAdapter<Movie> {

        public MovieAdapter(Context context, int textViewResourceId, ArrayList<Movie> items) {
            super(context, textViewResourceId, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            String coverUrl = getItem(position).coverUrl;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_cover_item, parent, false);
            }

            ImageView movieCoverIv = (ImageView) convertView.findViewById(R.id.cover_image);
            TextView movieRankTv = (TextView) convertView.findViewById(R.id.movie_rank);

            movieRankTv.setText(String.valueOf(getItem(position).rank));

            Picasso.with(getActivity())
                    .load(coverUrl)
                    .into(movieCoverIv);

            return convertView;
        }
    }

    /* Async function for fetching movie data from The Movie DB return type doInBackground:AArrayList<Movie>*/
    private class FetchMovieDataFromTMDB extends AsyncTask<String, Void, ArrayList<Movie>> {

        public ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            final String MOVIE_ITEMS = "results";
            final String MOVIE_ID = "id";
            final String ORIGINAL_TITLE = "original_title";
            final String POSTER_NR = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DAY = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String POSTER_PATH = "http://image.tmdb.org/t/p/w342/";

            ArrayList<Movie> movieList = new ArrayList<>();

            JSONObject moviesJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(MOVIE_ITEMS);

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieJson = movieArray.getJSONObject(i);
                String id = movieJson.get(MOVIE_ID).toString();
                String originalTitle = movieJson.get(ORIGINAL_TITLE).toString();
                String posterPath = POSTER_PATH + movieJson.get(POSTER_NR).toString();
                String plot = movieJson.get(OVERVIEW).toString();
                String releaseDate = movieJson.get(RELEASE_DAY).toString();
                float rating = movieJson.getLong(VOTE_AVERAGE);

                movieList.add(new Movie(id, originalTitle, posterPath, plot, i + 1, releaseDate, rating));
            }

            return movieList;
        }

        protected ArrayList<Movie> doInBackground(String... params) {
            if (params.length == 0)
                return null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                String TMDB_URL;

                if (params[0].equals(getString(R.string.pref_order_popularity)))
                    TMDB_URL = TMDB_POPULAR_URL;
                else
                    TMDB_URL = TMDB_RATING_URL;

                URL url = new URL(TMDB_URL);

                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("FetchMovieDataFromTMDB", e.getMessage(), e);
                Toast.makeText(getActivity().getApplicationContext(), R.string.error_retrieving_data, Toast.LENGTH_SHORT).show();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ERROR", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        /* put the result of doInBackground into movieArrayList*/
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            if (movieAdapter != null && movies != null) {
                movieArrayList.clear();
                movieAdapter.clear();
                movieArrayList.addAll(movies);
            }
        }
    }

}
