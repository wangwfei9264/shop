package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MyTask {

    //定时任务 每隔5秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void myTask() {
        log.info("定时任务开始执行 {}",new Date());
    }



}
