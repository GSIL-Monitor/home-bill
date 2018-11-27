package com.hb.logic.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.hb.logic.LoadFinishFile;
import com.hb.logic.UploadLogic;
import com.hb.web.exception.ApiException;
import com.hb.web.util.AmazonS3;

@Service
public class UploadLogicImpl implements UploadLogic {
	private static final Logger logger = LoggerFactory.getLogger(UploadLogicImpl.class);

	private String bucketName = "fngte-image";
	private boolean isPublicRead = true;

	public int uploadFile(File f, String s3path) {
		try {
			if (LoadFinishFile.FINISH_FILE_NAME.contains(f.getAbsolutePath())) {
				return 0;
			}

			AmazonS3.upload(bucketName, s3path, f, isPublicRead);
			//			System.out.println("upload:" + f + " path:" + s3path);

			StringBuilder logName = new StringBuilder();
			logName.append(LoadFinishFile.FINISH_PREFIX);
			String dirName = MDC.get("dirName");
			if (StringUtils.isNotBlank(dirName)) {
				logName.append(dirName).append(".");
			}
			logName.append(Thread.currentThread().getId());
			MDC.put("logName", logName.toString());
			logger.info(f.getAbsolutePath());
		} catch (Exception e) {
			StringBuilder logName = new StringBuilder();
			logName.append("error.");
			logName.append(LoadFinishFile.FINISH_PREFIX);
			String dirName = MDC.get("dirName");
			if (StringUtils.isNotBlank(dirName)) {
				logName.append(dirName).append(".");
			}
			logName.append(Thread.currentThread().getId());
			MDC.put("logName", logName.toString());
			logger.info(f.getAbsolutePath());
			return 0;
		}
		return 1;
	}

	public int uploadDirectory(File dir, String s3dir) {
		if (!dir.exists()) {
			throw new ApiException(2, dir.getAbsolutePath() + " not exists!");
		}

		File[] file = dir.listFiles();
		int num = 0;
		for (File f : file) {
			if (f.isDirectory()) {
				num += uploadDirectory(f, s3dir + "/" + f.getName());
			} else {
				num += uploadFile(f, s3dir + "/" + f.getName());
			}
		}
		return num;
	}

}
