package com.duoc.batch.advanced;

import com.duoc.batch.model.CuentaInteres;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.core.ExitStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class ErrorFileStepExecutionListenerInteres implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ErrorFileStepExecutionListenerInteres.class);
    private final FlatFileItemWriter<CuentaInteres> errorWriter;

    public ErrorFileStepExecutionListenerInteres(FlatFileItemWriter<CuentaInteres> errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        try {
            errorWriter.open(stepExecution.getExecutionContext());
            logger.info("Apertura de archivo de errores de intereses para el step: {}", stepExecution.getStepName());
        } catch (Exception e) {
            logger.error("Error al abrir archivo de errores de intereses", e);
        }
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        try {
            errorWriter.close();
            logger.info("Cierre de archivo de errores de intereses para el step: {}", stepExecution.getStepName());
        } catch (Exception e) {
            logger.error("Error al cerrar archivo de errores de intereses", e);
        }
        return ExitStatus.COMPLETED;
    }
}
