package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("admin/dish")
@Api(tags = "菜品管理")
public class DishController {

    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("正在添加菜品{}",dishDTO);
        dishService.save(dishDTO);
        return Result.success();

    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result page(DishPageQueryDTO dishPageQueryDTO){
        log.info("正在进行分页查询{}",dishPageQueryDTO);
        PageResult dishList =dishService.page(dishPageQueryDTO);
        return Result.success(dishList);
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("正在批量删除菜品{}",ids);
        dishService.delete(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("正在查询菜品{}",id);
        DishVO dish = dishService.selectById(id);
        return Result.success(dish);
    }
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("正在修改菜品{}",dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("按照类别查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("正在按照类别查询菜品{}",categoryId);
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }

}
