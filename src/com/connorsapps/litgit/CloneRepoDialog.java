package com.connorsapps.litgit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

public class CloneRepoDialog extends DialogFragment
{
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{		
		final MainActivity act = (MainActivity)this.getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("Enter Repo Information:");
		
		View view = View.inflate(getActivity(), R.layout.clone_repo_dialog, null);
		
		final EditText remote = (EditText)view.findViewById(R.id.remote);
		final EditText name = (EditText)view.findViewById(R.id.name);
		
		builder.setView(view);
		
		builder.setPositiveButton("Clone", new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String rStr = remote.getText().toString();
				String nStr = name.getText().toString();
				
				if (nStr.equals("") && rStr.contains("/") && rStr.contains(".git"))
					nStr = GitRepo.getRepoNameFromRemote(rStr);
				else if (nStr.equals(""))
				{
					act.showNotificationDialog("Invalid Repository", "Check entered remote address");
					return;
				}
				
				act.gitClone(nStr, rStr);
			}
			
		});
		
		builder.setNegativeButton("Nevermind", null);
		
		return builder.create();
	}
}
