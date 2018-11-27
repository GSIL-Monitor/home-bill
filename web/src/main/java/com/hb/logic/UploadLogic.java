package com.hb.logic;

import java.io.File;

public interface UploadLogic {
	public int uploadFile(File f,String s3path);
	
	public int uploadDirectory(File dir,String s3dir);
}
