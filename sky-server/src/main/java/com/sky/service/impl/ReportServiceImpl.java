package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    //营业额统计
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //存放 begin 到 end 每一天
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        //计算指定日期的后一天
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String s = StringUtils.join(dateList, ",");

        //计算营业额
        //遍历日期
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询对应日期的金额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 :turnover;

            turnoverList.add(turnover);
        }

        //返回结果
        return TurnoverReportVO
                .builder()
                .dateList(s)
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    //统计用户
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //计算一天的用户数量
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天新增用户量
        List<Integer> newUserList = new ArrayList<>();
        //每天总的用户量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            HashMap map = new HashMap();
            map.put("beginTime", beginTime);
            Integer total =  userMapper.countByMap(map);
            map.put("endTime", endTime);
            Integer newUser =  userMapper.countByMap(map);

            totalUserList.add(total);
            newUserList.add(newUser);
        }



        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,",") )
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }
}
