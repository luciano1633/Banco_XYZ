package com.duoc.batch.advanced;

import com.duoc.batch.model.CuentaInteres;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.lang.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;

public class InteresSkipListener implements SkipListener<CuentaInteres, CuentaInteres> {
    private static final Logger logger = LoggerFactory.getLogger(InteresSkipListener.class);
    private final FlatFileItemWriter<CuentaInteres> errorWriter;

    public InteresSkipListener(FlatFileItemWriter<CuentaInteres> errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void onSkipInRead(@NonNull Throwable t) {
        logger.warn("Error al leer registro de intereses: {}", t.getMessage(), t);
    }

    @Override
    public void onSkipInWrite(@NonNull CuentaInteres item, @NonNull Throwable t) {
        try {
            errorWriter.write(new Chunk<>(java.util.Collections.singletonList(item)));
        } catch (Exception e) {
            logger.error("Error al escribir registro omitido en archivo de errores de intereses: {}", item, e);
        }
        logger.warn("Registro omitido en escritura de intereses: {} - Error: {}", item, t.getMessage(), t);
    }

    @Override
    public void onSkipInProcess(@NonNull CuentaInteres item, @NonNull Throwable t) {
        try {
            errorWriter.write(new Chunk<>(java.util.Collections.singletonList(item)));
        } catch (Exception e) {
            logger.error("Error al escribir registro omitido en archivo de errores de intereses: {}", item, e);
        }
        logger.warn("Registro omitido en procesamiento de intereses: {} - Error: {}", item, t.getMessage(), t);
    }
}
