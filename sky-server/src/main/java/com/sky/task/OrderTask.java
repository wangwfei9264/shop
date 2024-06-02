package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    //处理超时订单
    @Scheduled(cron = "0 * * * * ?")    //每分钟触发一次
    //@Scheduled(cron = "0/5 * * * * ?")    //每5秒触发一次
    public void processTimeOut(){
        log.info("定时处理超时订单 {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status = ? and order_time < 15m (当前时间 - 15 分钟)
        List<Orders> orderList = orderMapper.getStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if(orderList != null && orderList.size() > 0){
            for(Orders order : orderList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }

    }

    //处理一直处于派送中的订单
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理一直处于派送中的订单 {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = orderMapper.getStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(orderList != null && orderList.size() > 0){
            for(Orders order : orderList){
                order.setStatus(Orders.COMPLETED);
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }

    }


}
