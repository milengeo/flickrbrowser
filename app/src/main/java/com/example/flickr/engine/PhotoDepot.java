package com.example.flickr.engine;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.example.flickr.R;
import com.example.flickr.model.PhotoModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;


/**
 * A singleton class responsible for processing of search queries.
 * Its lifecycle is linked to the application, but not the activity.
 * In that way the search terms and result will persist between configuration changes.
 */

public class PhotoDepot {

    /**
     * Callback interface for events
     */
    public interface Listener {
        void onUpdateStatus();
        void onUpdateList();
        void onConnectError(String link);
    }

    private static final String TAG = PhotoDepot.class.getSimpleName();
    private static final String BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1&safe_search=1";
    private static final long TIMER_INTERVAL = 1000;
    private static final int PAGE_SIZE = 100;
    private static final int DEFAULT_STORE_COUNT = 33;
    private static final boolean SIMULATION = false;

    private Context mContext;  // application context
    private Handler mHandler;
    private Listener mListener;

    private volatile boolean mStatusChanged;
    private volatile boolean mQuerying;
    private volatile boolean mStreaming;
    private volatile String mTerm = "";
    private int mAskedPageIndex;
    private int mAskedStoreCount;

    private List<PhotoModel> mPhotoStore = new Vector();
    private List<PhotoModel> mThisPage = new Vector();
    private PhotoModel mAskedImage;
    int mPageIndex;
    int mPageSize;
    int mPageCount;
    int mTotalCount;



    /**
     * Creating the instance at class loading time
     */
    private static final PhotoDepot sInstance = new PhotoDepot();


    /**
     * Static instance getter
     * @return The instance
     */
    public static PhotoDepot getInstance() {
        return sInstance;
    }


    /**
     * private constructor, to prevent instantiating from outside
     */
    private PhotoDepot() {
    }


    /**
     * One time setup, at the time of application creation
     * @param context
     */
    public void initialize(Context context) {
        mContext = context;
        mHandler = new Handler(); // Handler of the main thread
        doTimer();
    }


