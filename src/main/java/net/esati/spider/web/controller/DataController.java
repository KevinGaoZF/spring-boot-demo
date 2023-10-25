package net.esati.spider.web.controller;

import net.esati.spider.web.dao.OrderRepository;
import net.esati.spider.web.domain.Order;
import net.esati.spider.web.domain.RestResponse;
import net.esati.spider.web.util.RestResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version 1.0
 * @description: TODO
 */
// 注解
@RestController
@RequestMapping(value = "/data",produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class DataController {

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/login")
    public RestResponse<String> login(@RequestParam String userName ,@RequestParam String pwd ) {
        // 固定用户名，密码
        if( "admin".equals(userName) && ("1234").equals(pwd)){
            return RestResponseUtil.success("登录成功！",null);
        } else {
            return RestResponseUtil.error("用户名或密码错误！");
        }
    }


    @GetMapping("/list")
    public RestResponse<List<Order>> dataList() {
        return RestResponseUtil.success("接口请求成功！",orderRepository.selectOrder());
    }

    @GetMapping("/query-by-id")
    public RestResponse<List<Order>> getDataById(@RequestParam String dataId) {
        return RestResponseUtil.success("接口请求成功！",orderRepository.selectOrderById(dataId));
    }

    /**
     * 修改订单状态
     * @param orderId
     * @param status
     * @return
     */
    @PostMapping("/update")
    public RestResponse<Integer> getDataById(@RequestParam String orderId,@RequestParam Integer status) {
        return RestResponseUtil.success("接口请求成功！",orderRepository.updateOrderStatus(orderId,status));
    }


}