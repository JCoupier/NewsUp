package com.example.android.newsup;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * News Up created by JCoupier on 19/06/2017.
 */
public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    /** URL for news data from the Guardian API dataset */
    private static final String QUERY_URL = "https://content.guardianapis.com/search";

    // API key which is needed to access content from the API
    private static final String URL_KEY = "test";

    private SwipeRefreshLayout mSwipeContainer;

    private RecyclerView mRecyclerView;

    // Adapter for the list of news
    private NewsAdapter mAdapter;

    // TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    // Progress bar that is displayed to show that the info is loading
    private ProgressBar mLoadingProgressIndicator;

    // Constant value for the news loader ID
    private static final int NEWS_LOADER_ID = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Lookup the swipe container view
        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsActivity.this);
            }
        });

        // Lookup the recyclerView in activity layout
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Set layout manager to position the items
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Find the emptyStateTextView
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);

        // Find the loadingProgressIndicator
        mLoadingProgressIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        // Create adapter passing in the sample user data and set the listener creating the intent
        mAdapter = new NewsAdapter(this, new ArrayList<News>(), new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                String webUrl = news.getWebUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webUrl));
                startActivity(intent);
            }
        });

        // Attach the adapter to the recyclerView to populate items
        mRecyclerView.setAdapter(mAdapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo =  connectivityManager.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingProgressIndicator.setVisibility(View.GONE);
            // Hide RecyclerView
            mRecyclerView.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        // Read the user's preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String order = sharedPreferences.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_default));
        String subject = sharedPreferences.getString(getString(R.string.settings_subject_key),
                getString(R.string.settings_subject_default));

        // Hide RecyclerView
        mRecyclerView.setVisibility(View.GONE);

        // Set the URI builder with the query url
        Uri baseUri = Uri.parse(QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        // Append components of the final url
        uriBuilder.appendQueryParameter("q", subject);
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("order-by", order);
        uriBuilder.appendQueryParameter("api-key", URL_KEY);

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Hide loading indicator because the data has been loaded
        mLoadingProgressIndicator.setVisibility(View.GONE);
        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            // Show the RecyclerView
            mRecyclerView.setVisibility(View.VISIBLE);
            // Add all news to the adapter
            mAdapter.addAll(news);
            // Call setRefreshing(false) to signal the refresh is finished
            mSwipeContainer.setRefreshing(false);
        } else {
            // Set empty state text to display "No corresponding news found."
            mEmptyStateTextView.setText(R.string.no_news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }
}
