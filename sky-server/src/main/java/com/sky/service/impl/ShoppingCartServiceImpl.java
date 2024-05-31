package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    //添加购物车
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //存在
        if(list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);  //Update
            shoppingCartMapper.updateById(cart);
            return;
        }
        //不存在
        //判断本次添加的时菜品还是套餐
        Long dishId = shoppingCartDTO.getDishId();

        if(dishId != null) {
            //本次是菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());

        }else {
            //本次是套餐
            Long setmealId = shoppingCart.getSetmealId();
            Setmeal setmeal = setmealMapper.getById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());

        }
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setNumber(1);

        shoppingCartMapper.insert(shoppingCart);

    }

    //查看购物车
    public List<ShoppingCart> showShoppingCart() {
        //获取当前微信用户Id
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId).build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    //清空购物车
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();

        shoppingCartMapper.deleteByUserId(userId);
    }



}

