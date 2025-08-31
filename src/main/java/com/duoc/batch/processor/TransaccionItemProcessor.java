package com.duoc.batch.processor;

import com.duoc.batch.model.Transaccion;
import com.duoc.batch.model.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;

public class TransaccionItemProcessor implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(final Transaccion transaccion) throws Exception {
        // Validación de fecha
        if (transaccion.getFecha() == null) {
            throw new InvalidDataException("Fecha inválida o faltante para id: " + transaccion.getId());
        }

        // Validación de monto
        if (transaccion.getMonto() == null || transaccion.getMonto() <= 0) {
            throw new InvalidDataException("Monto inválido (nulo, cero o negativo) para id: " + transaccion.getId());
        }

        // Validación de tipo
        String tipo = transaccion.getTipo();
        if (tipo == null || (!tipo.equals("credito") && !tipo.equals("debito"))) {
            // Filtra el registro, no lo considera error grave
            return null;
        }

        // Si pasa todas las validaciones, se procesa normalmente
        return transaccion;
    }
}
