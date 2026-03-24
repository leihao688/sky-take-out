package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {


    void delete(List<Long> ids);

    void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);


    DishVO selectById(Long id);

    void updateDishWithFlavor(DishDTO dishDTO);

    List<Dish> list(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
