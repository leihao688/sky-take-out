package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
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
