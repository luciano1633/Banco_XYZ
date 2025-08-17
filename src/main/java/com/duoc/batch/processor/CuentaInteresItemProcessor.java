package com.duoc.batch.processor;

import com.duoc.batch.model.CuentaInteres;
import org.springframework.batch.item.ItemProcessor;

public class CuentaInteresItemProcessor implements ItemProcessor<CuentaInteres, CuentaInteres> {

    @Override
    public CuentaInteres process(final CuentaInteres cuenta) throws Exception {
        // Validar saldo
        if (cuenta.getSaldo() == null || cuenta.getSaldo() < 0) {
            System.out.println("Descartada: Saldo inválido o faltante para cuenta_id: " + cuenta.getCuentaId());
            return null;
        }
        // Validar tipo
        String tipo = cuenta.getTipo();
        if (tipo == null || (!tipo.equals("ahorro") && !tipo.equals("prestamo"))) {
            System.out.println("Descartada: Tipo inválido para cuenta_id: " + cuenta.getCuentaId());
            return null;
        }
        // Validar nombre
        String nombre = cuenta.getNombre();
        if (nombre == null || nombre.isBlank() || nombre.equalsIgnoreCase("unknown") || nombre.equals("-1")) {
            System.out.println("Descartada: Nombre inválido para cuenta_id: " + cuenta.getCuentaId());
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
