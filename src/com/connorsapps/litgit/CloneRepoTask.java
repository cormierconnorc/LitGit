package com.connorsapps.litgit;

import java.io.File;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class CloneRepoTask extends AsyncTask<String, GitRepo, Boolean>
{
	private final MainActivity callback;
	private final ProgressDialog dial;
	private boolean success;
	
	public CloneRepoTask(MainActivity callback, ProgressDialog dial)
	{
		this.callback = callback;
		this.dial = dial;
	}

	@Override
	protected Boolean doInBackground(String... params)
	{
		//Local folder to hold the repo
		//If there is only 1 parameter (the remote repo to clone), then use it to generate a name. Otherwise, Use the given name
		File folder = new File(callback.getRepoDirectory().getAbsolutePath() + "/" + (params.length == 1 ? GitRepo.getRepoNameFromRemote(params[0]) : params[1]));
		
		try
		{
			GitRepo repo = new GitRepo(folder.getAbsolutePath(), params[0]);
			
			//Set success flag
			success = true;
			
			//Refresh the size of this repo
			FileAdapter.recurseFileLength(folder);
			
			this.publishProgress(repo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			//Set success flag and publish progress
			success = false;
			
			//If you couldn't clone, delete the associated folder to clean up after yourself
			if (folder.exists())
				callback.recurseDelete(folder);
			
			this.publishProgress();
			
			return false;
		}
		return true;
	}
	
	@Override
	protected void onProgressUpdate(GitRepo... repos)
	{
		//Dismiss the dialog
		if (dial.isShowing())
			dial.dismiss();
		
		if (success)
			callback.addRepo(repos[0]);
		else
			callback.showNotificationDialog("Could not clone repository", "Something went wrong.");
	}
	
	
}
