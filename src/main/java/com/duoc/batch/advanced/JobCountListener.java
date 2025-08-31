package com.duoc.batch.advanced;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class JobCountListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        // No-op
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        int totalRead = 0;
        int totalWrite = 0;
        int totalSkipped = 0;
        int totalFiltered = 0;
        System.out.println("\nDetalle por Step:");
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            System.out.println("Step: " + stepExecution.getStepName()
                + " | Read: " + stepExecution.getReadCount()
                + " | Write: " + stepExecution.getWriteCount()
                + " | Skipped: " + stepExecution.getSkipCount()
                + " | Filtered: " + stepExecution.getFilterCount());
            totalRead += stepExecution.getReadCount();
            totalWrite += stepExecution.getWriteCount();
            totalSkipped += stepExecution.getSkipCount();
            totalFiltered += stepExecution.getFilterCount();
        }
        System.out.println("\nResumen del Job: ");
        System.out.println("Total leídos: " + totalRead);
        System.out.println("Total escritos (válidos): " + totalWrite);
        System.out.println("Total descartados por excepción (skipped): " + totalSkipped);
        System.out.println("Total filtrados por processor (filtered): " + totalFiltered);
        System.out.println("Total inválidos (skipped + filtered): " + (totalSkipped + totalFiltered));
    }
}
