package com.duoc.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Map<String, Job> jobs;

    public static void main(String[] args) {
    // Forzamos el cierre de la JVM al finalizar el job porque, en este proyecto, algunos threads de pools, JDBC y recursos internos
    // (por ejemplo: Batch-Thread-1, HikariPool-1:housekeeper, OJDBC-WORKER-THREAD-1) pueden quedar abiertos tras finalizar el job,
    // impidiendo que la JVM termine sola. El cierre forzado garantiza que el proceso finalice correctamente y puedas ejecutar otros jobs en secuencia.
        var ctx = SpringApplication.run(BatchApplication.class, args);
        int exitCode = SpringApplication.exit(ctx);
        System.exit(exitCode);
    }

   @Override
public void run(String... args) throws Exception {
    boolean jobLaunched = false;
    for (String arg : args) {
        if (arg.startsWith("spring.batch.job.name=")) {
            String jobName = arg.substring("spring.batch.job.name=".length());
            Job job = jobs.get(jobName);
            if (job != null) {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();
                jobLauncher.run(job, jobParameters);
                jobLaunched = true;
            } else {
                    System.err.println("Job no encontrado: " + jobName);
            }
        }
    }
    // Si no se pasó ningún argumento, ejecuta el job de transacciones por defecto
    if (!jobLaunched) {
        Job job = jobs.get("importTransaccionJob");
        if (job != null) {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
        } else {
            System.out.println("Job not found: importTransaccionJob");
            }
        }
     
    }   
}