package com.hb.logic.impl;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.hb.logic.LoadFinishFile;
import com.hb.logic.UploadLogic;
import com.hb.web.exception.ApiException;

@Service
public class UploadLogicAsyncImpl implements UploadLogic {
	private static final Logger logger = LoggerFactory.getLogger(UploadLogicAsyncImpl.class);

	private int THREAD_NUM = 40;
	private Semaphore s = new Semaphore(THREAD_NUM);
	private ExecutorService es = Executors.newFixedThreadPool(THREAD_NUM);

	@Resource(name = "uploadLogicImpl")
	private UploadLogic uploadLogic;

	public int uploadFile(final File f, final String s3path) {
		if (LoadFinishFile.FINISH_FILE_NAME.contains(f.getAbsolutePath())) {
			return 0;
		}
		try {
			s.acquire();
			final String dirName = MDC.get("dirName");
			es.submit(new Callable<Object>() {
				public Object call() throws Exception {
					try {
						MDC.put("dirName",dirName);
						uploadLogic.uploadFile(f, s3path);
					} finally {
						s.release();
					}
					return null;
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
		return 1;
	}

	public int uploadDirectory(File dir, String s3dir) {
		if(!dir.exists()) {
			throw new ApiException(2,dir.getAbsolutePath()+" not exists!");
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
