package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;


    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {
        //处理业务异常
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //抛出异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(cart);
        if(list == null || list.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList = new ArrayList();
        //向订单明细表插入n条数据
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //封装Vo返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id

        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Long Number = Long.parseLong(orderNumber);
        Long id = orderMapper.getIdByNumber(Number);
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        /*//调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/

//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        return null;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type",1); // 1表示来单提醒 2表示客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    //历史订单
    public PageResult pageQuery(int page, int pageSize, Integer status) {
        //准备分页查询
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> pageList = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //封装Vo返回

        if(pageList != null && pageList.getTotal() > 0){
            for (Orders orders : pageList) {
                Long userId = orders.getUserId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getDetailById(userId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetailList);

                list.add(orderVO);
            }
        }

        return new PageResult(pageList.getTotal(),list);
    }

    //取消订单
    public void cancelOrder(Long id) {

        Orders orders = Orders.builder()
                .id(new Long(id))
                .status(Orders.CANCELLED)
                .payStatus(Orders.REFUND)
                .cancelTime(LocalDateTime.now())
                .cancelReason("用户取消")
                .build();

        //可能需要退款

        orderMapper.update(orders);
    }

    //查询订单详情
    public OrderVO getDetailById(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> detailById = orderDetailMapper.getDetailById(orders.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(detailById);

        return orderVO;
    }

    //再下一单
    public void repetiton(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setOrderTime(LocalDateTime.now());
        order.setCancelReason(null);
        order.setId(null);
        order.setPayStatus(Orders.UN_PAID);

        OrdersSubmitDTO orderSubmitDTO = new OrdersSubmitDTO();
        BeanUtils.copyProperties(order,orderSubmitDTO);

        //处理业务异常
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //抛出异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //向订单表插入一条数据
        orderMapper.insert(order);
        //向订单明细表插入n条数据
        List<OrderDetail> detail = orderDetailMapper.getDetailById(id);
        for (OrderDetail orderDetail : detail) {
            orderDetail.setId(order.getId());
        }
        orderDetailMapper.insertBatch(detail);

    }

    //订单分页 及其 搜索
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页 PageHelper 设置
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //搜索数据 并 返回 查询数量
        Page<Orders> list = orderMapper.pageQuery(ordersPageQueryDTO);

        //封装结果返回 total result 补充信息
        List<OrderVO> listVO = getOrderVOList(list);

        return new  PageResult(list.getTotal(),listVO);
    }

    //统计订单
    public OrderStatisticsVO getStatusTotal() {
        Page<Orders> orders = orderMapper.pageQuery(new OrdersPageQueryDTO());
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(0);
        orderStatisticsVO.setDeliveryInProgress(0);
        orderStatisticsVO.setToBeConfirmed(0);

        for (Orders order : orders) {
            if(order.getStatus().equals(Orders.CONFIRMED)){
                orderStatisticsVO.setConfirmed(orderStatisticsVO.getConfirmed()+1);
            } else if (order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
                orderStatisticsVO.setDeliveryInProgress(orderStatisticsVO.getDeliveryInProgress()+1);
            } else if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                orderStatisticsVO.setToBeConfirmed(orderStatisticsVO.getToBeConfirmed()+1);
            }
        }
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //改变订单状态
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(ordersConfirmDTO.getStatus());
        orderMapper.update(orders);
    }

   //拒单
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    //取消订单
    public void cancel(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    //派送订单
    public void delivery(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    //完成订单
    public void complete(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> list) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> orders = list.getResult();
        if(!CollectionUtils.isEmpty(orders)){
            for (Orders order : orders) {
                //相同字段赋值
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                String orderDishes =  getOrderDishesStr(order);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    private String getOrderDishesStr(Orders order) {
        //查询订单具体菜品名称
        List<OrderDetail> detail = orderDetailMapper.getDetailByOrderId(order.getId());
        //将菜品名称拼接
        List<String> orderDishList = detail.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join(";",orderDishList);
    }


}
