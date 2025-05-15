package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 黄昊
 * @version 1.0
 **/
@SpringBootTest
class ChartServiceImplTest {
    @Resource
    private ChartService chartService;

    @Test
    void dropCollection() {
    }

    @Test
    void saveDataFromString() {
        String text="日期,用户数\n" +
                "1号,10\n" +
                "2号,12\n" +
                "3号,20";
        chartService.saveDataFromString(text,"chart_"+1111);
    }
}