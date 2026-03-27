package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.dialect.helper.HsqldbDialect;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.webService.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private OrderService orderService;
    @Autowired
    WebSocketServer webSocketServer;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //获取当前用户ID以及地址簿和购物车信息
        Long userId = BaseContext.getCurrentId();
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook address = addressBookMapper.getById(addressBookId);
        if(address==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart=ShoppingCart.builder()
                                  .userId(userId)
                                  .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.select(shoppingCart);
        if(shoppingCarts==null||shoppingCarts.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //创建订单设置订单相关参数，（注意要返回订单ID已插入订单详细表）
        Orders order=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setAddress(address.getDetail());
        order.setOrderTime(LocalDateTime.now());
        order.setConsignee(address.getConsignee());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPhone(address.getPhone());
        order.setUserId(userId);
        String orderNumber = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%02d", (int)(Math.random() * 100));
        order.setNumber(orderNumber);
        orderMapper.insert(order);
        //插入订单详细
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        }
        orderMapper.insertBeach(orderDetails);
        //清空购物车
        shoppingCartMapper.delete(userId);

        //创建VO对象
        OrderSubmitVO orderSubmitVO= OrderSubmitVO.builder()
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .id(order.getId())
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
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

    }

    @Override
    public PageResult page(int page, int pageSize,Integer status) {
        //先将order的信息查询出来
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        List<Orders> orders = orderMapper.page(ordersPageQueryDTO);
        Page<Orders> pageOrder =  (Page<Orders>)orders;
        //将OrderVO列表进行封装
        ArrayList<OrderVO>OrderVoList = new ArrayList<>();
        if(pageOrder!=null&&pageOrder.size()>0){
        for (Orders order : pageOrder) {
        Long orderId = order.getId();
        //通过orderId查询订单详情
        List<OrderDetail> orderDetails = orderMapper.getByOrderId(orderId);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        OrderVoList.add(orderVO);

        }}
        return new PageResult(pageOrder.getTotal(),OrderVoList);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
       Orders orders=orderMapper.getById(id);
       List<OrderDetail>orderDetailList=orderMapper.getByOrderId(id);
       OrderVO orderVO=new OrderVO();
       BeanUtils.copyProperties(orders,orderVO);
       orderVO.setOrderDetailList(orderDetailList);
       return orderVO;

    }

    @Override
    public void cancel(Long id) throws Exception {
        //获取订单信息
        Orders orders=orderMapper.getById(id);
        Integer status=orders.getStatus();
        if(status==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (status>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(status.equals(Orders.TO_BE_CONFIRMED)){
            weChatPayUtil.refund(
                    orders.getNumber(),//商品订单号
                    orders.getNumber(),//微信支付订单号
                    new BigDecimal(0.01),//退款金额
                    new BigDecimal(0.01));//订单金额

            orders.setPayStatus(Orders.REFUND);}
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("用户取消");
            orders.setCancelTime(LocalDateTime.now());

    }

    @Override
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail>orderDetails=orderMapper.getByOrderId(id);
        //通过map映射将订单的详细数据转化为购物车数据并返回
        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(userId);
            return shoppingCart;

        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCarts);


    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders>ordersList=orderMapper.conditionSearch(ordersPageQueryDTO);
        Page<Orders>page= (Page<Orders>) ordersList;
        ArrayList<OrderVO> ordersPageList=new ArrayList<>();
        for (Orders orders : ordersList) {

            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);

            List<OrderDetail> ODList = orderMapper.getByOrderId(orders.getId());
            String orderDish = orderDishStr(ODList);
            orderVO.setOrderDishes(orderDish);
            ordersPageList.add(orderVO);
        }
        return new PageResult(page.getTotal(),ordersPageList);
    }
    public String orderDishStr(List<OrderDetail> ODList){
        List<String> str = ODList.stream().map(x -> {
            String dish = x.getName() + "*" + x.getNumber();
            return dish;
        }).collect(Collectors.toList());
        return String.join("",str);
    }

    @Override
    public OrderStatisticsVO getCount() {
       Integer tobeConfirmed = orderMapper.getCount(Orders.TO_BE_CONFIRMED);
       Integer confirmed = orderMapper.getCount(Orders.CONFIRMED);
       Integer deliveryInProgress = orderMapper.getCount(Orders.DELIVERY_IN_PROGRESS);
       OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
       orderStatisticsVO.setConfirmed(confirmed);
       orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
       orderStatisticsVO.setToBeConfirmed(tobeConfirmed);
       return orderStatisticsVO;
    }

    @Override
    public OrderVO getOrderDetailById(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetail = orderMapper.getByOrderId(id);
        String orderDishStr = orderDishStr(orderDetail);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDishes(orderDishStr);
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order=Orders.builder().id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders DB=orderMapper.getById(ordersRejectionDTO.getId());
        if(!(DB.getStatus().equals(Orders.TO_BE_CONFIRMED))||DB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer payStatus=DB.getPayStatus();
        if (payStatus.equals(Orders.PAID)){
        weChatPayUtil.refund(
                DB.getNumber(),//商品订单号
                DB.getNumber(),//微信支付订单号
                new BigDecimal(0.01),//退款金额
                new BigDecimal(0.01));//订单金额

                DB.setPayStatus(Orders.REFUND);
                DB.setStatus(Orders.CANCELLED);
                DB.setCancelReason("用户取消");
                DB.setCancelTime(LocalDateTime.now());}

    }

    @Override
    public void delivery(Long id) {
        Orders orders=orderMapper.getById(id);
        if(orders==null || orders.getStatus()!=Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders=orderMapper.getById(id);
        if(!(orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))||orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders=orderMapper.getById(id);
        if(!(orders.getStatus().equals(Orders.TO_BE_CONFIRMED))||orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map=new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号："+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
