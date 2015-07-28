package com.gezginci.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {

    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String MOVIE_INTENT_OBJECT = "MOVIE";

        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);


        Movie movie = new Movie(((Movie) getActivity().getIntent().getParcelableExtra(MOVIE_INTENT_OBJECT)));

        TextView movieNameTv = (TextView) rootView.findViewById(R.id.header_movie_name);
        TextView moviePloTv = (TextView) rootView.findViewById(R.id.plot);
        TextView releaseDateTv = (TextView) rootView.findViewById(R.id.release_date);
        TextView ratingTv = (TextView) rootView.findViewById(R.id.movie_rating);
        ImageView moviePosterIv = (ImageView) rootView.findViewById(R.id.movie_poster);
        RatingBar ratingBarRb = (RatingBar) rootView.findViewById(R.id.ratingBar);


        movieNameTv.setText(movie.name);
        moviePloTv.setText(movie.plot);

        releaseDateTv.setText(getString(R.string.release_date) + movie.releaseDate);
        ratingBarRb.setRating(movie.rating);
        ratingTv.setText(Float.toString(movie.rating));


        Picasso.with(getActivity())
                .load(movie.coverUrl)
                .into(moviePosterIv);


        return rootView;
    }
}
