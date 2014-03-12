package com.connorsapps.litgit;

/**
 * Connor Cormier
 * 3/09/14
 * LitGit Main Activity
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
{
	public static final String REPO_INFO_FILE = "rp.info";
	public static final long DOUBLE_CLICK_TIME = 1000;
	private List<GitRepo> repos;
	private File repoDirectory;	//Root directory for repositories
	private GitRepo curRepo;	//Repo currently being observed, null if viewing "home"
	private File curDirectory;	//Directory currently being observed in file view
	private Spinner repoSelect;
	private ListView fileList;
	private Button pull, clone;
	private CountdownThread count;	//Thread that determines whether a back button click should be interpreted as double or single
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Set the directory in which to store repos:
		//Use external file directory
		repoDirectory = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/repos");
		
		//Create the directory if it doesn't exist
		if (!repoDirectory.exists())
			repoDirectory.mkdir();

		curDirectory = repoDirectory;
		
		//Create the repo list
		repos = new ArrayList<GitRepo>();
		
		//Instantiate view fields from XML
		buildViews();
		//Load the repos in the background
		loadRepos();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.deleteRepo:
			showDeleteDialog();
			break;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	public void buildViews()
	{
		repoSelect = (Spinner)findViewById(R.id.selectRepo);
		fileList = (ListView)findViewById(R.id.fileList);
		
		//Spinner Listener
		repoSelect.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				//Set the current repo view and directory, then refresh the file view
				if (position == 0)
				{
					curRepo = null;
					curDirectory = repoDirectory;
				}
				else
				{
					curRepo = repos.get(position - 1);
					curDirectory = new File(curRepo.getLocalPath());
				}
				
				refreshFileList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				//Do nothing
			}
			
		});
		
		//ListView Listener
		fileList.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				//Get the list of files in the current directory
				File[] files = curDirectory.listFiles();
				
				//Sort so the indices match up with the list view
				Arrays.sort(files);
				
				//Move into directory if a directory
				if (files[position].isDirectory())
				{
					curDirectory = files[position];
					
					refreshFileList();
				}
				//Open file with external application if not directory
				else
				{
					openFileExternal(files[position]);
				}
			}			
		});
		
		pull = (Button)findViewById(R.id.pullButton);
		clone = (Button)findViewById(R.id.cloneButton);
		
		//Set listeners
		pull.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				MainActivity.this.executePull();
				
			}
		});
		clone.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				MainActivity.this.executeClone();
			}
		});
	}
	
	public void openFileExternal(File file)
	{
		//Setup intent
		Intent launchExternal = new Intent();
		launchExternal.setAction(Intent.ACTION_VIEW);
		
		//Get mime type of file
		MimeTypeMap tMap = MimeTypeMap.getSingleton();
		String mimeType = tMap.getMimeTypeFromExtension(getExtension(file));
		
		//Try to open it as plain text if you have no idea what to do with it.
		if (mimeType == null)
			mimeType = "text/html";
		
		//Set data of file
		launchExternal.setDataAndType(Uri.fromFile(file), mimeType);
		
		//Launch activity
		startActivity(launchExternal);
	}
	
	private String getExtension(File f)
	{
		String name = f.getName();
		
		if (name.contains("."))
			return name.substring(name.lastIndexOf(".") + 1);
		return "";
	}
	
	public void showDeleteDialog()
	{
		DeleteRepoDialog dial = new DeleteRepoDialog();
		
		//Since the repository list should not be public, pass the dialog an array of the repo names
		String[] repoNames = new String[repos.size()];
		
		for (int i = 0; i < repoNames.length; i++)
			repoNames[i] = repos.get(i).getRepoName();
		
		Bundle b = new Bundle();
		b.putStringArray("repoNames", repoNames);
		
		dial.setArguments(b);
		
		dial.show(getSupportFragmentManager(), "DeleteDialog");
	}
	
	/**
	 * Delete repositories with given names
	 * @param repoNames
	 */
	public void deleteRepos(List<String> repoNames)
	{
		ProgressDialog dial = ProgressDialog.show(this, "Deleting Repositories", "Please wait...", true, false);
		DeleteRepoTask task = new DeleteRepoTask(this, dial);
		
		String[] names = new String[repoNames.size()];
		repoNames.toArray(names);
		
		task.execute(names);
	}
	
	public void updateSpinner()
	{
		//Generate list of repo names
		List<String> repoNames = new ArrayList<String>();
		
		//Add in home button at top
		repoNames.add("Home");
		
		for (int i = 0; i < repos.size(); i++)
			repoNames.add(repos.get(i).getRepoName());
		
		ArrayAdapter<String> ada = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, repoNames);
		
		repoSelect.setAdapter(ada);
	}
	
	/**
	 * Execute pull on current repo
	 */
	public void executePull()
	{
		//If in home directory, do on all
		if (curRepo == null)
		{
			GitRepo[] repos = new GitRepo[this.repos.size()];
			this.repos.toArray(repos);
			gitPull(repos);
		}
		else
			gitPull(curRepo);
			
	}
	
	/**
	 * Starts a task to handle the actual git pull for the specified repo
	 * @param rep
	 */
	public void gitPull(GitRepo... reps)
	{
		ProgressDialog dial = ProgressDialog.show(this, "Pulling Repository", "Press back to dismiss. Operation will continue in background.", true, true);
		PullRepoTask task = new PullRepoTask(this, dial);
		task.execute(reps);
	}
	
	public void executeClone()
	{
		executeClone("", "");
	}
	
	/**
	 * Show clone dialog
	 */
	public void executeClone(String name, String remote)
	{
		//Prompt user with clone dialog
		CloneRepoDialog cloner = new CloneRepoDialog();
		
		//Pass in the pre-settings
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		bundle.putString("remote", remote);
		cloner.setArguments(bundle);
		
		cloner.show(getSupportFragmentManager(), "CloneRepoDialog");
	}
	
	/**
	 * Carry out git clone operation
	 * @param remote
	 */
	public void gitClone(String name, String remote)
	{
		//Check to see if the remote repo already exists and exit if it does. If name conflict, prompt for new name
		for (int i = 0; i < repos.size(); i++)
		{
			if (repos.get(i).getRemotePath().equals(remote))
			{
				showNotificationDialog("Could Not Create Repo", "Repository already exists.");
				return;
			}
			else if (repos.get(i).getRepoName().equals(name))
			{
				showNotificationDialog("Duplicate Name", "Please change the repository name to continue.");
				return;
			}
		}
		
		//Create a progress dialog and pass it a reference so the task can dismiss it.
		ProgressDialog dial = ProgressDialog.show(this, "Cloning Repository", "Press back to dismiss. Operation will continue in background.", true, true);
		CloneRepoTask task = new CloneRepoTask(this, dial);
		task.execute(remote, name);
	}
	
	public void showNotificationDialog(String title, String msg)
	{
		CustomAlertDialog dial = new CustomAlertDialog();
		
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", msg);
		dial.setArguments(args);
		
		dial.show(this.getSupportFragmentManager(), "NotificationDialog");
	}
	
	/**
	 * Refresh list of files in ListView
	 * @param dir
	 */
	public void refreshFileList()
	{
		int loc;
		
		//If in repo directory, set to home
		if (curDirectory.equals(repoDirectory))
		{
			curRepo = null;
			repoSelect.setSelection(0);
		}
		//Set the repo if you're in its root directory
		else if ((loc = this.findRepoByPath(curDirectory.getAbsolutePath())) != -1)
		{	
			curRepo = repos.get(loc);
			repoSelect.setSelection(loc + 1);
		}
		
		File[] files = curDirectory.listFiles();
		
		//Sort files to allow for easier viewing
		Arrays.sort(files);

		//Time consuming when file sizes are not known, so preload that information
		FileAdapter fileList = new FileAdapter(this, android.R.layout.simple_list_item_1, files);
		
		//Update the adapter
		this.fileList.setAdapter(fileList);
	}
	
	/**
	 * Load repositories from save file
	 */
	public void loadRepos()
	{
		ProgressDialog dial = ProgressDialog.show(this, "Loading Repositories", "Please wait...", true, false);
		LoadRepoTask task = new LoadRepoTask(this, dial);
		task.execute(REPO_INFO_FILE);
	}
	
	/**
	 * Save the list of repositories
	 */
	public void saveRepos()
	{
		Thread saveThread = new Thread()
		{
			public void run()
			{
				PrintWriter out = null;
				
				try
				{
					out = new PrintWriter(openFileOutput(REPO_INFO_FILE, MODE_PRIVATE));
					
					for (int i = 0; i < repos.size(); i++)
						out.println(repos.get(i));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					out.close();
				}
			}
		};
		
		saveThread.start();
	}
	
	/**
	 * Find the position of a GitRepo in the repos list based on the repo name
	 * Uses a binary search
	 * @param name
	 * @return Position if found, -1 if not
	 */
	public int findRepoByName(String name)
	{
		return findRepoByPath(new File(this.repoDirectory.getAbsolutePath() + "/" + name).getAbsolutePath());
	}
	
	/**
	 * Find the position of a GitRepo in the repos list based on the repo path
	 * Uses a binary search
	 * @param localPath
	 * @return
	 */
	public int findRepoByPath(String localPath)
	{
		return findRepoBinary(localPath, 0, repos.size() - 1);
	}
	
	/**
	 * Binary search to find repository by path
	 * @param target
	 * @param left Inclusive lower bound
	 * @param right Inclusive upper bound
	 * @return Position if found, -1 if not
	 */
	private int findRepoBinary(String target, int left, int right)
	{
		while (left <= right)
		{
			int middle = (left + right) / 2;
		
			int cmp = target.compareTo(repos.get(middle).getLocalPath());
		
			if (cmp > 0)
				left = middle + 1;
			else if (cmp < 0)
				right = middle - 1;
			else
				return middle;
		}
		
		return -1;
	}
	
	/**
	 * Add a new repository to the list and refresh the file list
	 * @param repo
	 */
	public void addRepo(GitRepo repo)
	{
		int addPos;
		
		//Find position to place this repo
		for (addPos = 0; addPos < repos.size(); addPos++)
			if (repo.compareTo(repos.get(addPos)) < 0)
				break;
		
		repos.add(addPos, repo);
		
		updateRepos();
	}
	
	/**
	 * Add a collection of repositories and refresh the file list
	 * @param repos
	 */
	public void addAllRepos(Collection<GitRepo> repos)
	{
		this.repos.addAll(repos);
		
		//Sort the repo list
		Collections.sort(this.repos);
		
		updateRepos();
	}
	
	/**
	 * Add an array of repos to the repo list
	 * @param repos
	 */
	public void addAllRepos(GitRepo[] repos)
	{
		for (GitRepo r : repos)
			this.repos.add(r);
		
		Collections.sort(this.repos);
		
		updateRepos();
	}
	
	/**
	 * Perform operations necessary after an update to the repository list
	 */
	public void updateRepos()
	{
		//Refresh
		refreshFileList();
		
		//Update spinner
		updateSpinner();
		
		//Save updates
		saveRepos();
	}
	
	/**
	 * Get the directory in which to store repositories
	 * @return
	 */
	public File getRepoDirectory()
	{
		return this.repoDirectory;
	}
	
	public void recurseDelete(File file)
	{
		if (file == null)
			return;
		
		if (file.isDirectory())
		{
			for (File f : file.listFiles())
			{
				if (f.isDirectory())
					recurseDelete(f);
				else
					f.delete();
			}
		}
		
		file.delete();
	}
	
	public GitRepo getRepoAt(int pos)
	{
		if (pos < 0 || pos >= repos.size())
			return null;
		
		return this.repos.get(pos);
	}
	
	public void removeRepoAt(int pos)
	{
		if (pos < 0 || pos >= repos.size())
			return;
		
		repos.remove(pos);
	}
	
	/**
	 * Handle back button press
	 */
	@Override
	public void onBackPressed()
	{
		//If already in root folder, go through exit procedure:
		if (this.curDirectory.equals(this.repoDirectory))
		{
			//First press
			if (count == null || !count.isAlive())
			{
				count = new CountdownThread(DOUBLE_CLICK_TIME);
				count.start();
				
				//Notify the user
				Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
			}
			//Second press
			else
			{
				//Exit the app
				this.finish();
			}
		}
		else
		{
			//Step up a directory
			this.curDirectory = this.curDirectory.getParentFile();
			this.refreshFileList();
		}
	}
	
	public static class CountdownThread extends Thread
	{
		private final long time;
		
		public CountdownThread(long time)
		{
			this.time = time;
		}
		
		public void run()
		{
			try
			{
				Thread.sleep(time);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
