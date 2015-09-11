package com.franktan.popularmovies.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.franktan.popularmovies.R;
import com.franktan.popularmovies.data.favorite.FavoriteColumns;
import com.franktan.popularmovies.data.favorite.FavoriteContentValues;
import com.franktan.popularmovies.data.favorite.FavoriteCursor;
import com.franktan.popularmovies.data.favorite.FavoriteSelection;
import com.franktan.popularmovies.data.movie.MovieColumns;
import com.franktan.popularmovies.data.movie.MovieCursor;
import com.franktan.popularmovies.util.Constants;
import com.franktan.popularmovies.util.Utilities;
import com.squareup.picasso.Picasso;

/**
 * Created by tan on 12/08/2015.
 */
public class MovieGridAdapter extends CursorAdapter {
    Context mContext;

    public MovieGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.grid_element, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        MovieCursor movieCursor = new MovieCursor(cursor);
        String posterPath = movieCursor.getPosterPath();
        String posterFullPath = Constants.POSTER_BASE_PATH + posterPath;

        FavoriteCursor favoriteCursor = new FavoriteCursor(cursor);
        boolean isFavorite = false;

        try {
            if(favoriteCursor.getId() >= 0)
                isFavorite = true;
        } catch (NullPointerException e) {}

        viewHolder.favoriteCheckbox.setChecked(isFavorite);
        viewHolder.favoriteCheckbox.setTag(movieCursor.getMovieMoviedbId());
        viewHolder.favoriteCheckbox.setOnClickListener(createFavoriteCheckboxOnClickListener());

        Picasso.with(context)
                .load(posterFullPath)
                .placeholder(R.drawable.poster_loading_placeholder)
                .error(R.drawable.poster_failed_placeholder)
                .fit()
                .centerCrop()
                .into(viewHolder.posterImage);
    }

    private static class ViewHolder {
        public final ImageView posterImage;
        public final CheckBox favoriteCheckbox;


        public ViewHolder(View view) {
            posterImage = (ImageView) view.findViewById(R.id.poster_image);
            favoriteCheckbox = (CheckBox) view.findViewById(R.id.favorite_checkbox);
        }
    }

//    public int getCount() {
//        return 10;
//    }

//    public Object getItem(int position) {
//        return ;
//    }

    public long getItemId(int position) {
        if(mCursor != null) {
            if(mCursor.moveToPosition(position)) {
                MovieCursor movieCursor = new MovieCursor(mCursor);
                return movieCursor.getMovieMoviedbId();
            }
        }
        return 0;
    }

    private View.OnClickListener createFavoriteCheckboxOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long movieDBId = (long) v.getTag();
                CheckBox favoriteCheckbox = (CheckBox) v;

                if (favoriteCheckbox.isChecked()) {
                    FavoriteContentValues favoriteContentValues = new FavoriteContentValues();
                    favoriteContentValues.putFavoriteMoviedbId(movieDBId);
                    favoriteContentValues.putCreated(Utilities.getCurrentTimeInMillis());
                    mContext.getContentResolver().insert(FavoriteColumns.CONTENT_URI, favoriteContentValues.values());
                } else {
                    FavoriteSelection selection = new FavoriteSelection();
                    selection.favoriteMoviedbId(movieDBId);
                    mContext.getContentResolver().delete(FavoriteColumns.CONTENT_URI, selection.sel(), selection.args());
                }
                mContext.getApplicationContext().getContentResolver().notifyChange(Uri.withAppendedPath(MovieColumns.CONTENT_URI, "with_favorite"), null);
            }
        };
    }
}
