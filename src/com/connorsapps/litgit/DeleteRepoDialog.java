package com.connorsapps.litgit;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeleteRepoDialog extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final String[] repoNames = this.getArguments().getStringArray("repoNames");
		final MainActivity act = (MainActivity)this.getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		
		builder.setTitle("Select Repos to Delete");
		
		final ListView view = new ListView(act);
		
		//Set the data for the listview
		ArrayAdapter<String> ada = new ArrayAdapter<String>(act, android.R.layout.simple_list_item_multiple_choice, repoNames);
		view.setAdapter(ada);
		
		view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		//Show listview in dialog
		builder.setView(view);
		
		
		builder.setPositiveButton("Delete Selected", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				SparseBooleanArray ar = view.getCheckedItemPositions();
				List<String> selected = new ArrayList<String>();
				
				for (int i = 0; i < repoNames.length; i++)
					if (ar.get(i))
						selected.add(repoNames[i]);
				
				act.deleteRepos(selected);
			}
			
		});
		
		builder.setNegativeButton("Nevermind", null);
		
		return builder.create();
	}
}
