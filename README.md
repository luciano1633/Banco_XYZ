# Proyecto de Migración de Procesos Batch con Spring Batch

## Objetivo del Proyecto

Este proyecto tiene como objetivo modernizar el sistema legacy del Banco XYZ mediante la implementación de un sistema de migración de procesos batch utilizando Spring Batch. Se recrean tres procesos clave: el reporte de transacciones diarias, el cálculo de intereses mensuales y la generación de estados de cuenta anuales.

## Estructura del Código

El proyecto está organizado de la siguiente manera:

- `src/main/java/com/duoc/batch/`: Contiene el código fuente de la aplicación.
  - `config/BatchConfiguration.java`: Configuración principal de los Jobs y Steps de Spring Batch.
  - `model/`: Clases de modelo que representan los datos (Transaccion, CuentaInteres, CuentaAnual).
  - `processor/`: Clases `ItemProcessor` que contienen la lógica de negocio para validar y transformar los datos.
  - `BatchApplication.java`: Clase principal de la aplicación Spring Boot.
- `src/main/resources/`: Contiene los recursos de la aplicación.
  - `data/`: Archivos CSV con los datos de entrada.
  - `application.properties`: Archivo de configuración de la aplicación, incluida la conexión a la base de datos.
- `pom.xml`: Archivo de configuración de Maven con las dependencias del proyecto.

## Instrucciones para Ejecutar el Proyecto

### Prerrequisitos

- Java 21 o superior
- Maven 3.5.4 o superior
- Una base de datos Oracle

### Configuración de la Base de Datos

1.  Crea una base de datos en tu motor de base de datos preferido.
2.  Actualiza el archivo `src/main/resources/application.properties` con la configuración de tu base de datos:

    ```properties
    # Ejemplo para Oracle
    spring.datasource.url=jdbc:oracle:thin:@localhost:1521:nombre_base_datos
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver
    spring.batch.jdbc.initialize-schema=always
    ```

3.  Asegúrate de tener las tablas necesarias. Spring Batch puede crearlas automáticamente si `spring.batch.jdbc.initialize-schema` está configurado como `always`. Además, necesitarás las tablas para los datos procesados:

    ```sql
    -- Tabla para Transacciones
    CREATE TABLE transacciones (
        id VARCHAR(255) PRIMARY KEY,
        fecha DATE,
        monto DOUBLE PRECISION,
        tipo VARCHAR(255)
    );

    -- Tabla para Cuentas (usada por el job de intereses)
    CREATE TABLE cuentas (
        cuenta_id VARCHAR(255) PRIMARY KEY,
        nombre VARCHAR(255),
        saldo DOUBLE PRECISION,
        edad INT,
        tipo VARCHAR(255)
    );
    ```

### Ejecución de los Jobs

Puedes ejecutar los jobs de Spring Batch de varias maneras. Una forma es a través de la línea de comandos al iniciar la aplicación, especificando el nombre del job como argumento.

1.  **Compila el proyecto:**
    ```bash
    mvn clean install
    ```

2.  **Ejecuta un Job específico:**

    - **Reporte de Transacciones Diarias:**
      ```bash
      java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=importTransaccionJob
      ```

    - **Cálculo de Intereses Mensuales:**
      ```bash
      java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=calculoInteresJob
      ```

    - **Generación de Estados de Cuenta Anuales:**
      ```bash
      java -jar target/batch-0.0.1-SNAPSHOT.jar spring.batch.job.name=generacionEstadoCuentaJob
      ```

### Evidencia de Ejecución

Después de ejecutar cada job, puedes verificar los resultados:

- **`importTransaccionJob`**: Consulta la tabla `transacciones` en tu base de datos para ver los datos procesados.
- **`calculoInteresJob`**: Consulta la tabla `cuentas` para ver los saldos actualizados.
- **`generacionEstadoCuentaJob`**: Revisa el archivo `target/estado_cuenta_anual.txt` que se genera con el estado de cuenta.

La salida de la consola también mostrará el estado de la ejecución de cada job.
