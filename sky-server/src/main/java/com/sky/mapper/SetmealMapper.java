package com.sky.mapper;

import com.sky.annotation.autoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    @autoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    List<Setmeal> page(SetmealPageQueryDTO setmealPageQueryDTO);

    void delete(Long[] ids);


    List<Setmeal> getById(Long[] ids);
    @Select("select * from setmeal where id = #{id}")
    Setmeal selectById(Long id);
    @autoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    List<Setmeal> list(Setmeal setmeal);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long id);
}
