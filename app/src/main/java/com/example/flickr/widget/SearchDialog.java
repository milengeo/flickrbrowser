package com.example.flickr.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.flickr.R;
import com.example.flickr.engine.PhotoDepot;


public class SearchDialog extends DialogFragment {

	private static final int MIN_LENGTH = 3;

	private static boolean sDialogShown = false;


	public void ask(FragmentActivity activity) {
		if (sDialogShown) return;
		show(activity.getSupportFragmentManager(), "SearchDialog");
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View dialogLayout = inflater.inflate(R.layout.search_dialog, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

		final EditText termEdit = dialogLayout.findViewById(R.id.search_dlg_term);
		final Button cancelButton = dialogLayout.findViewById(R.id.search_dlg_cancel);
		final Button doneButton = dialogLayout.findViewById(R.id.search_dlg_okay);

		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}
		);

		doneButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						String term = termEdit.getText().toString().trim();
						if (term.length() < MIN_LENGTH) {
							showAlert(dialog.getContext(), getResources().getString(R.string.sorry)
									,getResources().getString(R.string.term_too_short));
							return;
						}
						dialog.dismiss();
						PhotoDepot.getInstance().search(term);
					}
				}
		);

		return dialogLayout;
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		sDialogShown = false;
	}


	private void showAlert(Context aContext, String aTitle, String aText) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(aContext);
		alertDialog.setIcon(R.mipmap.ic_launcher);
		alertDialog.setPositiveButton(getResources().getString(R.string.okay), null);
		alertDialog.setMessage(aText);
		alertDialog.setTitle(aTitle);
		alertDialog.show();
	}


}