    private void doTimer() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onTimer();
                doTimer();
            }
        }, TIMER_INTERVAL);
    }


    private void onTimer() {
        checkState();
        if (mStatusChanged) {
            mStatusChanged = false;
            if (mListener != null) {
                mListener.onUpdateStatus();
            }
        }
    }


    private void checkState() {
        ///Log.d(TAG, "checkState");
        if (mTerm == null || mTerm.isEmpty()) {
            return; // nothing to search for
        }

        if (isBusy()) {
            return; // busy with network operation
        }
        if (mPhotoStore.size() >= mAskedStoreCount) {
            return; // have enough photos
        }

        int wantedIndex = mPhotoStore.size();
        int wantedPage = (mPageSize == 0) ? 1 : wantedIndex / mPageSize + 1;
        int pageOffset = (mPageSize == 0) ? wantedIndex : wantedIndex % mPageSize;

        if (wantedPage == mPageIndex && pageOffset >= mThisPage.size()) {
            Log.d(TAG, "checkState pageOffset >= mThisPage.size()");
            wantedPage = mPageIndex+1;
        }

        if (wantedPage != mPageIndex) {
            Log.d(TAG, "checkState-we need another page, start a page query");
            mAskedPageIndex = wantedPage;
            queryPage();
            return;
        }

        if (pageOffset < mThisPage.size()) {
            Log.d(TAG, "checkState-we need an image, start streaming it");
            streamImage(mThisPage.get(pageOffset));
        }
    }


    /**
     * Set an event listener
     * @param listener object
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }


    /**
     * Nullify the current listener
     */
    public void resetListener() {
        mListener = null;
    }


    public void setStatusChanged() {
        mStatusChanged = true;
    }


    public boolean isBusy() {
        return mQuerying || mStreaming;
    }


    /**
     * Get the queryPage state
     * @return true, if queryPage is currently executing
     */
    public boolean isQuerying() {
        return mQuerying;
    }


    /**
     * Get the download state
     * @return true, if download is currently executing
     */
    public boolean isStreaming() {
        return mStreaming;
    }


    public String getSearchTerm() {
        return mTerm;
    }


    public int getPageSize() {
        return mPageSize;
    }


    public int getPageIndex() {
        return mPageIndex;
    }


    public int getPageCount() {
        return mPageCount;
    }


    public int getTotalCount() {
        return mTotalCount;
    }


    public int getPhotoCount() {
        if (mPhotoStore == null)
            return 0;
        else
            return mPhotoStore.size();
    }



    public PhotoModel getPhoto(int index) {
        if (mPhotoStore == null)
            return null;
        else
            return mPhotoStore.get(index);
    }


    public void askStoreItems(int count) {
        Log.d(TAG, "askStoreItems: " + count);
        mAskedStoreCount = count;
        checkState();
    }


    public void search(String term) {
        mPhotoStore.clear();
        mPageIndex = 0;
        mPageCount = 0;
        mTotalCount = 0;
        if (mListener != null) {
            mListener.onUpdateList();
        }

        if (SIMULATION) {
            makeSimulation();
            return;
        }

        mTerm = term;
        mAskedStoreCount = DEFAULT_STORE_COUNT;
        checkState();
    }


    private void makeSimulation() {
        mTerm = "tEsTiNg";
        mTotalCount = 123456;
        mPageIndex = 1;
        mPageSize = 10;
        mPageCount = mTotalCount/mPageSize+1;
        mAskedStoreCount = mPageSize+1;

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test_540);
        for (int i=1; i<= mAskedStoreCount; i++) {
            PhotoModel photo = new PhotoModel();
            photo.setId("id-"+i);
            photo.setTitle("title-"+i);
            photo.setBitmap(bitmap);
            mPhotoStore.add(photo);
        }
        if (mListener != null)
            mListener.onUpdateList();
    }


    private void queryPage() {
        mQuerying = true;
        mThisPage.clear();

        QueryTask task = new QueryTask();
        task.start();
//        if (mListener != null) {
//            mListener.onUpdateList();
//        }
    }


    /**
     * The background thread for queries
     */
    private class QueryTask extends Thread {

        @Override
        public void run() {
            doQuery();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mQuerying = false;
                    onQueryDone();
                    checkState();
                }
            });
        }
    }


    private void doQuery(){
        HttpsURLConnection connection = null;
        JSONObject jsonObject = null;
        String queryUrl = null;
        StringBuilder link = null;

        // build the link
        link = new StringBuilder();
        link.append(BASE_URL);
        link.append("&api_key=3e7cc266ae2b0e0d78e279ce8e361736");
        link.append("&page=" + mAskedPageIndex);
        link.append("&text=" + mTerm);
        link.append("&per_page=" + PAGE_SIZE);

        try {
            URL url = new URL(link.toString());
            Log.d(TAG, "queryPage url " + url.toString());

            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int status = connection.getResponseCode();
            Log.d(TAG, "doQuery http status: " + status);

            switch (status) {
                case HttpsURLConnection.HTTP_OK:
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String response = builder.toString();
                    Log.d(TAG, "response: " + response);

                    parseResponse(response);

                    reader.close();
                    break;

                default:
                    Log.d(TAG, "doQuery_http error status code: " + status);
                    break;

            }
        } catch (Exception ex) {
            Log.e(TAG, " * ex doQuery: " + ex.getMessage());
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ex) {
                Log.e(TAG, " * ex doQuery_connection.disconnect: " + ex.getMessage());
            }
        }
    }


    private void parseResponse(String jsonText){
        try {
            JSONObject JsonObject = new JSONObject(jsonText);
            JSONObject photos = JsonObject.getJSONObject("photos");
            mPageSize = photos.getInt("perpage");
            mPageIndex = photos.getInt("page");
            mPageCount = photos.getInt("pages");
            mTotalCount = photos.getInt("total");
            Log.d(TAG, "parsePhotoArray_photos: " + mPageIndex + "/" + mPageCount + "/" + mTotalCount);

            JSONArray photoArray = photos.getJSONArray("photo");
            Log.d(TAG, "parsePhotoArray_length: " + photoArray.length());
            for (int i=0; i<photoArray.length(); i++) {
                JSONObject jsonPhoto = photoArray.getJSONObject(i);
                boolean isPublic = jsonPhoto.optBoolean("ispublic", true);
                if (!isPublic) {
                    continue;
                }
                PhotoModel photo = new PhotoModel();
                photo.setId(jsonPhoto.getString("id"));
                photo.setTitle(jsonPhoto.getString("title"));
                photo.setOwner(jsonPhoto.getString("owner"));
                photo.setSecret(jsonPhoto.getString("secret"));
                photo.setServer(jsonPhoto.getString("server"));
                photo.setFarm(jsonPhoto.getString("farm"));

                mThisPage.add(photo);
            }
        } catch (Exception ex) {
             Log.e(TAG, " * ex parseResponse: " + ex.getMessage());
        }
        Log.d(TAG, "parsePhotoArray_found: " + mThisPage.size());
    }


    private void onQueryDone() {
        if (mListener != null) {
            mListener.onUpdateList();
        }
    }




    private void streamImage(PhotoModel askedImage) {
        mAskedImage = askedImage;
        mStreaming = true;
        ImageTask task = new ImageTask();
        task.start();
    }


    /**
     * The background thread for image download
     */
    private class ImageTask extends Thread {

        @Override
        public void run() {
            doStream();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStreaming = false;
                    onImageDone(mAskedImage);
                    if (mAskedImage.getBitmap() != null)
                        checkState();
                }
            });
        }
    }


    private void doStream(){
        if (mAskedImage == null) return;
        HttpsURLConnection connection = null;
        JSONObject jsonObject = null;
        String queryUrl = null;
        StringBuilder link = null;

        // build the link
        ///http://farm{farm}.static.flickr.com/{server}/{id}_{secret}.jpg
        link = new StringBuilder();
        link.append("https://");
        link.append("farm"+mAskedImage.getFarm());
        link.append(".staticflickr.com/");
        link.append(mAskedImage.getServer());
        link.append("/" + mAskedImage.getId());
        link.append("_" + mAskedImage.getSecret());
        link.append(".jpg");

        try {
            URL url = new URL(link.toString());
            Log.d(TAG, "stream url " + url.toString());

            //instantiate url for connection
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int status = connection.getResponseCode();
            Log.d(TAG, "doStream http status: " + status);

            switch (status) {
                case HttpsURLConnection.HTTP_OK:
                    InputStream is = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Log.d(TAG, "doStream_bitmap decoded: " + bitmap.getByteCount());
                    mAskedImage.setBitmap(bitmap);
                    break;

                default:
                    Log.d(TAG, "doStream error status code: " + status);
                    break;
            }

        } catch (ConnectException ce) {
            Log.e(TAG, " * connect exception: " + ce.getMessage());
            if (mListener != null) {
                mListener.onConnectError(link.toString());
            }
        } catch (Exception ex) {
            Log.e(TAG, " * ex doStream: " + ex.getMessage());
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ex) {
                Log.e(TAG, " * ex doDownload.disconnect: " + ex.getMessage());
            }
        }
    }


    private void onImageDone(PhotoModel photo) {
        if (photo.getBitmap() != null) {
            // the image was downloaded, add it to the store
            mPhotoStore.add(photo);
            Log.d(TAG, "onImageDone-download success: " + photo.getTitle()
                + " " + photo.getBitmap().getWidth()
                + "x" + photo.getBitmap().getHeight());
            if (mListener != null) {
                mListener.onUpdateList();
            }
        } else {
            Log.d(TAG, "onImageDone-failed download: " + photo.getTitle());
        }
    }


}
