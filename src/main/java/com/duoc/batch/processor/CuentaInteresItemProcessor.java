package com.duoc.batch.processor;

import com.duoc.batch.model.CuentaInteres;
import com.duoc.batch.model.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;

public class CuentaInteresItemProcessor implements ItemProcessor<CuentaInteres, CuentaInteres> {

    @Override
    public CuentaInteres process(final CuentaInteres cuenta) throws Exception {
        // Validar saldo
        if (cuenta.getSaldo() == null || cuenta.getSaldo() < 0) {
            throw new InvalidDataException("Saldo inválido o faltante para cuenta_id: " + cuenta.getCuentaId());
        }
        // Validar tipo
        String tipo = cuenta.getTipo();
        if (tipo == null || (!tipo.equals("ahorro") && !tipo.equals("prestamo"))) {
            throw new InvalidDataException("Tipo inválido para cuenta_id: " + cuenta.getCuentaId());
        }
        // Validar nombre
        String nombre = cuenta.getNombre();
        if (nombre == null || nombre.isBlank() || nombre.equalsIgnoreCase("unknown") || nombre.equals("-1")) {
            throw new InvalidDataException("Nombre inválido para cuenta_id: " + cuenta.getCuentaId());
        }

        // Validar edad
        Integer edad = cuenta.getEdad();
        if (edad == null || edad < 18 || edad > 150) {
            return null;
        }

        // Calcular interés
        double interes = 0.0;
        if (tipo.equals("ahorro")) {
            interes = cuenta.getSaldo() * 0.05;
        } else if (tipo.equals("prestamo")) {
            interes = cuenta.getSaldo() * 0.10;
        }
        cuenta.setSaldo(cuenta.getSaldo() + interes);
        return cuenta;
    }
}
