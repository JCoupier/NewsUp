package com.example.android.newsup;

/**
 * News Up created by JCoupier on 19/06/2017.
 *
 * {@link News} represents a news object.
 */
public class News {

    // Title of the News
    private String mTitle;

    // Section Name of the News
    private String mSectionName;

    // Url for the image Thumbnail
    private String mImageUrl;

    // Url for the link to the guardian website page
    private String mWebUrl;

    /**
     * Constructs a new {@link News} object.
     *
     * @param title is the title of the news
     * @param sectionName is hte section name of the news
     * @param imageUrl is the url for the thumbnail of the news
     * @param webUrl is the url for the link to the page at the guardian website of the news
     */
    public News (String title, String sectionName, String imageUrl, String webUrl){
        mTitle = title;
        mSectionName = sectionName;
        mImageUrl = imageUrl;
        mWebUrl = webUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getWebUrl() {
        return mWebUrl;
    }
}
