package com.hb.web.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ApiException extends RuntimeException {
	private static final long serialVersionUID = -2946975633299532589L;

	private Integer code;
	private String message;
	private String trace;

	public ApiException(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public ApiException(Integer code, String message, Exception e) {
		this.code = code;
		this.message = message;
		if (e != null) {
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(fos));
			this.trace = fos.toString();
		}
	}

	public ApiException(Exception e) {
		this.code = ApiErrorCode.SERVICE_ERROR.getCode();
		this.message = ApiErrorCode.SERVICE_ERROR.getDesc();
		if (e != null) {
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(fos));
			this.trace = fos.toString();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("ApiException :{");
		sb.append("\"code\":").append(code).append(",");
		sb.append("\"message\":\"").append(message).append("\",");
		sb.append("\"stackTrace\":\"").append(trace).append("\"}");
		return sb.toString();
	}

	public Integer getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getTrace() {
		return trace;
	}

}