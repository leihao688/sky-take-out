package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    void insertBeach(ArrayList<OrderDetail> orderDetails);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    List<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    List<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    @Select("select count(*) from orders where status=#{status}")
    Integer getCount(Integer  status);
    @Select("select * from orders where order_time < #{time} and status=#{status}")
    List<Orders> selectByTimeandStatus(Integer status, LocalDateTime time);

    Double sum(Map map);
}
