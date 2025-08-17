package com.duoc.batch.config;

import com.duoc.batch.model.CuentaAnual;
import com.duoc.batch.model.CuentaInteres;
import com.duoc.batch.model.Transaccion;
import com.duoc.batch.processor.CuentaAnualItemProcessor;
import com.duoc.batch.processor.CuentaInteresItemProcessor;
import com.duoc.batch.processor.TransaccionItemProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    @Bean
public FlatFileItemReader<Transaccion> reader() {
    return new FlatFileItemReaderBuilder<Transaccion>()
            .name("transaccionItemReader")
            .resource(new ClassPathResource("data/transacciones.csv"))
            .delimited()
            .delimiter(";")
            .names("id", "fecha", "monto", "tipo")
            .linesToSkip(1)
            .fieldSetMapper(new FieldSetMapper<Transaccion>() {
                @Override
                public Transaccion mapFieldSet(FieldSet fieldSet) throws BindException {
                    Transaccion t = new Transaccion();
                    t.setId(fieldSet.readInt("id"));
                    // Manejo seguro de fecha
                    try {
                        t.setFecha(LocalDate.parse(fieldSet.readString("fecha")));
                    } catch (Exception e) {
                        t.setFecha(null);
                    }
                    // Manejo seguro de monto
                    String montoStr = fieldSet.readString("monto");
                    try {
                        t.setMonto(montoStr == null || montoStr.isBlank() ? null : Double.parseDouble(montoStr));
                    } catch (Exception e) {
                        t.setMonto(null);
                    }
                    t.setTipo(fieldSet.readString("tipo"));
                    return t;
                }
            })
            .build();
}

    @Bean
    public TransaccionItemProcessor processor() {
        return new TransaccionItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO transacciones (id, fecha, monto, tipo) VALUES (:id, :fecha, :monto, :tipo)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importTransaccionJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importTransaccionJob", jobRepository)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
public Step step1(JobRepository jobRepository, 
                  PlatformTransactionManager transactionManager,
                  FlatFileItemReader<Transaccion> reader,
                  TransaccionItemProcessor processor,
                  JdbcBatchItemWriter<Transaccion> writer) {
    return new StepBuilder("step1", jobRepository)
            .<Transaccion, Transaccion>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(FlatFileParseException.class)
            .skip(NumberFormatException.class)
            .skip(DuplicateKeyException.class) // <-- Agrega esta línea
            .skipLimit(1000)
            .build();
}

    // Beans for the second job: "Cálculo de Intereses Mensuales"

    @Bean
    public FlatFileItemReader<CuentaInteres> interesReader() {
        return new FlatFileItemReaderBuilder<CuentaInteres>()
                .name("cuentaInteresItemReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .delimited()
                .delimiter(";")
                .names(new String[]{"cuentaId", "nombre", "saldo", "edad", "tipo"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<CuentaInteres>() {{
                    setTargetType(CuentaInteres.class);
                }})
                .build();
    }

    @Bean
    public CuentaInteresItemProcessor interesProcessor() {
        return new CuentaInteresItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<CuentaInteres> interesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaInteres>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("MERGE INTO cuentas c USING (SELECT :cuentaId AS cuenta_id, :nombre AS nombre, :saldo AS saldo, :edad AS edad, :tipo AS tipo FROM dual) src " +
                "ON (c.cuenta_id = src.cuenta_id) " +
                "WHEN MATCHED THEN UPDATE SET c.saldo = src.saldo " +
                "WHEN NOT MATCHED THEN INSERT (cuenta_id, nombre, saldo, edad, tipo) VALUES (src.cuenta_id, src.nombre, src.saldo, src.edad, src.tipo)")
                .dataSource(dataSource)
                .build();
    }

        @Bean
    public Job calculoInteresJob(JobRepository jobRepository, Step stepInteres) {
        return new JobBuilder("calculoInteresJob", jobRepository)
                .start(stepInteres)
                .build();
    }

    @Bean
public Step stepInteres(JobRepository jobRepository, 
                        PlatformTransactionManager transactionManager,
                        FlatFileItemReader<CuentaInteres> interesReader,
                        CuentaInteresItemProcessor interesProcessor,
                        JdbcBatchItemWriter<CuentaInteres> interesWriter) {
    return new StepBuilder("stepInteres", jobRepository)
            .<CuentaInteres, CuentaInteres>chunk(100, transactionManager)
            .reader(interesReader)
            .processor(interesProcessor)
            .writer(interesWriter)
            .faultTolerant()
            .skip(Exception.class)
            .skipLimit(1000)
            .build();
}


@Bean
public FlatFileItemReader<CuentaAnual> cuentaAnualReader() {
    return new FlatFileItemReaderBuilder<CuentaAnual>()
            .name("cuentaAnualItemReader")
            .resource(new ClassPathResource("data/cuentas_anuales.csv"))
            .delimited()
            .delimiter(";")
            .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
            .fieldSetMapper(fieldSet -> {
                CuentaAnual c = new CuentaAnual();
                c.setCuentaId(fieldSet.readInt("cuentaId"));
                // Parseo seguro de fecha dd-MM-yyyy
                try {
                    String fechaStr = fieldSet.readString("fecha");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    c.setFecha(fechaStr == null || fechaStr.isBlank() ? null : LocalDate.parse(fechaStr, formatter));
                } catch (Exception e) {
                    c.setFecha(null);
                }
                // Parseo seguro de monto
                String montoStr = fieldSet.readString("monto");
                try {
                    c.setMonto(montoStr == null || montoStr.isBlank() ? null : Double.parseDouble(montoStr));
                } catch (Exception e) {
                    c.setMonto(null);
                }
                c.setTransaccion(fieldSet.readString("transaccion"));
                c.setDescripcion(fieldSet.readString("descripcion"));
                return c;
            })
            .linesToSkip(1)
            .build();
}

    @Bean
    public CuentaAnualItemProcessor cuentaAnualProcessor() {
        return new CuentaAnualItemProcessor();
    }

    @Bean
    public FlatFileItemWriter<CuentaAnual> cuentaAnualWriter() {
        return new FlatFileItemWriterBuilder<CuentaAnual>()
                .name("cuentaAnualItemWriter")
                .resource(new FileSystemResource("target/estado_cuenta_anual.txt"))
                .lineAggregator(item -> {
                    // Custom line aggregator to format the output
                    return String.format("Cuenta: %d, Fecha: %s, Transaccion: %s, Monto: %.2f, Descripcion: %s",
                            item.getCuentaId(), item.getFecha(), item.getTransaccion(), item.getMonto(), item.getDescripcion());
                })
                .build();
    }

    @Bean
    public Job generacionEstadoCuentaJob(JobRepository jobRepository, Step step3) {
        return new JobBuilder("generacionEstadoCuentaJob", jobRepository)
                .flow(step3)
                .end()
                .build();
    }

    @Bean
    public Step step3(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<CuentaAnual> cuentaAnualReader,
                      FlatFileItemWriter<CuentaAnual> cuentaAnualWriter) {
        return new StepBuilder("step3", jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(10, transactionManager)
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor())
                .writer(cuentaAnualWriter)
                .build();
    }
}
