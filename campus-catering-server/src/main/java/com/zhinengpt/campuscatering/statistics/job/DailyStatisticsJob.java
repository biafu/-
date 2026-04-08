package com.zhinengpt.campuscatering.statistics.job;

import com.zhinengpt.campuscatering.statistics.service.DailyStatisticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyStatisticsJob {

    private final DailyStatisticsService dailyStatisticsService;

    public DailyStatisticsJob(DailyStatisticsService dailyStatisticsService) {
        this.dailyStatisticsService = dailyStatisticsService;
    }

    @Scheduled(cron = "${app.statistics.daily-job-cron:0 10 0 * * *}")
    public void dailyStatisticsJob() {
        dailyStatisticsService.summarizeYesterday();
    }
}
