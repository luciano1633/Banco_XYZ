package com.duoc.batch.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;
import com.duoc.batch.model.Transaccion;

@Component
public class ErrorFileStepExecutionListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ErrorFileStepExecutionListener.class);
    private final FlatFileItemWriter<Transaccion> errorItemWriter;

    public ErrorFileStepExecutionListener(FlatFileItemWriter<Transaccion> errorItemWriter) {
        this.errorItemWriter = errorItemWriter;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Abriendo archivo de errores al inicio del step");
        errorItemWriter.open(new ExecutionContext());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("Cerrando archivo de errores al final del step");
        errorItemWriter.close();
        return ExitStatus.COMPLETED;
    }
}
