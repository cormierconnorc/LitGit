package com.connorsapps.litgit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class LoadRepoTask extends AsyncTask<String, GitRepo, Boolean>
{
	private final MainActivity callback;
	private final ProgressDialog dial;
	
	public LoadRepoTask(MainActivity callback, ProgressDialog dial)
	{
		this.callback = callback;
		this.dial = dial;
	}

	@Override
	protected Boolean doInBackground(String...params)
	{		
		File nF = new File(callback.getFilesDir() + "/" + params[0]);
		Scanner in = null;
		
		try
		{
			if (!nF.exists())
				nF.createNewFile();
			
			in = new Scanner(new FileInputStream(nF));
			
			List<GitRepo> repos = new ArrayList<GitRepo>();
			
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				
				if (!line.trim().equals(""))
					repos.add(GitRepo.fromString(line));
			}
			
			//Clean out "broken" directories
			removeUnassociatedFiles(repos);
			
			//After loading, build the file map on the repo directory
			FileAdapter.recurseFileLength(callback.getRepoDirectory());
			
			GitRepo[] repoArray = new GitRepo[repos.size()];
			repos.toArray(repoArray);
			
			//Pass the loaded repos to the progress update method
			this.publishProgress(repoArray);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null)
				in.close();
		}
		
		return true;
	}
	
	private void removeUnassociatedFiles(List<GitRepo> repos)
	{
		List<File> toDelete = new ArrayList<File>();
		
		//Remove all folders not associated with repositories
		for (File f : callback.getRepoDirectory().listFiles())
		{
			boolean associated = false;
			
			if (f.isDirectory())
				for (GitRepo rep : repos)
				{
					if (rep.getLocalPath().equals(f.getAbsolutePath()))
						associated = true;
				}
			if (!associated)
				toDelete.add(f);
		}
		
		for (File f : toDelete)
			callback.recurseDelete(f);
	}
	
	@Override
	protected void onProgressUpdate(GitRepo...gitRepos)
	{
		//Dismiss dialog
		if (dial.isShowing())
			dial.dismiss();
		
		callback.addAllRepos(gitRepos);
	}

}
