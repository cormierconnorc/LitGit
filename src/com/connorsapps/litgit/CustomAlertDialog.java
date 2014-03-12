package com.connorsapps.litgit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CustomAlertDialog extends DialogFragment
{
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		String title = this.getArguments().getString("title");
		String message = this.getArguments().getString("message");
		
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		
		builder.setPositiveButton("Okay", null);
		
		return builder.create();
	}
}
