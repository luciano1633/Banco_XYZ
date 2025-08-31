package com.duoc.batch.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;
import com.duoc.batch.model.Transaccion;
import java.util.List;

@Component
public class TransaccionSkipListener implements SkipListener<Transaccion, Transaccion> {
    private static final Logger logger = LoggerFactory.getLogger(TransaccionSkipListener.class);
    private final FlatFileItemWriter<Transaccion> errorItemWriter;

    public TransaccionSkipListener(FlatFileItemWriter<Transaccion> errorItemWriter) {
        this.errorItemWriter = errorItemWriter;
    }

    @Override
    public void onSkipInProcess(Transaccion item, Throwable t) {
        logger.warn("Registro omitido en procesamiento: {} - Error: {}", item, t.getMessage());
        try {
            errorItemWriter.write(new Chunk<>(List.of(item)));
        } catch (Exception e) {
            logger.error("Error al escribir registro omitido en archivo de errores: ", e);
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        logger.warn("Error al leer registro: {}", t.getMessage());
        if (t instanceof FlatFileParseException) {
            logger.warn("Línea omitida por error de parseo: {}", ((FlatFileParseException) t).getInput());
        }
    }

    @Override
    public void onSkipInWrite(Transaccion item, Throwable t) {
        logger.error("Error al escribir registro: {} - Error: {}", item, t.getMessage());
    }
}
