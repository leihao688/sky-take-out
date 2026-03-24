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
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return new PageResult(dishPage.getTotal(),dishPage.getResult());
    }

    @Override
    public DishVO selectById(Long id) {
        Dish dish=dishMapper.selectById(id);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        List<DishFlavor> flavors= flavorMapper.selectByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void updateDishWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //先删除菜品的口味再添加
        flavorMapper.deleteByDishIdOne(dishDTO.getId());
        if(dishDTO.getFlavors()!=null && dishDTO.getFlavors().size()>0){
            dishDTO.getFlavors().forEach(flavor->{flavor.setDishId(dishDTO.getId());});
        }
        //添加
        flavorMapper.InsertBeach(dishDTO.getFlavors());

    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish= Dish.builder().
                categoryId(categoryId).
                status(StatusConstant.ENABLE).
                build();
        List<Dish> dishList = dishMapper.list(dish);
        return dishList;

    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = flavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
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
