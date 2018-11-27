package com.hb.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hb.web.dto.Message;


@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public Message errorHandler(Exception e) {
		LOGGER.error("error msg info: ", e);
		Message message = new Message();
		if (e instanceof ApiException) {
			ApiException apiException = (ApiException) e;
			message.setCode(apiException.getCode());
			message.setMsg(apiException.getMessage());
			return message;
		}
		message.setCode(ApiErrorCode.SERVICE_ERROR.getCode());
		message.setMsg("系统异常");
		return message;
	}
}
