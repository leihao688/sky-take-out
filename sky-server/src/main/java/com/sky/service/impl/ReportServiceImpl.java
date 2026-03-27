package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
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
    private OrderMapper orderMapper;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.isAfter(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        turnoverReportVO.setDateList(StringUtils.join(dateList,","));
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime localDateBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",localDateBegin);
            map.put("end",localDateEnd);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sum(map);
            turnover = turnover == null ? 0.0 : turnover;

            turnoverList.add(turnover);
        }

        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        return turnoverReportVO;

    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.isAfter(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        userReportVO.setDateList(StringUtils.join(dateList,","));
        List<Integer> userNewList = new ArrayList<>();
        List<Integer> userTotalList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime localDateBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            Map mapnew = new HashMap();
            mapnew.put("begin",localDateBegin);
            mapnew.put("end",localDateEnd);
            mapnew.put("today",localDate);
            Integer newUser = orderMapper.getUserCount(mapnew);
            userNewList.add(newUser);
            Map maptotal = new HashMap();
            maptotal.put("begin",localDateBegin);
            maptotal.put("end",localDateEnd);
            Integer totalUser = orderMapper.getUserCount(maptotal);
            userTotalList.add(totalUser);

        }
//        System.out.println(userTotalList);
//        System.out.println(userNewList);
        userReportVO.setTotalUserList(StringUtils.join(userTotalList,","));
        userReportVO.setNewUserList(StringUtils.join(userNewList,","));
        return userReportVO;
    }
}
