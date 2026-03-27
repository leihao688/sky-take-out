package com.sky.controller.admin;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.DishVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@Api(tags = "管理端的点单接口")
@RequestMapping("admin/order")
public class OrderController {
    @Autowired
     private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("条件搜索订单")
    public Result<PageResult> conditionSearch( OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("正在搜索订单");
        PageResult pageResult=orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/statistics")
    @ApiOperation("订单统计")
    public Result<OrderStatisticsVO>getCount(){
        log.info("订单统计");
        OrderStatisticsVO orderStatisticsVO = orderService.getCount();
        System.out.println(orderStatisticsVO);
        return Result.success(orderStatisticsVO);
    }
    @GetMapping("/details/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO>getOrderDetail(@PathVariable Long id){
        log.info("订单详情");
        OrderVO orderVO=orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("确认订单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("正在确认订单");
        orderService.confirm(ordersConfirmDTO);
        return Result.success();

    }
    @PutMapping("/rejection")
    @ApiOperation("拒绝订单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("正在拒绝订单");
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("正在派送订单");
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("正在完成订单");
        orderService.complete(id);
        return Result.success();
    }

}
