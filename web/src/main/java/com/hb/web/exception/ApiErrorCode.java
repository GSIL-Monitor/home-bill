package com.hb.web.exception;

public enum ApiErrorCode {
	
	SUCCESS(1, "操作成功"),
	FAILURE(-1, "操作失败"),
	
	PARA_NULL(101, "缺少必填参数"),
	PARA_INVALID(102,"参数不合法"),
	NO_DATA_FOUND(103,"没有找到相关数据"),

    SERVICE_ERROR(500, "服务内部错误"),
    
    NOT_AUTHED(10001, "未授权"),
    PARAM_LENGTH_NOT_LEGAL(10002, "字符串长度不合法"),
    DATA_NOT_EXIST(10001, "数据不存在"),
    ENUM_CODE_NOT_EXIST(10003, "此枚举数据不存在"),
    MOBILE_INVALID_ERROR(10004, "手机号不合法"),
    PARAM_BLANK_ERROR(10005, "参数为空"),
    PARAM_INT_CAN_NOT_NEGATIVE(10006, "数字不能为负值"),
    PARAM_DATE_FORMAT_ERROR(10007, "日期格式不正确"),
    DATA_SAVE_FAIL(10008,"数据保存失败"),
    DATA_INIT_FAIL(10009,"数据初始化失败"),
    DATA_DELETE_FAIL(10010,"数据删除失败"),
    DATA_SERACH_FAIL(10011,"数据查询失败"),
    ANNOTATION_ILLEGAL_USE(10012,"注解不合法"),
    INVOKE_REMOTE_API_ERROR(10013,"调用API错误"),
    DATA_NOT_PRECISE(10014,"参数不精确"),
    UC_SERVICE_ERROR(-1110,"UC服务异常"),
    ;

    private Integer code;
    private String desc;

    ApiErrorCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
