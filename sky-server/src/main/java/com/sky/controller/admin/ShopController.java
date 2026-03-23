package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    private final static String KEY="SHOP_STATUS";
    @PutMapping("/{status}")
    @ApiOperation("修改商品状态")
    public Result updateStatus(@PathVariable Integer status){
        log.info("正在修改店铺状态{}",status==1?"营业中":"未营业");
        redisTemplate.opsForValue().set(KEY,status);
        log.info("Redis 存储成功，key={}, value={}", KEY, status);
        return Result.success();
    }

    @GetMapping()
    @ApiOperation("查看商铺状态")
    public Result<Integer> showStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get(KEY);
        log.info("正在查询店铺营业状态{}",status==1?"营业中":"未营业");
        return Result.success(status);
    }
}
