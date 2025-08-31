package com.duoc.batch.advanced;

import com.duoc.batch.model.CuentaAnual;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.batch.item.Chunk;

public class CuentaAnualSkipListener implements SkipListener<CuentaAnual, CuentaAnual> {
    private final FlatFileItemWriter<CuentaAnual> errorWriter;

    public CuentaAnualSkipListener(FlatFileItemWriter<CuentaAnual> errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void onSkipInRead(@NonNull Throwable t) {}

    @Override
    public void onSkipInWrite(@NonNull CuentaAnual item, @NonNull Throwable t) {
        try {
            errorWriter.write(new Chunk<>(java.util.Collections.singletonList(item)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSkipInProcess(@NonNull CuentaAnual item, @NonNull Throwable t) {
        try {
            errorWriter.write(new Chunk<>(java.util.Collections.singletonList(item)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
