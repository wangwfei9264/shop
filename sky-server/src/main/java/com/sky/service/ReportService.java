package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);
}
