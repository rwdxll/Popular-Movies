package com.franktan.popularmovies.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franktan.popularmovies.R;
import com.franktan.popularmovies.data.movie.MovieColumns;
import com.franktan.popularmovies.data.movie.MovieCursor;
import com.franktan.popularmovies.data.review.ReviewColumns;
import com.franktan.popularmovies.data.review.ReviewCursor;
import com.franktan.popularmovies.data.trailer.TrailerColumns;
import com.franktan.popularmovies.data.trailer.TrailerCursor;
import com.franktan.popularmovies.service.MovieDetailsIntentService;
import com.franktan.popularmovies.util.Constants;
import com.franktan.popularmovies.util.Parser;
import com.franktan.popularmovies.util.Utilities;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private long mMovieDBId = -1;

    ImageView mMovieTrailer;
    ImageView mMoviePoster;
    TextView mMovieTitle;
    TextView mMovieReleaseDate;
    TextView mOriginalLanguage;
    TextView mRatingText;
    TextView mVoteCount;
    TextView mOverview;
    LinearLayout mReviewSection;
    LinearLayout mTrailerSection;

    private static final String[] MOVIE_COLUMNS = {
            MovieColumns.TABLE_NAME + "." + MovieColumns._ID,
            MovieColumns.TABLE_NAME + "." + MovieColumns.BACKDROP_PATH,
            MovieColumns.TABLE_NAME + "." + MovieColumns.ORIGINAL_LAN,
            MovieColumns.TABLE_NAME + "." + MovieColumns.OVERVIEW,
            MovieColumns.TABLE_NAME + "." + MovieColumns.RELEASE_DATE,
            MovieColumns.TABLE_NAME + "." + MovieColumns.POSTER_PATH,
            MovieColumns.TABLE_NAME + "." + MovieColumns.TITLE,
            MovieColumns.TABLE_NAME + "." + MovieColumns.VOTE_AVERAGE,
            MovieColumns.TABLE_NAME + "." + MovieColumns.VOTE_COUNT,
            ReviewColumns.TABLE_NAME + "." + ReviewColumns.AUTHOR,
            ReviewColumns.TABLE_NAME + "." + ReviewColumns.CONTENT,
            ReviewColumns.TABLE_NAME + "." + ReviewColumns.URL,
            TrailerColumns.TABLE_NAME + "." + TrailerColumns.TYPE,
            TrailerColumns.TABLE_NAME + "." + TrailerColumns.NAME,
            TrailerColumns.TABLE_NAME + "." + TrailerColumns.SIZE,
            TrailerColumns.TABLE_NAME + "." + TrailerColumns.SOURCE
    };

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mMovieTrailer       = (ImageView)   view.findViewById(R.id.movie_trailer);
        mMoviePoster        = (ImageView)   view.findViewById(R.id.movie_poster);
        mMovieTitle         = (TextView)    view.findViewById(R.id.movie_title);
        mMovieReleaseDate   = (TextView)    view.findViewById(R.id.release_date);
        mOriginalLanguage   = (TextView)    view.findViewById(R.id.original_language);
        mRatingText         = (TextView)    view.findViewById(R.id.rating_text);
        mVoteCount          = (TextView)    view.findViewById(R.id.vote_count);
        mOverview           = (TextView)    view.findViewById(R.id.overview);
        mReviewSection      = (LinearLayout)view.findViewById(R.id.review_section);
        mTrailerSection     = (LinearLayout)view.findViewById(R.id.trailer_section);

        YouTubeThumbnailView youTubeThumbnailView =
                (YouTubeThumbnailView) view.findViewById(R.id.youtube_thumbnail);
        youTubeThumbnailView.initialize(Utilities.getGoogleApiKey(getActivity()), new TrailerThumbnailListener());

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Intent intent = activity.getIntent();
        mMovieDBId = intent.getLongExtra(Constants.MOVIEDB_ID,-1);
        Log.i(Constants.APP_NAME, "mMovieDBId from intent is " + mMovieDBId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(mMovieDBId != -1) {
            // Invoke IntentService to retrieve reviews and trailers
            startMovieDetailsIntentService();

            // Query database
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                Uri.withAppendedPath(MovieColumns.CONTENT_URI, "moviedb/" + String.valueOf(mMovieDBId)),
                MOVIE_COLUMNS,
                MovieColumns.TABLE_NAME + "." + MovieColumns.MOVIE_MOVIEDB_ID + "="+ String.valueOf(mMovieDBId),
                null,
                null
        );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //nothing needed
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(Constants.APP_NAME,"loader finished");
        if (cursor != null && !cursor.moveToFirst()){
            Log.i(Constants.APP_NAME,"loader finished: no records returned");
            return;
        }
        Log.i(Constants.APP_NAME, String.valueOf(cursor.getCount()) + "  records returned");

        MovieCursor movieCursor = new MovieCursor(cursor);
        movieCursor.moveToFirst();
        String backdropPath = Constants.BACKDROP_BASE_PATH + movieCursor.getBackdropPath();
        String posterPath   = Constants.POSTER_BASE_PATH + movieCursor.getPosterPath();
        String title        = movieCursor.getTitle();
        long releaseDate    = movieCursor.getReleaseDate();
        String language     = movieCursor.getOriginalLan();
        Double voteAverage  = movieCursor.getVoteAverage();
        int voteCount       = movieCursor.getVoteCount();
        String overview     = movieCursor.getOverview();
        
        Picasso.with(getActivity())
                .load(backdropPath)
                .placeholder(R.drawable.backdrop_loading_placeholder)
                .error(R.drawable.backdrop_failed_placeholder)
                .fit()
                .centerCrop()
                .into(mMovieTrailer);
        Picasso.with(getActivity())
                .load(posterPath)
                .placeholder(R.drawable.poster_loading_placeholder)
                .error(R.drawable.poster_failed_placeholder)
                .fit()
                .centerCrop()
                .into(mMoviePoster);
        mMovieTitle.setText(title);
        mMovieReleaseDate.setText(Parser.humanDateStringFromMiliseconds(releaseDate));
        mOriginalLanguage.setText(language);
        mRatingText.setText(String.valueOf(voteAverage) + "/10");
        mVoteCount.setText(String.valueOf(voteCount) + " votes");
        mOverview.setText(overview);

        ReviewCursor reviewCursor = new ReviewCursor(cursor);
        showAllReviewRecords(reviewCursor);

        TrailerCursor trailerCursor = new TrailerCursor(cursor);
        showAllTrailerRecords(trailerCursor);
    }

    private void showAllTrailerRecords(TrailerCursor trailerCursor) {
        Set<String> trailerSet = new HashSet<String>();
        trailerCursor.moveToFirst();
        do {
            try {
                if(trailerSet.add(trailerCursor.getSource())) {
                    //// TODO: 4/09/2015 need to finalize the way to show trailer
                    Log.i(Constants.APP_NAME, trailerCursor.getName());
                    Log.i(Constants.APP_NAME, trailerCursor.getSize());
                    Log.i(Constants.APP_NAME, trailerCursor.getSource());
                    Log.i(Constants.APP_NAME, trailerCursor.getType());
                }
            } catch (NullPointerException e){}
        } while (trailerCursor.moveToNext());
    }

    private void showAllReviewRecords(ReviewCursor reviewCursor) {
        mReviewSection.removeAllViews();

        Set<String> reviewSet = new HashSet<String>();
        reviewCursor.moveToFirst();
        do {
            try {
                if(reviewSet.add(reviewCursor.getUrl()))
                    showCurrentReviewRecord(reviewCursor);
            } catch (NullPointerException e){}
        } while (reviewCursor.moveToNext());

        if(mReviewSection.getChildCount() > 0) {
            TextView reviewTitle = new TextView(getActivity());
            reviewTitle.setText(getString(R.string.reviews_title));
            ViewGroup.LayoutParams textViewLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            reviewTitle.setLayoutParams(textViewLayoutParams);
            reviewTitle.setTextAppearance(getActivity(),R.style.Base_TextAppearance_AppCompat_Large);

            mReviewSection.addView(reviewTitle,0);
        } else {
            TextView reviewTitle = new TextView(getActivity());
            reviewTitle.setText(getString(R.string.no_reviews_available));
            ViewGroup.LayoutParams textViewLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            reviewTitle.setLayoutParams(textViewLayoutParams);

            mReviewSection.addView(reviewTitle);
        }
    }

    private void showCurrentReviewRecord(ReviewCursor reviewCursor) {
        View reviewItemView = getActivity().getLayoutInflater().inflate(R.layout.review_item, null);
        TextView authorTextView = (TextView) reviewItemView.findViewById(R.id.review_author);
        TextView contentTextView = (TextView) reviewItemView.findViewById(R.id.review_text);
        mReviewSection.addView(reviewItemView);

        authorTextView.setText(reviewCursor.getAuthor());
        contentTextView.setText(reviewCursor.getContent());
    }

    public void showDetailsByMovieDBId(long movieDBId) {
        mMovieDBId = movieDBId;
        startMovieDetailsIntentService();
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    private void startMovieDetailsIntentService () {
        Context appContext = getActivity().getApplicationContext();
        Intent intent = new Intent(appContext, MovieDetailsIntentService.class);
        intent.putExtra(Constants.MOVIEDB_ID, mMovieDBId);
        appContext.startService(intent);
    }

    private final class TrailerThumbnailListener implements
            YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            //thumbnailViewToLoaderMap.put(view, loader);
            //// TODO: 5/09/2015: load a place holder image
            //view.setImageResource(R.drawable.loading_thumbnail);
            //// TODO: 5/09/2015 remove hardcoded video id
            loader.setVideo("YWNWi-ZWL3c");
        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view, YouTubeInitializationResult loader) {
            //// TODO: 5/09/2015: load backdrop image
            //view.setImageResource(R.drawable.no_thumbnail);
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
            // // TODO: 5/09/2015 show play button
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
            //// TODO: 5/09/2015: load backdrop image
            //view.setImageResource(R.drawable.no_thumbnail);
        }
    }

}
