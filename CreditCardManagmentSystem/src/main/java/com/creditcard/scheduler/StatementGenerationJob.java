package com.creditcard.scheduler;

import com.creditcard.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class StatementGenerationJob implements Job {

    @Autowired
    private StatementService statementService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  QUARTZ JOB: Monthly Statement Generation Started    ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Fire Time  : {}                     ", context.getFireTime());
        log.info("║  Next Fire  : {}                     ", context.getNextFireTime());
        log.info("╚══════════════════════════════════════════════════════╝");

        try {
            statementService.generateStatementsForAllActiveCards();
            log.info("✅ Quartz Job Completed: Statement generation finished successfully.");
        } catch (Exception e) {
            log.error("❌ Quartz Job Failed: {}", e.getMessage(), e);
            JobExecutionException ex = new JobExecutionException(e);
            ex.setRefireImmediately(false);
            throw ex;
        }
    }
}
