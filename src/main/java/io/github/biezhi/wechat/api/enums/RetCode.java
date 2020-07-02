package io.github.biezhi.wechat.api.enums;

import lombok.Getter;

/**
 * 心跳检查状态码
 *
 * @author biezhi
 * @date 2018/1/21
 */
@Getter
public enum RetCode {

    NORMAL(0, "正常"),
    NOT_LOGIN_CHECK(1101, "未检测到登录"),
    COOKIE_INVALID_ERROR(1102, "cookie值无效"),
    UNKNOWN(9999, "未知");

    private int    code;
    private String type;

    RetCode(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public static RetCode parse(int code) {
        switch (code) {
            case 0:
                return NORMAL;
            case 1102:
                return COOKIE_INVALID_ERROR;
            case 1101:
                return NOT_LOGIN_CHECK;
            default:
                return UNKNOWN;
        }
    }

}
