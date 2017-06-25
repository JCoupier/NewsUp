package com.example.android.newsup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * News Up created by JCoupier on 19/06/2017.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    // The ArrayList of News objects
    private ArrayList<News> mNews;

    // The Listener
    private OnItemClickListener mListener;

    private Context mContext;

    public NewsAdapter(Context context, ArrayList<News> news, OnItemClickListener listener) {
        this.mContext = context;
        this.mNews = news;
        this.mListener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(News news);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder holder, int position) {

        // Find the news at the given position in the list of news
        News news = mNews.get(position);

        // Get the TextView from the ViewHolder and then set the text (title)
        holder.newsTitleTextView.setText(news.getTitle());
        // Get the TextView from the ViewHolder and then set the text (sectionName)
        holder.newsSectionTextView.setText(news.getSectionName());

        // Use the Picasso library to display the thumbnail of the current news.
        // If there is no thumbnail or if after three try the thumbnail can't be downloaded:
        // an image placeholder is displayed instead.
        Picasso.with(mContext).load(news.getImageUrl())
                .placeholder(R.drawable.news_placeholder)
                .error(R.drawable.news_placeholder)
                .into(holder.newsImageView);

        // Bind a listener to the item
        holder.listenerBinder(news, mListener);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mNews.size();
    }

    // The ViewHolder which caches the ImageView and the two TextViews
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView newsImageView;
        public TextView newsTitleTextView;
        public TextView newsSectionTextView;

        ViewHolder(final View itemView) {
            super(itemView);

            // Find the different component of the viewHolder
            newsImageView = (ImageView) itemView.findViewById(R.id.news_image);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.news_title);
            newsSectionTextView = (TextView) itemView.findViewById(R.id.news_section);
        }
        // Bind a listener to an item (News)
        private void listenerBinder(final News news, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(news);
                }
            });
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mNews.clear();
        notifyDataSetChanged();
    }

    // Add a list of items (News)
    public void addAll(List<News> news) {
        mNews.addAll(news);
        notifyDataSetChanged();
    }
}
