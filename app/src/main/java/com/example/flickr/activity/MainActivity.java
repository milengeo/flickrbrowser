package com.example.flickr.activity;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.flickr.R;
import com.example.flickr.engine.PhotoAdapter;
import com.example.flickr.engine.PhotoDepot;
import com.example.flickr.widget.LayoutManagerWrapper;
import com.example.flickr.widget.SearchDialog;

/**
 * The main and only activity of the app.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private View mRootLayout;
    private FloatingActionButton mSearchButton;
    private ProgressDialog mWaitDlg;
    private TextView mStatusText;
    private TextView mNothingHint;
    private RecyclerView mPhotoView;
    private PhotoAdapter mPhotoAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mRootLayout = findViewById(R.id.main_container);
        mSearchButton = findViewById(R.id.main_search_button);
        mSearchButton.setOnClickListener(new SearchClicker());

        mPhotoView = findViewById(R.id.main_grid_view);
        LayoutManagerWrapper layoutManager = new LayoutManagerWrapper(this, PhotoAdapter.COLUMN_COUNT);
        mPhotoView.setLayoutManager(layoutManager);

        mPhotoAdapter = new PhotoAdapter();
        mPhotoView.setAdapter(mPhotoAdapter);

        mNothingHint = findViewById(R.id.main_hint_text);
        mStatusText = findViewById(R.id.main_status_text);

        PhotoDepot.getInstance().setListener(new DepotListener());

        updateLook();
        Log.d(TAG, "onCreate");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhotoDepot.getInstance().resetListener();
        Log.d(TAG, "onDestroy");
    }


    private void updateLook() {
        updateStatus();

        if (PhotoDepot.getInstance().isBusy() && PhotoDepot.getInstance().getTotalCount() == 0) {
            showWaitDlg(getResources().getString(R.string.searching_please_wait));
        } else {
            hideWaitDlg();
        }

        if (PhotoDepot.getInstance().getTotalCount() == 0) {
            mPhotoView.setVisibility(View.INVISIBLE);
            mNothingHint.setVisibility(View.VISIBLE);
        } else {
            mPhotoView.setVisibility(View.VISIBLE);
            mNothingHint.setVisibility(View.INVISIBLE);
            mPhotoAdapter.notifyDataSetChanged();
        }
    }

    private void updateStatus() {
        if (PhotoDepot.getInstance().getTotalCount() == 0) {
            if (PhotoDepot.getInstance().isBusy()) {
                mStatusText.setText(getResources().getString(R.string.loading));
            } else {
                mStatusText.setText(getResources().getString(R.string.nothing_is_found));
            }
        } else {
            String status = " " + PhotoDepot.getInstance().getSearchTerm() + " " + mPhotoAdapter.getShownCount() + "/" + PhotoDepot.getInstance().getTotalCount();
            mStatusText.setText(status);
        }
    }


    private void showWaitDlg(String message) {
        if (mWaitDlg != null) return;
        mWaitDlg = new ProgressDialog(this);
        mWaitDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mWaitDlg.setMessage(message);
        mWaitDlg.setCanceledOnTouchOutside(false);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }


    private void hideWaitDlg() {
        if (null == mWaitDlg) return;
        mWaitDlg.dismiss();
        mWaitDlg = null;
    }



    private class DepotListener implements PhotoDepot.Listener {

        public void onUpdateStatus() {
            Log.d(TAG, "DepotListener_onUpdateList");
            updateStatus();
        }

        public void onUpdateList() {
            MainActivity.this.mPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "DepotListener_onUpdateList");
                    updateLook();
                }
            });
        }

        public void onConnectError(final String link) {
            Log.d(TAG, "DepotListener_onConnectError: " + link);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusText.setText(getResources().getString(R.string.error_connecting) + " " + link);
                }
            });
        }

    }


    private class SearchClicker implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "SearchClicker.onClick");
            new SearchDialog().ask(MainActivity.this);
///            PhotoDepot.getInstance().search("horse");
///            PhotoDepot.getInstance().search("sofia");
        }
    }


}
