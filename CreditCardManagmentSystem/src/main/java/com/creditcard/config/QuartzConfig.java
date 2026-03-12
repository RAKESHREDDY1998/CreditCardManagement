package com.creditcard.config;

import com.creditcard.scheduler.StatementGenerationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    /**
     * Defines the Quartz Job for monthly statement generation.
     */
    @Bean
    public JobDetail statementJobDetail() {
        return JobBuilder.newJob(StatementGenerationJob.class)
                .withIdentity("statementGenerationJob", "creditcard-jobs")
                .withDescription("Generates monthly statements for all active credit cards")
                .storeDurably()
                .requestRecovery(true)
                .build();
    }

    /**
     * Cron trigger: Runs at 01:00 AM on the 1st of every month.
     * Cron expression: 0 0 1 1 * ?
     */
    @Bean
    public Trigger statementJobTrigger() {
        CronScheduleBuilder scheduleBuilder =
            CronScheduleBuilder.cronSchedule("0 0 1 1 * ?")
                .withMisfireHandlingInstructionDoNothing();

        return TriggerBuilder.newTrigger()
                .forJob(statementJobDetail())
                .withIdentity("statementJobTrigger", "creditcard-triggers")
                .withDescription("Monthly trigger for statement generation at 1 AM on 1st of each month")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
