package com.sky.Task;

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
    @Scheduled(cron = "0 * * * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")
    public void processOrderTimeOut() {
        log.info("处理超时订单");
        LocalDateTime Time=LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.selectByTimeandStatus(Orders.PENDING_PAYMENT, Time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("支付超时");
                orderMapper.update(orders);
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")
    public void processCancelTimeOut() {
        log.info("处理取消订单");
        LocalDateTime Time=LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.selectByTimeandStatus(Orders.DELIVERY_IN_PROGRESS, Time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
