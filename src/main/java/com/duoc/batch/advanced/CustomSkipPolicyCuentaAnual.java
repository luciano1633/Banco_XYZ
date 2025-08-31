package com.duoc.batch.advanced;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.lang.NonNull;

public class CustomSkipPolicyCuentaAnual implements SkipPolicy {
    @Override
    public boolean shouldSkip(@NonNull Throwable t, long skipCount) {
        // Personaliza aquí los errores a saltar
        return true; // Permite saltar cualquier excepción
    }
}
