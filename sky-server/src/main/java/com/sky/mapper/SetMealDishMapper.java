package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    public List<Long> selectByIds(List<Long> ids) ;

    void insertBeach(List<SetmealDish> setmealDishes);

    void delete(Long[] ids);
}
