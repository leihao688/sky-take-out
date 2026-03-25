package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
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
}
