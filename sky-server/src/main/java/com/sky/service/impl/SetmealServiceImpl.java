package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setMeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setMeal);

        setmealMapper.insert(setMeal);
        Long setId=setMeal.getId();
        if(setmealDTO.getSetmealDishes()!=null && setmealDTO.getSetmealDishes().size()>0){
            setmealDTO.getSetmealDishes().forEach(dish->{
                dish.setSetmealId(setId);
            });

        }
        setMealDishMapper.insertBeach(setmealDTO.getSetmealDishes());


    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        List<Setmeal> setmealList=setmealMapper.page(setmealPageQueryDTO);
        Page<Setmeal>setmealPage=(Page<Setmeal>)setmealList;
        return new PageResult(setmealPage.getTotal(),setmealPage.getResult());

    }
}
