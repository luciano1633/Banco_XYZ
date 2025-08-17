package com.duoc.batch.processor;

import com.duoc.batch.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;

public class CuentaAnualItemProcessor implements ItemProcessor<CuentaAnual, CuentaAnual> {

    @Override
    public CuentaAnual process(final CuentaAnual cuenta) throws Exception {
        if (cuenta.getFecha() == null || cuenta.getMonto() == null || cuenta.getTransaccion() == null) {
            System.out.println("Skipping annual account record due to missing data: " + cuenta.getCuentaId());
            return null;
        }
        return cuenta;
    }
}
