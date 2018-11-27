package com.hb.logic.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.hb.logic.LoadFinishFile;

@Service
public class LoadFinishFileImpl implements LoadFinishFile {

	public void loadFinish(String logPath, final String dirName) throws IOException {
		FINISH_FILE_NAME.clear();

		File base = new File(logPath);
		File[] arr = base.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.indexOf(FINISH_PREFIX + dirName) == 0) {
					return true;
				}
				return false;
			}
		});

		for (File f : arr) {
			InputStream is = new FileInputStream(f);
			List<String> lst = IOUtils.readLines(is, "UTF-8");
			FINISH_FILE_NAME.addAll(lst);
			IOUtils.closeQuietly(is);
		}
	}

	public void loadFinish(String logPath) throws IOException {
		loadFinish(logPath,"");
	}

}
