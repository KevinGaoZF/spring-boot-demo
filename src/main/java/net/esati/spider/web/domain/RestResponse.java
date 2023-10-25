package net.esati.spider.web.domain;

import java.io.Serializable;

/**
 * @author Gao
 * @version 1.0
 * @description: 返回对象类
 * @date 2023/5/8
 */

public class RestResponse<T> implements Serializable {

    // http 状态码
    private Integer code;

    // 返回信息
    private String msg;

    // 返回数据
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RestResponse {" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public RestResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
