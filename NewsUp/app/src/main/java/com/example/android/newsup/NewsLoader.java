package com.example.android.newsup;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * News Up created by JCoupier on 19/06/2017.
 *
 * Loads a list of news by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class NewsLoader extends AsyncTaskLoader<List<News>> {

    /** Query URL */
    private String mQueryUrl;

    /**
     * Constructs a new {@link NewsLoader}.
     *
     * @param context of the activity
     * @param queryUrl to load data from
     */
    public NewsLoader(Context context, String queryUrl) {
        super(context);
        mQueryUrl = queryUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<News> loadInBackground() {
        if (mQueryUrl == null) {
            return null;
        }
        return NewsUtils.fetchNewsData(mQueryUrl);
    }
}
