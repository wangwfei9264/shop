package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.orderSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    //统计订单情况
    @GetMapping("/statistics")
    public Result getTotal(){
         OrderStatisticsVO orderStatisticsVO = orderService.getStatusTotal();
        return Result.success(orderStatisticsVO);
    }

    //查询订单详情
    @GetMapping("/details/{id}")
    public Result details(@PathVariable Long id) {
        OrderVO detail = orderService.getDetailById(id);
        return Result.success(detail);
    }

    //接单
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    //拒单
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }

    //取消订单
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.cancel(ordersRejectionDTO);
        return Result.success();
    }

    //派送订单
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    //完成订单
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
