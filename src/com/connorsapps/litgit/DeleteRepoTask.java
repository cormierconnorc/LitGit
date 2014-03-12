package com.connorsapps.litgit;

import java.io.File;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class DeleteRepoTask extends AsyncTask<String, Boolean, Boolean>
{
	private final MainActivity callback;
	private final ProgressDialog dial;
	
	public DeleteRepoTask(MainActivity callback, ProgressDialog dial)
	{
		this.callback = callback;
		this.dial = dial;
	}

	@Override
	protected Boolean doInBackground(String... params)
	{
		for (String name : params)
		{
			int loc = callback.findRepoByName(name);

			//Get the associated repo object
			GitRepo repo = callback.getRepoAt(loc);
	
			//Delete the files associated with this repo
			callback.recurseDelete(new File(repo.getLocalPath()));
	
			callback.removeRepoAt(loc);		
		}
		
		//Update the repos and dismiss the dialog
		this.publishProgress();
		
		return true;
	}
	
	@Override
	protected void onProgressUpdate(Boolean... unused)
	{
		callback.updateRepos();
		
		dial.dismiss();
	}

}
