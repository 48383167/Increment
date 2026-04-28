package com.lin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    BUSINESS_ERROR(600, "业务异常"),
    UNAUTHORIZED(401, "未登录或令牌已过期"),
    FORBIDDEN(403, "权限不足");

    private final Integer code;
    private final String msg;
}
