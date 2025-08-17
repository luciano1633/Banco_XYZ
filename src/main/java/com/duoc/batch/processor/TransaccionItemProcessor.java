package com.duoc.batch.processor;

import com.duoc.batch.model.Transaccion;
import org.springframework.batch.item.ItemProcessor;

public class TransaccionItemProcessor implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(final Transaccion transaccion) throws Exception {
        // Validación de fecha
        if (transaccion.getFecha() == null) {
            System.out.println("Descartada: Fecha inválida o faltante para id: " + transaccion.getId());
            return null;
        }

        // Validación de monto
        if (transaccion.getMonto() == null) {
            System.out.println("Descartada: Monto inválido o faltante para id: " + transaccion.getId());
            return null;
        }

        // Validación de tipo
        String tipo = transaccion.getTipo();
        if (tipo == null || (!tipo.equals("credito") && !tipo.equals("debito"))) {
            System.out.println("Descartada: Tipo inválido para id: " + transaccion.getId());
            return null;
        }

        // Si pasa todas las validaciones, se procesa normalmente
        return transaccion;
    }
}
