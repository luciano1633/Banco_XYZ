package com.duoc.batch.advanced;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.lang.NonNull;

public class CustomSkipPolicyInteres implements SkipPolicy {
    @Override
    public boolean shouldSkip(@NonNull Throwable t, long skipCount) {
        // Puedes personalizar aquí los tipos de errores a saltar
        return true; // Permite saltar cualquier excepción
    }
}
