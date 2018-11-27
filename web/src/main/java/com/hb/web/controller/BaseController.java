package com.hb.web.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPObject;
import com.hb.web.dto.Message;

public class BaseController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final DateTimeFormatter fDate = DateTimeFormat.forPattern("yyyy-MM-dd");
	private final DateTimeFormatter fTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private final Pattern pNum = Pattern.compile("\\d+");
	private final Pattern pDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private final Pattern pTime = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport(){
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if(StringUtils.isEmpty(text)){
					setValue(null);
				}
				if(pNum.matcher(text).matches()){
					setValue(new Date(Long.valueOf(text)));
				}else if(pDate.matcher(text).matches()){
					setValue(fDate.parseDateTime(text).toDate());
				}else if(pTime.matcher(text).matches()){
					setValue(fTime.parseDateTime(text).toDate());
				}
			}
		});
		binder.registerCustomEditor(Timestamp.class, new PropertyEditorSupport(){
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if(StringUtils.isEmpty(text)){
					setValue(null);
				}
				if(pNum.matcher(text).matches()){
					setValue(new Timestamp(Long.valueOf(text)));
				}else if(pDate.matcher(text).matches()){
					setValue(new Timestamp(fDate.parseDateTime(text).getMillis()));
				}else if(pTime.matcher(text).matches()){
					setValue(new Timestamp(fTime.parseDateTime(text).getMillis()));
				}
			}
		});
	}

	public Message getResult(Integer code, String msg) {
		Message message = new Message();
		message.setCode(code);
		message.setMsg(msg);
		return message;
	}


	public Message getDataResult(Integer code, Object data) {
		Message message = new Message();
		message.setCode(code);
		message.setData(data);
		return message;
	}

	public Message getOkResult() {
		Message message = new Message();
		message.setCode(0);
		return message;
	}

	public Message getOkResult(Object data) {
		Message message = new Message();
		message.setCode(0);
		message.setData(data);
		return message;
	}

	public Message getOkResult(Object data, String msg) {
		Message message = new Message();
		message.setCode(0);
		message.setData(data);
		message.setMsg(msg);
		return message;
	}

	public Message getErrorMsgResult(Integer code, String msg) {
		Message message = new Message();
		message.setCode(code);
		message.setMsg(msg);
		return message;
	}

	protected void renderResponse(HttpServletResponse response, Object object) throws IOException {
		response.setContentType("application/json; charset=utf-8");
		String str = JSON.toJSONString(object);
		response.getWriter().println(str);
	}

	protected void renderResponse(HttpServletResponse response, Object object, String callback) throws IOException {
		response.setContentType("application/javascript; charset=utf-8");
		JSONPObject jsonp = new JSONPObject(callback);
		jsonp.addParameter(object);
		String str = jsonp.toJSONString();
		response.getWriter().println(str);
	}
}