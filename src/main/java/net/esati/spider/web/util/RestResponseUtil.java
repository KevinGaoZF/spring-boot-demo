package net.esati.spider.web.util;

import net.esati.spider.web.domain.RestResponse;

import java.util.Date;

/**
 * @author Gao
 * @version 1.0
 * @description: 返回对象 工具类
 * @date 2023/5/8
 */

public class RestResponseUtil {

    public static <T> RestResponse<T> success(){
        return success(null);
    }

    public static <T> RestResponse<T> success(T data){
        return success("响应成功！",data);
    }

    public static <T> RestResponse<T> success(String message, T data){
        return new RestResponse<T>(200,message,data);
    }

    public static <T> RestResponse<T> error(){
        return error(null);
    }

    public static <T> RestResponse<T> error(T data){
        return error("服务出错，请稍后重试！ ",data);
    }

    public static <T> RestResponse<T> error(String message){
        return error(message,null);
    }

    public static <T> RestResponse<T> error(String message, T data){
        return new RestResponse<T>(505,message,data);
    }

    public static void main(String[] args) {
        System.out.println(error());
    }

}
