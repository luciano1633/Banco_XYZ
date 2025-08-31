package com.duoc.batch.advanced;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobRunner {
    private static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job importTransaccionJob;

    public void runImportTransaccionJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            logger.info("Ejecutando importTransaccionJob con parámetros: {}", jobParameters);
            jobLauncher.run(importTransaccionJob, jobParameters);
            logger.info("importTransaccionJob finalizado correctamente.");
        } catch (Exception e) {
            logger.error("Error al ejecutar importTransaccionJob", e);
        }
    }
}
