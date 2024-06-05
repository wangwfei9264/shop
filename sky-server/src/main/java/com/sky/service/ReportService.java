package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
