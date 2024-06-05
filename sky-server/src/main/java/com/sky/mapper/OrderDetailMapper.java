package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    void insertBatch(List<OrderDetail> orderDetailList);

    @Select("select * from order_detail where id = #{id}")
    List<OrderDetail> getDetailById(Long id);

    @Select("select * from order_detail where id=#{id}")
    List<OrderDetail> getDetailByOrderId(Long id);


    List<GoodsSalesDTO> getDishTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
