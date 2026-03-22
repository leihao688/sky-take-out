package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private FlavorMapper flavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;


    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        List<Dish>dishList=dishMapper.page(dishPageQueryDTO);
        Page<Dish> dishPage=(Page<Dish>) dishList;
        return new PageResult(dishPage.getPages(),dishPage.getResult());
    }
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void delete(List<Long> ids) {
       List<Dish>dishList = dishMapper.selectByIds(ids);
        for (Dish dish : dishList) {
           if (dish.getStatus() == StatusConstant.ENABLE){
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }
        List<Long>count=setMealDishMapper.selectByIds(ids);
        if(count!=null && count.size()!=0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.deleteById(ids);
        flavorMapper.deleteByDishId(ids);


    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.Insert(dish);
        List<DishFlavor> flavorList=dishDTO.getFlavors();
        Long id=dish.getId();

        if(flavorList !=null && flavorList.size()>0){
            flavorList.forEach(flavor->{flavor.setDishId(id);});
        }
        flavorMapper.InsertBeach(flavorList);

    }
}
