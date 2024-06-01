package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order")
@Api("搜索订单")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping("/conditionSearch")
    public Result orderSearch(OrdersPageQueryDTO ordersPageQueryDTO){

         //orderService.orderSearch(ordersPageQueryDTO);


        return Result.success();
    }

}
