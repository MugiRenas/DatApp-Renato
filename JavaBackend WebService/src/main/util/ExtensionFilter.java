package main.util;

import java.io.File;

public class ExtensionFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
	
//Attributes
	
	private String ext[];
	private String desc;
	private boolean listDirectories;
	
//Constructors
	
	public ExtensionFilter(String d, String e, boolean dir)
	{
		desc = d;
		ext = new String[] { e };
    	listDirectories = dir;
    }

    public ExtensionFilter(String d, String[] e, boolean dir)
    {
    	desc = d;
    	ext = (String[]) e.clone();
    	listDirectories = dir;
    }

//Public Methods
    
    @Override
    public boolean accept(File file)
    {
		if(file.isDirectory())
    		return listDirectories;
		int count = ext.length;
		String path = file.getAbsolutePath();
		for(int i = 0; i < count; i++)
		{
			String s = ext[i];
			if(path.endsWith(s) && (path.charAt(path.length() - s.length()) == '.'))
				return true;
		}
		return false;
    }

    public String getDescription()
    {
    	if(desc == null)
    		return ext[0];
   		return desc;
    }
}