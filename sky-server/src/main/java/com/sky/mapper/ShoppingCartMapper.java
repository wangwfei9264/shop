package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //动态条件查询
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number} where id=#{id}")
    void updateById(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, amount,image,create_time) " +
            "VALUES (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    //根据用户Id删除购物车
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);
}
