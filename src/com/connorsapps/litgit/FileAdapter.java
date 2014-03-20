package com.connorsapps.litgit;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<File>
{
	public static final String[] FILE_SIZE_EXTENSIONS = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"};
	public static final Map<File, Long> SIZES = new HashMap<File, Long>();
	
	public FileAdapter(Context context, int resource, File[] objects)
	{
		//Invoke ArrayAdapter constructor
		super(context, resource, objects);
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		View mView = convertView;
		
		if (mView == null)
			mView = View.inflate(getContext(), R.layout.file_list_view_row, null);
		
		ImageView view = (ImageView)mView.findViewById(R.id.fileTypeImage);
		TextView txt = (TextView)mView.findViewById(R.id.fileName);
		TextView infoSize = (TextView)mView.findViewById(R.id.fileInfoSize);
		TextView infoMod = (TextView)mView.findViewById(R.id.fileInfoModified);
		
		
		File f = this.getItem(pos);
		
		view.setImageDrawable(mView.getResources().getDrawable(f.isDirectory() ? R.drawable.folder : R.drawable.file));
		txt.setText(f.getName());
		infoSize.setText(getFileSizeStr(f));
		infoMod.setText(getFileModStr(f));
		return mView;
	}
	
	private String getFileSizeStr(File f)
	{
		long length = recurseFileLength(f);
		int pos = 0;
		
		while (pos < FILE_SIZE_EXTENSIONS.length - 1 && length >= 1024)
		{
			length /= 1024;
			pos++;
		}
		
		return length + " " + FILE_SIZE_EXTENSIONS[pos];
	}
	
	/**
	 * Get the length of the file if a file, all files in directory if directory
	 * @param file
	 * @return
	 */
	public static long recurseFileLength(File file)
	{
		//Return size in cache if possible
		if (SIZES.containsKey(file))
			return SIZES.get(file);
		
		if (!file.isDirectory())
		{
			long length = file.length();
			
			//Add to cache
			SIZES.put(file, length);
			
			return length;
		}
		
		long length = 0;
		
		for (File f : file.listFiles())
			length += recurseFileLength(f);
		
		//Add to cache
		SIZES.put(file, length);
		
		return length;
	}
	
	private String getFileModStr(File f)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(f.lastModified());
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		return "Mod: " + getNumString(hour) + ":" + getNumString(min) + " " + getNumString(month) + "/" + getNumString(day) + "/" + year;
	}
	
	private String getNumString(int num)
	{
		return (num < 10 ? "0" + num : "" + num);
	}
}
