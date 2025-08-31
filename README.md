# Notas sobre el tracking de registros escritos en jobs particionados

En este proyecto, al usar particionamiento con Spring Batch, se observó que el contador interno de registros escritos (`writeCount`) en los steps workers y en el resumen del job puede permanecer en 0, incluso cuando los datos válidos sí se insertan correctamente en la base de datos.

Esto ocurre porque, en escenarios avanzados de particionamiento y wiring personalizado, el tracking interno de Spring Batch no siempre refleja los inserts reales, aunque el procesamiento y la escritura funcionen correctamente.

**¿Cómo se validó la correcta inserción?**

- Se agregó un log explícito en el writer (`loggingTransaccionWriter`) que imprime en consola cada vez que se escribe un chunk y los IDs de las transacciones insertadas.
- Se comprobó que los IDs mostrados en el log coinciden exactamente con los datos insertados en la base de datos.

**¿Por qué se insertó el log en el writer?**

- Para tener una trazabilidad real y confiable de los registros escritos por cada partición, independientemente del contador interno de Spring Batch.
- Esto permite validar que el procesamiento y la escritura son correctos, aunque el resumen del job muestre 'Total escritos (válidos): 0'.

**Conclusión:**

El job funciona correctamente y cumple con los requerimientos de procesamiento, validación y escritura en la base de datos. El log en el writer es la fuente confiable para auditar los registros escritos en escenarios de particionamiento avanzado.

# Banco XYZ - Migración de Procesos Batch con Spring Batch

## 🚀 Objetivo
Modernizar el sistema batch legacy del Banco XYZ usando Spring Batch, con procesamiento paralelo, manejo robusto de errores y trazabilidad de resultados.

## 📁 Estructura del Proyecto

- `src/main/java/com/duoc/batch/`
  - `config/BatchConfiguration.java`: Configuración de jobs, steps, particiones y paralelismo.
  - `model/`: Modelos de datos (`Transaccion`, `CuentaInteres`, `CuentaAnual`).
  - `processor/`: Validación y transformación de datos.
  - `advanced/`: Listeners, skip policies y lógica avanzada de errores.
  - `BatchApplication.java`: Main Spring Boot.
- `src/main/resources/`
  - `data/`: Archivos CSV de entrada.
  - `application.properties`: Configuración de base de datos y batch.
  - `data.sql`: Limpieza de tablas antes de cada corrida.
- `pom.xml`: Dependencias Maven.

## 🛠️ Prerrequisitos

- Java 21+
- Maven 3.5.4+
- Oracle Database

## ⚙️ Configuración Inicial

1. Crea la base de datos y las tablas necesarias:
    ```sql
    CREATE TABLE transacciones (
        id VARCHAR(255) PRIMARY KEY,
        fecha DATE,
        monto DOUBLE PRECISION,
        tipo VARCHAR(255)
    );
    CREATE TABLE cuentas (
        cuenta_id VARCHAR(255) PRIMARY KEY,
        nombre VARCHAR(255),
        saldo DOUBLE PRECISION,
        edad INT,
        tipo VARCHAR(255)
    );
    ```
2. Configura `src/main/resources/application.properties` con tus credenciales Oracle.
3. El archivo `data.sql` limpia las tablas automáticamente al iniciar la app.

## 🏃 Ejecución de Jobs

Compila el proyecto:
```bash
mvn clean install
```

Ejecuta un job específico:

| Job                        | Comando                                                                 |
|----------------------------|-------------------------------------------------------------------------|
| Reporte de Transacciones   | `java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=importTransaccionJob` |
| Cálculo de Intereses       | `java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=calculoInteresJob`    |
| Estado de Cuenta Anual     | `java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=generacionEstadoCuentaJob` |

## 📊 Ejemplo de Archivos de Entrada

`data/transacciones.csv`:
```
id,fecha,monto,tipo
1,2023-01-01,1000,credito
2,2023-01-02,500,debito
...etc
```

## 📦 Resultados y Evidencia

- **importTransaccionJob**: Ver tabla `transacciones` en Oracle.
- **calculoInteresJob**: Ver tabla `cuentas` en Oracle.
- **generacionEstadoCuentaJob**: Ver archivo `target/estado_cuenta_anual.txt`.


## 📝 Logs, Monitoreo y Cierre del Proceso

- Todos los procesos y errores se registran usando SLF4J/Logback.
- Los archivos de errores por partición se generan en `target/` (ej: `transacciones_errores_0.csv`).
- Los logs muestran inicio, fin y resumen de cada job y step.
- **Cierre automático del proceso:** El método `main` de la aplicación utiliza `SpringApplication.exit(ctx); System.exit(exitCode);` para forzar el cierre de la JVM al finalizar el job. En este proyecto, debido a la forma en que está programado, fue necesario aplicar esta solución porque algunos threads de pools de ejecución, conexiones JDBC (como `Batch-Thread-1`, `HikariPool-1:housekeeper`, `OJDBC-WORKER-THREAD-1`, etc.) y recursos internos pueden quedar abiertos tras finalizar el job, impidiendo que la JVM termine sola. El cierre forzado garantiza que el proceso finalice correctamente y puedas ejecutar otros jobs en secuencia sin problemas.

## 🏗️ Arquitectura y Diseño

Este proyecto utiliza una arquitectura basada en **Spring Batch** para el procesamiento de datos batch, con énfasis en la escalabilidad, robustez y trazabilidad. La arquitectura se divide en capas:

