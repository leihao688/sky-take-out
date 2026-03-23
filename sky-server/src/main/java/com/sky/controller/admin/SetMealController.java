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
    public Result delete(Long[]ids){
        log.info("正在进行批量删除{}",ids);
        setmealService.detele(ids);
        return Result.success();
    }
    @GetMapping("{/id}")
    @ApiOperation("根据ID查询套餐")
    public Result selectById(@PathVariable Long id){
        log.info("正在根据ID{}查询套餐",id);
        Setmeal setmeal = setmealService.selectById(id);
        return Result.success(setmeal);
    }
}
