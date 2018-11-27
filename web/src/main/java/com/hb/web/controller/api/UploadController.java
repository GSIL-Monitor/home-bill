package com.hb.web.controller.api;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hb.logic.LoadFinishFile;
import com.hb.logic.UploadLogic;
import com.hb.web.controller.BaseController;
import com.hb.web.dto.Message;

import io.swagger.annotations.Api;

@Api(value = "上传接口")
@RestController
@RequestMapping(value = "/api")
public class UploadController extends BaseController {
	@Resource(name = "uploadLogicAsyncImpl")
	private UploadLogic uploadLogic;

	@Resource
	private LoadFinishFile loadFinishFile;

	@Value("${logging.path}")
	private String logPath;

	@ResponseBody
	@RequestMapping(value = "/uploadFullPath", method = RequestMethod.GET)
	Message uploadFullPath(String localDirPath, String s3path) throws IOException {
		logger.info("begin upload:localPath:{},s3path:{}", localDirPath, s3path);

		loadFinishFile.loadFinish(logPath);

		int num = uploadLogic.uploadDirectory(new File(localDirPath), s3path);

		Message message = new Message();
		message.setData(null);
		message.setCode(num);
		message.setMsg("success");
		return message;
	}

	@ResponseBody
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	Message upload(final String dirName) throws IOException {
		final String baseLocalPath = "/stpic/";
		final String baseS3Path = "/ste-web/file/pics/";
		final String localPath = baseLocalPath + dirName;
		final String s3Path = baseS3Path + dirName;

		Runnable r = new Runnable() {
			public void run() {
				logger.info("begin upload:localPath:{},s3path:{}", localPath, s3Path);
				MDC.put("dirName", dirName);
				try {
					loadFinishFile.loadFinish(logPath, dirName);

					uploadLogic.uploadDirectory(new File(localPath), s3Path);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				logger.info("finish upload:localPath:{},s3path:{}", localPath, s3Path);
			}
		};
		new Thread(r).start();

		Message message = new Message();
		message.setData(null);
		message.setCode(0);
		message.setMsg("success");
		return message;
	}

}