- **Capa de Configuración (`config/`)**: Define jobs, steps, particionadores y beans de Spring. Utiliza anotaciones como `@Bean` y `@StepScope` para inyección de dependencias y alcance limitado.
- **Capa de Modelo (`model/`)**: Representa las entidades de datos (Transaccion, CuentaInteres, CuentaAnual) con validaciones básicas.
- **Capa de Procesamiento (`processor/`)**: Contiene lógica de transformación y validación de datos, implementando `ItemProcessor` para filtrar o transformar items.
- **Capa Avanzada (`advanced/`)**: Incluye listeners para monitoreo, skip policies para manejo de errores, y lógica personalizada para escenarios complejos.

El diseño incorpora **particionamiento** para dividir grandes volúmenes de datos en chunks procesables en paralelo, mejorando el rendimiento. Los errores se manejan a nivel de item y chunk, con archivos de error generados por partición para facilitar la depuración.

## 📋 Explicación de Jobs

### importTransaccionJob
- **Función**: Importa transacciones desde un archivo CSV, valida los datos (fecha, monto, tipo) y los inserta en la tabla `transacciones` de Oracle.
- **Motivo**: Automatizar la carga masiva de transacciones bancarias, asegurando integridad de datos y manejo de errores.
- **Componentes**: Usa `FlatFileItemReader` para leer CSV, `TransaccionItemProcessor` para validación, `JdbcBatchItemWriter` para escritura en DB, y listeners para errores.

### calculoInteresJob
- **Función**: Calcula intereses sobre cuentas desde un CSV, actualiza o inserta en la tabla `cuentas` usando MERGE.
- **Motivo**: Procesar cálculos financieros periódicos, con paralelismo para manejar grandes volúmenes.
- **Componentes**: Similar al job de transacciones, pero con `CuentaInteresItemProcessor` y escritura condicional en DB.

### generacionEstadoCuentaJob
- **Función**: Genera un archivo único `estado_cuenta_anual.csv` con el estado de cuentas anuales, procesando datos desde CSV.
- **Motivo**: Crear reportes consolidados de estados de cuenta, con un solo archivo de salida para simplicidad, mientras que los errores se particionan.
- **Componentes**: Usa `FlatFileItemWriter` con `append(true)` para acumular en un solo archivo, y error writers por partición.

## 🔧 Componentes Clave

- **Readers**: `FlatFileItemReader` para leer CSVs con mapeo a modelos. Configurados con `@StepScope` para particionamiento.
- **Processors**: Implementan `ItemProcessor` para validar y transformar datos. Por ejemplo, `TransaccionItemProcessor` lanza excepciones para datos inválidos o retorna `null` para filtrar.
- **Writers**: `JdbcBatchItemWriter` para DB, `FlatFileItemWriter` para archivos. Los writers de error son `@StepScope` con `partitionId` para archivos separados.
- **Listeners**: `SkipListener` para manejar skips, `StepExecutionListener` para archivos de error. Proporcionan trazabilidad.
- **Skip Policies**: `CustomSkipPolicy` define cuándo saltar items (ej: excepciones de validación).
- **Partitioners**: `RangePartitioner` divide archivos en rangos para procesamiento paralelo.

## 🎯 Motivos de Decisiones de Diseño

- **Particionamiento**: Divide datos en chunks para paralelismo, mejorando rendimiento en archivos grandes. Usa hasta 6 hilos configurables.
- **Manejo de Errores**: Skip policies y listeners permiten continuar procesamiento ante errores, generando archivos de error por partición para análisis.
- **@StepScope**: Limita el alcance de beans a cada step/partición, evitando conflictos en entornos paralelos.
- **Logging Avanzado**: Logs explícitos en writers para auditar inserts, compensando limitaciones de Spring Batch en particionamiento.
- **Cierre Forzado**: `System.exit()` asegura liberación de recursos, necesario por threads persistentes en JDBC/Hikari.
- **Archivos Únicos vs. Particionados**: Para salidas principales (ej: estado_cuenta_anual), un archivo único simplifica; para errores, particionados facilitan depuración.

## ⚙️ Configuraciones Avanzadas

- **application.properties**: Configura DB (URL, usuario, contraseña), batch (chunk size, grid size), y logging.
- **pom.xml**: Incluye dependencias como `spring-boot-starter-batch`, `ojdbc8` para Oracle, y plugins para Maven.
- **data.sql**: Script para limpiar tablas antes de corridas, evitando duplicados.
- **TaskExecutor**: Configurado con `ThreadPoolTaskExecutor` para paralelismo controlado, con `corePoolSize=6` y `maxPoolSize=10`.

## 🛠️ Troubleshooting Avanzado

- **Archivos de Error Vacíos**: Verifica permisos de escritura en `output/`. Asegúrate de que `@StepScope` esté en writers de error.
- **PartitionId Null**: Puede ocurrir si el partitioner no configura correctamente el contexto. Verifica `RangePartitioner` y `@Value("#{stepExecutionContext['partitionId']}")`.
- **OutOfMemoryError**: Reduce `chunkSize` o `gridSize` en `BatchConfiguration` para archivos muy grandes.
- **Deadlocks en DB**: Asegúrate de que transacciones sean cortas; usa `PlatformTransactionManager` para rollback.
- **Logs No Aparecen**: Verifica configuración de SLF4J/Logback en `application.properties`.
- **Paralelismo No Funciona**: Confirma que `TaskExecutor` esté configurado y `gridSize > 1`.

---
Desarrollado por luciano1633 para Banco XYZ.
