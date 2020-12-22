package com.zut.lpf.schedule;

import com.zut.lpf.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MychatSchedule {

    @Autowired
    private RedisService redisService;

    @Scheduled(cron = "0 0 0 ? * *")
    public void loadHyperLogLog() {
        redisService.insertHyperLogLog();
        log.info("***********定时器执行*************");
    }
}
