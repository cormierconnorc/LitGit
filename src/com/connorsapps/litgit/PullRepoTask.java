package com.connorsapps.litgit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class PullRepoTask extends AsyncTask<GitRepo, Boolean, Boolean>
{
	private final MainActivity callback;
	private final ProgressDialog dial;
	private final List<String> failedPulls;
	private boolean success;
	
	public PullRepoTask(MainActivity callback, ProgressDialog dial)
	{
		this.callback = callback;
		this.dial = dial;
		success = true;
		failedPulls = new ArrayList<String>();
	}
	
	@Override
	protected Boolean doInBackground(GitRepo... params)
	{
		for (int i = 0; i < params.length; i++)
		{
			try
			{
				params[i].gitPull();
				
				//Refresh the file size of each directory that is successfully updated
				FileAdapter.recurseFileLength(new File(params[i].getLocalPath()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				success = false;
				
				//Add the name of this repo to the failure list
				failedPulls.add(params[i].getRepoName());
			}
		}
		
		//Refresh file list
		this.publishProgress(success);
		
		
		return true;
	}
	
	@Override
	protected void onProgressUpdate(Boolean... worked)
	{
		if (dial.isShowing())
			dial.dismiss();
		
		if (!success)
		{
			String failures = "Affected Repositories: ";
			
			for (int i = 0; i < failedPulls.size(); i++)
				failures += failedPulls.get(i) + (i != failedPulls.size() - 1 ? ", " : "");
			
			callback.showNotificationDialog("Failed Pull", failures);
		}
		
		callback.refreshFileList();
	}
}
