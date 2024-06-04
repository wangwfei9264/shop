package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) throws Exception;

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    //历史订单查询
    PageResult pageQuery(int page, int pageSize,Integer status);

    void cancelOrder(Long id);

    OrderVO getDetailById(Long id);

    //
    void repetiton(Long id);


    PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    OrderStatisticsVO getStatusTotal();
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersRejectionDTO ordersRejectionDTO);

    void delivery(Long id);

    void complete(Long id);
}
