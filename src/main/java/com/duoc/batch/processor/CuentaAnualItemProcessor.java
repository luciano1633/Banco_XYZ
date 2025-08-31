package com.duoc.batch.processor;

import com.duoc.batch.model.CuentaAnual;
import com.duoc.batch.model.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class CuentaAnualItemProcessor implements ItemProcessor<CuentaAnual, CuentaAnual> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CuentaAnualItemProcessor.class);

    @Override
    public CuentaAnual process(@NonNull final CuentaAnual cuenta) throws Exception {
    log.info("Procesando cuenta anual: {}", cuenta);
    System.out.println("[DEBUG][PROCESSOR][CUENTA_ANUAL] Procesando: " + cuenta);
        // Validar fecha
        if (cuenta.getFecha() == null) {
            log.warn("Fecha inválida o faltante para cuenta anual: {}", cuenta);
            throw new InvalidDataException("Fecha inválida o faltante para cuenta anual: " + cuenta.getCuentaId());
        }

        // Validar monto
        if (cuenta.getMonto() == null || cuenta.getMonto() <= 0) {
            log.warn("Monto inválido o faltante para cuenta anual: {}", cuenta);
            throw new InvalidDataException("Monto inválido o faltante para cuenta anual: " + cuenta.getCuentaId());
        }

        // Validar transacción
        String trans = cuenta.getTransaccion();
        if (trans == null ||
            !(trans.equalsIgnoreCase("deposito") || trans.equalsIgnoreCase("retiro") || trans.equalsIgnoreCase("compra"))) {
            log.warn("Transacción inválida para cuenta anual: {}", cuenta);
            throw new InvalidDataException("Transacción inválida para cuenta anual: " + cuenta.getCuentaId());
        }

        // Validar descripción (debe tener valor, si no, se deriva a errores)
        String desc = cuenta.getDescripcion();
        if (desc == null || desc.isBlank()) {
            log.warn("Descripción inválida o faltante para cuenta anual: {}", cuenta);
            throw new InvalidDataException("Descripción inválida o faltante para cuenta anual: " + cuenta.getCuentaId());
        }

        log.info("Cuenta anual válida: {}", cuenta);
        return cuenta;
    }
}
