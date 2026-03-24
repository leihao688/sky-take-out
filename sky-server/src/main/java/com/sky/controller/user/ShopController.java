package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商铺相关操作")
@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    private final static String KEY="SHOP_STATUS";
    @GetMapping("/status")
    @ApiOperation("查看商铺状态")
    public Result<Integer> showStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get(KEY);
        log.info("正在查询店铺营业状态{}",status==1?"营业中":"未营业");
        return Result.success(status);
    }
}
