package com.hb.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hb.web.dto.Message;

import io.swagger.annotations.Api;

@Api(value = "DEMO接口")
@RestController
@RequestMapping(value = "/api/v1")
public class DemoController extends BaseController {

	@ResponseBody
	@RequestMapping(value = "/demo", method = RequestMethod.GET)
	Message demo(@RequestParam(value = "ucId", required = true) Long ucId) {
		Message message = new Message();
		message.setData(null);
		message.setCode(0);
		message.setMsg("success");
		return message;
	}
}
