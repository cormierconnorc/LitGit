package com.connorsapps.litgit;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

/**
 * Class for interacting with Git.
 * A note about the exceptions: I'm throwing very general exceptions due to the nature of the JGit library. There are too many possible exception types to be conventiently handled individually
 * @author connor
 *
 */
public class GitRepo implements Comparable<GitRepo>
{
	private final String localPath, remotePath;
	private final Repository curRepo;
	private final Git git;
	
	/**
	 * Create a new repo
	 * @param local The local storage location
	 * @param remote The remote storage location
	 * @throws Exception
	 */
	public GitRepo(String local, String remote) throws Exception
	{
		this.localPath = local;
		this.remotePath = remote;
		
		curRepo = new FileRepository(localPath + "/.git");
		
		git = new Git(curRepo);
		
		File dir = new File(local);
		//Clone upon creation
		if (!dir.exists() || dir.listFiles().length == 0)
			gitClone();
	}
	
	/**
	 * Clone the remote repo
	 * @throws Exception
	 */
	public void gitClone() throws Exception
	{
		Git.cloneRepository()
			.setURI(remotePath)
			.setDirectory(new File(localPath))
			.call();
	}
	
	/**
	 * Pull/update repo
	 * @throws Exception
	 */
	public void gitPull() throws Exception
	{
		git.pull()
			.call();
	}
	
	/**
	 * Add a file to the git repo.
	 * @param file File to add, can be a directory
	 * @throws Exception
	 */
	public void gitAdd(File file) throws Exception
	{
		git.add()
			.addFilepattern(file.getAbsolutePath())
			.call();
	}
	
	/**
	 * Commit repo changes
	 * @param msg Message to include
	 * @throws Exception
	 */
	public void gitCommit(String msg) throws Exception
	{
		git.commit()
			.setMessage(msg)
			.setAll(true)
			.call();
	}
	
	/**
	 * Push changes to remote repo
	 * @throws Exception
	 */
	public void gitPush() throws Exception
	{
		git.push()
			.call();
	}
	
	/**
	 * Create a new git repo from a string representation
	 * @param str
	 * @return
	 */
	public static GitRepo fromString(String str) throws IOException
	{
		try
		{
			String local = str.substring(str.indexOf("[") + 1, str.indexOf(","));
			String remote = str.substring(str.indexOf(",") + 1, str.indexOf("]"));
			
			return new GitRepo(local, remote);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new InvalidSaveException("Save not valid: " + str);
		}
	}
	
	public String getLocalPath()
	{
		return localPath;
	}
	
	public String getRemotePath()
	{
		return remotePath;
	}
	
	/**
	 * Get the name of this repository. Local name overrides remote name.
	 * @return
	 */
	public String getRepoName()
	{
		return localPath.substring(localPath.lastIndexOf("/") + 1);
	}
	
	public String toString()
	{
		return "Repo[" + localPath + "," + remotePath + "]";
	}
	
	public static class InvalidSaveException extends RuntimeException
	{
		private static final long serialVersionUID = -8333930477590014670L;

		public InvalidSaveException(String s)
		{
			super(s);
		}
		
		public InvalidSaveException()
		{
		}
	}
	
	//Get the name of this repository from the remote path
	public static String getRepoNameFromRemote(String remote)
	{
		return remote.substring(remote.lastIndexOf("/") + 1, remote.lastIndexOf(".git"));
	}
	
	public boolean equals(Object o)
	{
		return o != null && o instanceof GitRepo && ((GitRepo)o).localPath.equals(localPath) && ((GitRepo)o).remotePath.equals(remotePath);
	}
	
	public int compareTo(GitRepo other)
	{
		int cmpName = localPath.compareTo(other.localPath);
		
		if (cmpName != 0)
			return cmpName;
		
		return remotePath.compareTo(other.remotePath);
	}
}
