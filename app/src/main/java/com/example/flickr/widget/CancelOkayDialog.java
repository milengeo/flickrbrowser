package com.example.flickr.widget;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.flickr.R;


@SuppressWarnings("deprecation")


public class CancelOkayDialog extends DialogFragment {

	private static boolean sDialogShown = false;
	private static String sTitle, sMessage;
	private static Runnable mOkayRunnable;


	public static void ask(Activity aActivity, String aTitle, String aMessage, Runnable aRunnable) {
		if (sDialogShown) return;
        CancelOkayDialog self = new CancelOkayDialog();
        sTitle = aTitle;
		sMessage = aMessage;
		mOkayRunnable = aRunnable;
		FragmentManager manager = aActivity.getFragmentManager();
		self.show(manager, "OkayDialog");
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		sDialogShown = true;
		View dialogLayout = inflater.inflate(R.layout.cancel_okay_dialog, container, false);
		final Dialog dialog = getDialog();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = dialogLayout.findViewById(R.id.cancelok_dialog_title);
        tvTitle.setText(sTitle);

		TextView tvMessage = dialogLayout.findViewById(R.id.cancelok_dialog_message);
		tvMessage.setText(sMessage);

        Button cancelButton = dialogLayout.findViewById(R.id.cancelok_dialog_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		Button okayButton = dialogLayout.findViewById(R.id.cancelok_dialog_okay);
		okayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (null != mOkayRunnable)
					mOkayRunnable.run();
			}
		});

		return dialogLayout;
	}


	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		sDialogShown = false;
	}


}
