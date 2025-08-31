package com.duoc.batch.config;
import com.duoc.batch.advanced.CustomSkipPolicyInteres;
import com.duoc.batch.advanced.InteresSkipListener;
import com.duoc.batch.advanced.ErrorFileStepExecutionListenerInteres;

import com.duoc.batch.advanced.JobCountListener;
import com.duoc.batch.advanced.CustomSkipPolicy;
import com.duoc.batch.advanced.TransaccionSkipListener;
import com.duoc.batch.advanced.ErrorFileStepExecutionListener;
import com.duoc.batch.advanced.CustomSkipPolicyCuentaAnual;
import com.duoc.batch.advanced.CuentaAnualSkipListener;
import com.duoc.batch.advanced.ErrorFileStepExecutionListenerCuentaAnual;
import com.duoc.batch.model.CuentaInteres;
import com.duoc.batch.model.CuentaAnual;
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
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {
    // Beans para manejo de errores y skips en cuentas anuales
    @Bean
    @StepScope
    public FlatFileItemWriter<com.duoc.batch.model.CuentaAnual> cuentaAnualErrorWriter(@Value("#{stepExecutionContext['partitionId']}") Integer partitionId) {
    org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<com.duoc.batch.model.CuentaAnual> fieldExtractor = new org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"cuentaId", "fecha", "transaccion", "monto", "descripcion"});
    org.springframework.batch.item.file.transform.DelimitedLineAggregator<com.duoc.batch.model.CuentaAnual> lineAggregator = new org.springframework.batch.item.file.transform.DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(";");
    lineAggregator.setFieldExtractor(fieldExtractor);

    String dirPath = "output";
    java.io.File dir = new java.io.File(dirPath);
    if (!dir.exists()) dir.mkdirs();
    String filePath = dirPath + "/cuentas_anuales_errores_" + (partitionId != null ? partitionId : 0) + ".csv";

    return new FlatFileItemWriterBuilder<com.duoc.batch.model.CuentaAnual>()
        .name("cuentaAnualErrorWriter")
        .resource(new FileSystemResource(filePath))
        .lineAggregator(lineAggregator)
        .headerCallback(writer -> writer.write("cuentaId;fecha;transaccion;monto;descripcion"))
        .build();
    }

    @Bean
    public CustomSkipPolicyCuentaAnual customSkipPolicyCuentaAnual() {
        return new CustomSkipPolicyCuentaAnual();
    }

    @Bean
    @StepScope
    public CuentaAnualSkipListener cuentaAnualSkipListener(FlatFileItemWriter<com.duoc.batch.model.CuentaAnual> cuentaAnualErrorWriter) {
        return new CuentaAnualSkipListener(cuentaAnualErrorWriter);
    }

    @Bean
    @StepScope
    public ErrorFileStepExecutionListenerCuentaAnual errorFileStepExecutionListenerCuentaAnual(FlatFileItemWriter<com.duoc.batch.model.CuentaAnual> cuentaAnualErrorWriter) {
        return new ErrorFileStepExecutionListenerCuentaAnual(cuentaAnualErrorWriter);
    }
    // Beans para manejo de errores y skips en intereses
    @Bean
    @StepScope
    public FlatFileItemWriter<com.duoc.batch.model.CuentaInteres> interesErrorWriter(@Value("#{stepExecutionContext['partitionId']}") Integer partitionId) {
    org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<com.duoc.batch.model.CuentaInteres> fieldExtractor = new org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"cuentaId", "nombre", "saldo", "edad", "tipo"});
    org.springframework.batch.item.file.transform.DelimitedLineAggregator<com.duoc.batch.model.CuentaInteres> lineAggregator = new org.springframework.batch.item.file.transform.DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(";");
    lineAggregator.setFieldExtractor(fieldExtractor);

    String dirPath = "output";
    java.io.File dir = new java.io.File(dirPath);
    if (!dir.exists()) dir.mkdirs();
    String filePath = dirPath + "/intereses_errores_" + partitionId + ".csv";

    return new FlatFileItemWriterBuilder<com.duoc.batch.model.CuentaInteres>()
        .name("interesErrorWriter")
        .resource(new FileSystemResource(filePath))
        .lineAggregator(lineAggregator)
        .headerCallback(writer -> writer.write("cuentaId;nombre;saldo;edad;tipo"))
        .build();
    }

    @Bean
    public CustomSkipPolicyInteres customSkipPolicyInteres() {
        return new CustomSkipPolicyInteres();
    }

    @Bean
    @StepScope
    public InteresSkipListener interesSkipListener(FlatFileItemWriter<com.duoc.batch.model.CuentaInteres> interesErrorWriter) {
        return new InteresSkipListener(interesErrorWriter);
    }

    @Bean
    @StepScope
    public ErrorFileStepExecutionListenerInteres errorFileStepExecutionListenerInteres(FlatFileItemWriter<com.duoc.batch.model.CuentaInteres> interesErrorWriter) {
        return new ErrorFileStepExecutionListenerInteres(interesErrorWriter);
    }

    // Beans para manejo de errores y skips en transacciones
    @Bean(name = "transaccionErrorWriter")
    @StepScope
    public FlatFileItemWriter<Transaccion> transaccionErrorWriter(@Value("#{stepExecutionContext['partitionId']}") Integer partitionId) {
        System.out.println("[DEBUG] partitionId recibido en transaccionErrorWriter: " + partitionId);
        if (partitionId == null) throw new IllegalStateException("partitionId no puede ser null en ejecución particionada");
        org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<Transaccion> fieldExtractor = new org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "fecha", "monto", "tipo"});
        org.springframework.batch.item.file.transform.DelimitedLineAggregator<Transaccion> lineAggregator = new org.springframework.batch.item.file.transform.DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        lineAggregator.setFieldExtractor(fieldExtractor);

        String dirPath = "output";
        java.io.File dir = new java.io.File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        String fileName = dirPath + "/transacciones_errores_" + partitionId + ".csv";

        return new FlatFileItemWriterBuilder<Transaccion>()
            .name("transaccionErrorWriter")
            .resource(new FileSystemResource(fileName))
            .lineAggregator(lineAggregator)
            .headerCallback(writer -> writer.write("id;fecha;monto;tipo"))
            .build();
    }

    @Bean
    public CustomSkipPolicy customSkipPolicy() {
        return new CustomSkipPolicy();
    }

    @Bean
    @StepScope
    public TransaccionSkipListener transaccionSkipListener(@Qualifier("transaccionErrorWriter") FlatFileItemWriter<Transaccion> transaccionErrorWriter) {
        return new TransaccionSkipListener(transaccionErrorWriter);
    }

    @Bean
    @StepScope
    public ErrorFileStepExecutionListener errorFileStepExecutionListener(@Qualifier("transaccionErrorWriter") FlatFileItemWriter<Transaccion> transaccionErrorWriter) {
        return new ErrorFileStepExecutionListener(transaccionErrorWriter);
    }

    // Configuración de paralelismo y particiones (configurable)
    @Bean
    public Integer partitionGridSize() {
        return 6; // Ajustado para aprovechar 6 hilos reales
    }

    @Bean
    public Integer chunkSize() {
        return 2; // Tamaño del chunk (ajustable)
    }

    // Configura el TaskExecutor para procesamiento paralelo
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6); // 50% de hilos disponibles
        executor.setMaxPoolSize(10); // Hasta el máximo de núcleos
        executor.setQueueCapacity(100); // Ajustable según carga
        executor.setThreadNamePrefix("Batch-Thread-");
        executor.initialize();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
 
    @Bean
    @StepScope
    public FlatFileItemReader<Transaccion> reader(
            @Value("#{stepExecutionContext['startLine']}") Integer startLine,
            @Value("#{stepExecutionContext['endLine']}") Integer endLine) {
        System.out.println("[DEBUG][READER] startLine=" + startLine + ", endLine=" + endLine);
        FlatFileItemReader<Transaccion> reader = new FlatFileItemReader<>() {
            private int currentLine = 0;
            @Override
            public Transaccion read() throws Exception {
                Transaccion item;
                while ((item = super.read()) != null) {
                    currentLine++;
                    if (currentLine - 1 < startLine) continue;
                    if (currentLine - 1 > endLine) return null;
                    System.out.println("[DEBUG][READER] partition " + startLine + "-" + endLine + " leyendo línea " + currentLine);
                    return item;
                }
                return null;
            }
        };
        reader.setResource(new ClassPathResource("data/transacciones.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split(";");
            Transaccion t = new Transaccion();
            try { t.setId(fields.length > 0 ? Integer.parseInt(fields[0]) : 0); } catch (Exception e) { t.setId(0); }
            try { t.setFecha(fields.length > 1 ? LocalDate.parse(fields[1]) : null); } catch (Exception e) { t.setFecha(null); }
            try { t.setMonto(fields.length > 2 && fields[2] != null && !fields[2].isBlank() ? Double.parseDouble(fields[2]) : null); } catch (Exception e) { t.setMonto(null); }
            t.setTipo(fields.length > 3 ? fields[3] : null);
            return t;
        });
        return reader;
    }

    @Bean
    public TransaccionItemProcessor processor() {
        return new TransaccionItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> writer(DataSource dataSource) {
        JdbcBatchItemWriter<Transaccion> writer = new JdbcBatchItemWriterBuilder<Transaccion>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO transacciones (id, fecha, monto, tipo) VALUES (:id, :fecha, :monto, :tipo)")
                .dataSource(dataSource)
                .build();
        return writer;
    }

    @Bean(name = "loggingTransaccionWriter")
    public org.springframework.batch.item.ItemWriter<Transaccion> loggingTransaccionWriter(JdbcBatchItemWriter<Transaccion> writer) {
        return items -> {
            System.out.println("[DEBUG][WRITER] Escribiendo chunk de tamaño: " + items.size());
            for (Transaccion t : items) {
                System.out.println("[DEBUG][WRITER] Transaccion id=" + t.getId());
            }
            writer.write(items);
        };
    }
    

    @Bean
    public Partitioner transaccionPartitioner() {
        return new RangePartitioner(new ClassPathResource("data/transacciones.csv"), 1000, partitionGridSize(), 1);
    }

    @Bean
    public Step transaccionWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("reader") FlatFileItemReader<Transaccion> reader,
            TransaccionItemProcessor processor,
            JdbcBatchItemWriter<Transaccion> writer,
            CustomSkipPolicy customSkipPolicy,
            TransaccionSkipListener transaccionSkipListener,
            ErrorFileStepExecutionListener errorFileStepExecutionListener) {
        return new StepBuilder("transaccionWorkerStep", jobRepository)
                .<Transaccion, Transaccion>chunk(chunkSize(), transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)
                .listener(transaccionSkipListener)
                .listener(errorFileStepExecutionListener)
                .build();
    }

    @Bean
    public Step transaccionPartitionerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                           Partitioner transaccionPartitioner, Step transaccionWorkerStep) {
    return new StepBuilder("transaccionPartitionerStep", jobRepository)
        .partitioner("transaccionWorkerStep", transaccionPartitioner)
        .step(transaccionWorkerStep)
        .gridSize(partitionGridSize())
        .taskExecutor(taskExecutor())
        .build();
    }

    @Bean
    public Job importTransaccionJob(JobRepository jobRepository, Step transaccionPartitionerStep, JobCountListener jobCountListener) {
        return new JobBuilder("importTransaccionJob", jobRepository)
                .listener(jobCountListener)
                .flow(transaccionPartitionerStep)
                .end()
                .build();
    }

    // Beans for the second job: "Cálculo de Intereses Mensuales"

    @Bean("interesReader")
    @StepScope
    public FlatFileItemReader<CuentaInteres> interesReader(
            @Value("#{stepExecutionContext['startLine']}") Integer startLine,
            @Value("#{stepExecutionContext['endLine']}") Integer endLine) {
        FlatFileItemReader<CuentaInteres> reader = new FlatFileItemReader<>() {
            private int currentLine = 0;
            @Override
            public CuentaInteres read() throws Exception {
                CuentaInteres item;
                while ((item = super.read()) != null) {
                    currentLine++;
                    if (currentLine - 1 < startLine) continue;
                    if (currentLine - 1 > endLine) return null;
                    return item;
                }
                return null;
            }
        };
        reader.setResource(new ClassPathResource("data/intereses.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split(";");
            CuentaInteres c = new CuentaInteres();
            try { c.setCuentaId(Integer.parseInt(fields[0])); } catch (Exception e) { c.setCuentaId(0); }
            c.setNombre(fields.length > 1 ? fields[1] : null);
            try { c.setSaldo(fields.length > 2 && fields[2] != null && !fields[2].isBlank() ? Double.parseDouble(fields[2]) : null); } catch (Exception e) { c.setSaldo(null); }
            try { c.setEdad(fields.length > 3 && fields[3] != null && !fields[3].isBlank() ? Integer.parseInt(fields[3]) : null); } catch (Exception e) { c.setEdad(null); }
            c.setTipo(fields.length > 4 ? fields[4] : null);
            return c;
        });
        return reader;
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
        public Partitioner interesPartitioner() {
            // Suponiendo 1000 líneas de datos, ajusta según tu archivo
            return new RangePartitioner(new ClassPathResource("data/intereses.csv"), 1000, partitionGridSize(), 1);
        }

        @Bean
        public Step interesWorkerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      FlatFileItemReader<CuentaInteres> interesReader,
                      CuentaInteresItemProcessor interesProcessor,
                      JdbcBatchItemWriter<CuentaInteres> interesWriter,
                      CustomSkipPolicyInteres customSkipPolicyInteres,
                      InteresSkipListener interesSkipListener,
                      ErrorFileStepExecutionListenerInteres errorFileStepExecutionListenerInteres,
                      FlatFileItemWriter<CuentaInteres> interesErrorWriter) {
    return new StepBuilder("interesWorkerStep", jobRepository)
        .<CuentaInteres, CuentaInteres>chunk(chunkSize(), transactionManager)
        .reader(interesReader)
        .processor(interesProcessor)
        .writer(interesWriter)
        .faultTolerant()
        .skipPolicy(customSkipPolicyInteres)
        .listener(interesSkipListener)
        .listener(errorFileStepExecutionListenerInteres)
        .build();
    }

        @Bean
        public Step interesPartitionerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                           Partitioner interesPartitioner, Step interesWorkerStep) {
    return new StepBuilder("interesPartitionerStep", jobRepository)
        .partitioner("interesWorkerStep", interesPartitioner)
        .step(interesWorkerStep)
        .gridSize(partitionGridSize())
        .taskExecutor(taskExecutor())
        .build();
        }

        @Bean
        public Job calculoInteresJob(JobRepository jobRepository, Step interesPartitionerStep, JobCountListener jobCountListener) {
            return new JobBuilder("calculoInteresJob", jobRepository)
                    .listener(jobCountListener)
                    .start(interesPartitionerStep)
                    .build();
        }


    @Bean
    public FlatFileItemReader<CuentaAnual> cuentaAnualReader() {
        FlatFileItemReader<CuentaAnual> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/cuentas_anuales.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split(";");
            CuentaAnual c = new CuentaAnual();
            try { c.setCuentaId(Integer.parseInt(fields[0])); } catch (Exception e) { c.setCuentaId(0); }
            try {
                String fechaStr = fields.length > 1 ? fields[1] : null;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                c.setFecha(fechaStr == null || fechaStr.isBlank() ? null : LocalDate.parse(fechaStr, formatter));
            } catch (Exception e) { c.setFecha(null); }
            try { c.setMonto(fields.length > 3 && fields[3] != null && !fields[3].isBlank() ? Double.parseDouble(fields[3]) : null); } catch (Exception e) { c.setMonto(null); }
            c.setTransaccion(fields.length > 2 ? fields[2] : null);
            c.setDescripcion(fields.length > 4 ? fields[4] : null);
            return c;
        });
        return reader;
    }

    @Bean
    public CuentaAnualItemProcessor cuentaAnualProcessor() {
        return new CuentaAnualItemProcessor();
    }

    @Bean
    public FlatFileItemWriter<CuentaAnual> cuentaAnualWriter() {
    org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<CuentaAnual> fieldExtractor = new org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"cuentaId", "fecha", "transaccion", "monto", "descripcion"});

    org.springframework.batch.item.file.transform.DelimitedLineAggregator<CuentaAnual> lineAggregator = new org.springframework.batch.item.file.transform.DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(";");
    lineAggregator.setFieldExtractor(fieldExtractor);

    String dirPath = "output";
    java.io.File dir = new java.io.File(dirPath);
    if (!dir.exists()) dir.mkdirs();
    String filePath = dirPath + "/estado_cuenta_anual.csv";

    return new FlatFileItemWriterBuilder<CuentaAnual>()
        .name("cuentaAnualItemWriter")
        .resource(new FileSystemResource(filePath))
        .lineAggregator(lineAggregator)
        .headerCallback(writer -> writer.write("cuentaId;fecha;transaccion;monto;descripcion"))
        .append(true)
        .build();
    }

    @Bean
    public Partitioner cuentaAnualPartitioner() {
    // Ajustado a 2000 líneas para cubrir archivos grandes y evitar que queden particiones sin datos
    return new RangePartitioner(new ClassPathResource("data/cuentas_anuales.csv"), 2000, partitionGridSize(), 1);
    }

    @Bean
    public Step cuentaAnualWorkerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      CuentaAnualItemProcessor cuentaAnualProcessor,
                      FlatFileItemWriter<CuentaAnual> cuentaAnualWriter,
                      CustomSkipPolicyCuentaAnual customSkipPolicyCuentaAnual,
                      CuentaAnualSkipListener cuentaAnualSkipListener,
                      ErrorFileStepExecutionListenerCuentaAnual errorFileStepExecutionListenerCuentaAnual,
                      FlatFileItemWriter<CuentaAnual> cuentaAnualErrorWriter,
                      FlatFileItemReader<CuentaAnual> cuentaAnualReader) {
    return new StepBuilder("cuentaAnualWorkerStep", jobRepository)
        .<CuentaAnual, CuentaAnual>chunk(10, transactionManager)
        .reader(cuentaAnualReader)
        .processor(cuentaAnualProcessor)
        .writer(cuentaAnualWriter)
        .faultTolerant()
        .skipPolicy(customSkipPolicyCuentaAnual)
        .listener(cuentaAnualSkipListener)
        .listener(errorFileStepExecutionListenerCuentaAnual)
        .build();
    }

    @Bean
    public Step cuentaAnualPartitionerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                           Partitioner cuentaAnualPartitioner, Step cuentaAnualWorkerStep) {
    return new StepBuilder("cuentaAnualPartitionerStep", jobRepository)
        .partitioner("cuentaAnualWorkerStep", cuentaAnualPartitioner)
        .step(cuentaAnualWorkerStep)
        .gridSize(partitionGridSize())
        .taskExecutor(taskExecutor())
        .build();
    }

    @Bean
    public Job generacionEstadoCuentaJob(JobRepository jobRepository, Step cuentaAnualWorkerStep, JobCountListener jobCountListener) {
        return new JobBuilder("generacionEstadoCuentaJob", jobRepository)
                .listener(jobCountListener)
                .start(cuentaAnualWorkerStep)
                .build();
    }
}
