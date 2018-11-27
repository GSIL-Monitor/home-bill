package com.hb.logic;

import java.io.IOException;
import java.util.HashSet;

public interface LoadFinishFile {
	public static String FINISH_PREFIX = "finish.";
	
	public static HashSet<String> FINISH_FILE_NAME=new HashSet<String>();

	public void loadFinish(String logPath) throws IOException;
	
	public void loadFinish(String logPath,String dirName) throws IOException;
}
