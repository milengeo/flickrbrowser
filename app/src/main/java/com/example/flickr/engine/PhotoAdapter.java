package com.example.flickr.engine;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.flickr.R;
import com.example.flickr.model.PhotoModel;


/**
 * The RecyclerView.Adapter which handles photo list.
 * The most important function is to detect when more items need to be downloaded.
 * It also estimates the position of the currently shown items
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder> {


    private static final String TAG = PhotoAdapter.class.getSimpleName();
    public static final int COLUMN_COUNT = 3;
    private static final int ENOUGH_ROOM = COLUMN_COUNT * 3;
    private GridLayoutManager mLayoutManager;
    private int mShownCount;
    private int mPrevIndex, mLastIndex;


    public int getShownCount() {
        return mShownCount;
    }


    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView mImage;
        TextView mTitle;

        public MyHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.card_image);
            mTitle = itemView.findViewById(R.id.card_title);
        }
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ///Log.d(TAG, "onCreateViewHolder: " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        MyHolder myViewHolder = new MyHolder(view);
        return myViewHolder;
    }


    @Override
    public void onBindViewHolder(final MyHolder holder, int index) {
        //Log.d(TAG, "onBindViewHolder: " + index);
        PhotoModel photo = PhotoDepot.getInstance().getPhoto(index);
        holder.mImage.setImageBitmap(photo.getBitmap());
        holder.mTitle.setText(photo.getTitle());

        estimateShownCount(index);
        checkRoom(index);
    }


    @Override
    public int getItemCount() {
        return PhotoDepot.getInstance().getPhotoCount();
    }


    private void estimateShownCount(int index) {
        mPrevIndex = mLastIndex;
        mLastIndex = index;
        if (mLayoutManager != null) {
            int firstItem = mLayoutManager.findFirstVisibleItemPosition();
            int lastItem = mLayoutManager.findLastVisibleItemPosition();
            Log.d(TAG, "estimateShownCount: " + mPrevIndex + "/" + index + "  " + firstItem + "/" + lastItem);


            if (lastItem > 0) {
                int shownCount = 0;
                if (mLastIndex > mPrevIndex) {
                    // going down
                    shownCount = lastItem + COLUMN_COUNT + 1;
                } else if (mLastIndex < mPrevIndex) {
                    // going up
                    shownCount = lastItem - COLUMN_COUNT + 1;
                }
                if (shownCount < 0) shownCount = 0;
                if (shownCount != mShownCount) {
                    mShownCount = shownCount;
                    PhotoDepot.getInstance().setStatusChanged();
                }
            }
        }
    }


    /**
     * Check for enough available items
     */
    private void checkRoom(int bound) {
        int room = PhotoDepot.getInstance().getPhotoCount() - bound;
        ///Log.d(TAG, "checkRoom_room: " + room);
        if (room > ENOUGH_ROOM) return;

        if (PhotoDepot.getInstance().isBusy()) {
            Log.d(TAG, "checkRoom_isBusy");
            return;
        }

        int askedCount = PhotoDepot.getInstance().getPhotoCount() + 2*ENOUGH_ROOM;
        PhotoDepot.getInstance().askStoreItems(askedCount);
    }
}
