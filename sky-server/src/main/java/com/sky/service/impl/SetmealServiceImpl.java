package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;


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
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void detele(Long[] ids) {
        List<Setmeal> setmealList = setmealMapper.getById(ids);
        setmealList.forEach(setmeal ->
        {if(setmeal.getStatus()== StatusConstant.ENABLE){
         throw new DeletionNotAllowedException("当前套餐正在售卖中，不能删除");
        }
        });
        setmealMapper.delete(ids);
        setMealDishMapper.delete(ids);
    }

    @Override
    public Setmeal selectById(Long id) {
        Setmeal setmeal= setmealMapper.selectById(id);
        return setmeal;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setMealDishMapper.delete(new Long[]{setmealDTO.getId()});
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(dish->{
                dish.setSetmealId(setmealDTO.getId());
            });
        }
        setMealDishMapper.insertBeach(setmealDishes);

    }

    @Override
    public void startOrStop(Integer status, Long id) {
       List<Long>ids=setMealDishMapper.selectById(id);
       List<Dish>dishList=dishMapper.selectByIds(ids);
       dishList.forEach(dish->{
           if(dish.getStatus()==StatusConstant.ENABLE){
               throw new DeletionNotAllowedException("当前有起售中的菜品，不能停售套餐");
           }
       });
       Setmeal setmeal= Setmeal.builder().id(id).status(status).build();
       setmealMapper.update(setmeal);

    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
