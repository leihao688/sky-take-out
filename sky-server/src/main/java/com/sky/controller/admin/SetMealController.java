package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "套餐相关接口")
@RestController
@RequestMapping("/admin/setmeal")
public class SetMealController {
    @Autowired
    private SetmealService setmealService;
    @PostMapping()
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("正在新增套餐{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult>page ( SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("正在进行分页查询{}",setmealPageQueryDTO);
        PageResult list=setmealService.page(setmealPageQueryDTO);
        return Result.success(list);
    }
    @DeleteMapping
    @ApiOperation("批量删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(Long[]ids){
        log.info("正在进行批量删除{}",ids);
        setmealService.detele(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐")
    public Result selectById(@PathVariable Long id){
        log.info("正在根据ID{}查询套餐",id);
        Setmeal setmeal = setmealService.selectById(id);
        return Result.success(setmeal);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("正在修改套餐{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("起售、停售套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result StartOrStop(@PathVariable Integer status,Long id){
        log.info("正在修改套餐{}",id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
