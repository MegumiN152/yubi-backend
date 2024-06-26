package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 黄昊
 * @version 1.0
 **/
@RestController
@Slf4j
@RequestMapping("/queue")
@Profile({"dev","local"})
public class QueueController {
    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name){
        CompletableFuture.runAsync(()->{
            log.info("任务执行中："+name+",任务执行线程："+Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },threadPoolExecutor);
    }
    @GetMapping("/get")
    public String get(){
        HashMap<String,Object> hashMap = new HashMap();
        int size=threadPoolExecutor.getQueue().size();
        hashMap.put("队列长度",size);
        long taskCount = threadPoolExecutor.getTaskCount();
        hashMap.put("任务数量",taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        hashMap.put("已完成任务数",completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        hashMap.put("正在工作的线程数",activeCount);
        return JSONUtil.toJsonStr(hashMap);
    }
}
