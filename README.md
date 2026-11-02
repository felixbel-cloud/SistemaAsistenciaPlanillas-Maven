# Sistema de Asistencia y Planillas — Instituto Americano

Proyecto convertido de **NetBeans (Ant)** a **Maven**, listo para abrir y ejecutar en **Visual Studio Code**.

## Requisitos previos

1. **JDK 17 o superior** instalado (compatible con JDK 17, 21, 25).
2. **Maven** (VS Code puede usar el que trae integrado el extension pack, no hace falta instalarlo aparte).
3. **MySQL** corriendo en `localhost:3306`.
4. En VS Code, instala el paquete de extensiones **"Extension Pack for Java"** (Microsoft) — incluye soporte de Maven, debugger y ejecución con un clic.

## Base de datos

El archivo `instituto_americano.sql` (en la raíz de este proyecto) ya incluye la creación automática de la base de datos y todas las tablas con su información completa (personal, horarios, asistencias, planillas, usuarios y roles).

### Opción A (MySQL Workbench):
1. Abre MySQL Workbench y conéctate a tu servidor local.
2. Ve a `File > Open SQL Script...` y selecciona `instituto_americano.sql`.
3. Haz clic en el ícono del rayo (⚡) para ejecutar todo el script.

### Opción B (Línea de comandos):
```bash
mysql -u root -p < instituto_americano.sql
```

### ¿Ya tenías la base importada?

Si ya importaste una versión anterior de `instituto_americano.sql` y no quieres perder tus datos, ejecuta **solo** `migrar_correcciones.sql` (Workbench o `mysql -u root -p < migrar_correcciones.sql`). Repara las tildes, cambia la clave única de `asistencia` para permitir varios bloques por día, crea la tabla de **feriados** (con los feriados nacionales del Perú 2025-2027) y agrega la protección contra registros duplicados del personal sin horario. Se puede ejecutar más de una vez sin problema. Las contraseñas antiguas en texto plano se convierten a hash solas, la primera vez que cada usuario inicia sesión.

> **Importante:** para **todo el personal** (tanto administrativo como docente), `salario_base` representa su **sueldo mensual fijo**. Este monto no sube salvo por horas extras, y solo tiene descuentos por inasistencias y tardanzas.

La conexión está configurada en `src/main/java/modelo/conexion/ConexionDB.java`:

```java
URL      = "jdbc:mysql://localhost:3306/instituto_americano"
USUARIO  = "root"
PASSWORD = "root" // Cambia a "2024" o tu contraseña local si es diferente
```

## Cómo abrir y ejecutar en VS Code

1. Abre la carpeta `maven-project` (esta carpeta) directamente en VS Code: `File > Open Folder...`.
2. Espera a que la extensión de Java indexe el proyecto (barra de progreso abajo). Al detectar el `pom.xml`, descargará automáticamente todas las dependencias (MySQL connector, JCalendar, Apache POI) desde Maven Central — necesitas conexión a internet la primera vez.
3. Abre `src/main/java/main/Main.java`.
4. Haz clic en **"Run"** (▶) que aparece justo encima de `public static void main`, o presiona `F5` (ya hay una configuración de debug lista en `.vscode/launch.json`).

### Alternativa por terminal

```bash
mvn compile exec:java
```

### Generar un .jar ejecutable (con todas las dependencias incluidas)

```bash
mvn clean package
java -jar target/SistemaAsistenciaPlanillas.jar
```

## Estructura del proyecto

```
maven-project/
├── pom.xml                     # Dependencias y configuración de build (reemplaza a build.xml/nbproject)
├── instituto_americano.sql     # Script de la base de datos
├── migrar_correcciones.sql     # Aplica las correcciones a una base ya existente
├── .vscode/
│   ├── launch.json             # Configuración de ejecución/depuración
│   └── settings.json
└── src/main/java/
    ├── main/                   # Punto de entrada (Main.java)
    ├── controlador/            # Controladores
    ├── modelo/
    │   ├── conexion/           # ConexionDB
    │   ├── dao/                # DAOs
    │   └── entidades/          # Entidades
    ├── util/                   # Validaciones, ExportadorReportes (Excel)
    └── vista/                  # Ventanas Swing (Login, MenuPrincipal, etc.)
```

## Dependencias (declaradas en `pom.xml`, se descargan solas)

| Librería | Versión | Uso |
|---|---|---|
| `com.mysql:mysql-connector-j` | 9.3.0 | Driver JDBC para MySQL |
| `com.toedter:jcalendar` | 1.4 | Selector de fechas (`JDateChooser`) |
| `org.apache.poi:poi` + `poi-ooxml` | 5.2.3 | Exportación de reportes a Excel |

No necesitas descargar ni copiar los `.jar` manualmente como en NetBeans: Maven los resuelve solo a partir del `pom.xml`.

## Notas de la migración

- El código fuente (paquetes `controlador`, `main`, `modelo`, `util`, `vista`) es el mismo, solo se movió a la carpeta estándar de Maven `src/main/java`.
- No había archivos `.form` de NetBeans (los formularios Swing están escritos directamente en Java), así que no hubo nada que "desenganchar" del editor visual de NetBeans.
- Se quitaron `poi-ooxml-schemas`, `xmlbeans`, `commons-collections4`, `commons-compress` y `commons-io` de las dependencias explícitas: Maven los trae automáticamente como dependencias transitivas de `poi-ooxml`.
- Las carpetas `build/`, `dist/` y `nbproject/` de NetBeans no se copiaron (ya no se usan).

## Correcciones aplicadas

1. **Tildes y eñes** del script SQL reparadas (antes se veía `P├®rez` en lugar de `Pérez`).
2. **Varios bloques por día:** la clave única de `asistencia` ahora es `(id_personal, fecha, id_horario)`. Además, la entrada se asocia al bloque que todavía no termina (si alguien falta en la mañana y llega en la tarde, se registra en el bloque de la tarde).
3. **Tarifas de docentes** de ejemplo convertidas a pago por hora (sueldo mensual / 80 h).
4. **Planilla docente:** las horas extra ya no se pagan dos veces (se separan horas regulares y extra), y la tardanza y las faltas ya no se descuentan aparte, porque esas horas no se pagan.
5. **Faltas automáticas:** solo se registran en bloques que ya terminaron. Generar la planilla del mes en curso ya no marca como ausente a quien todavía no ha llegado.
6. **Registrar salida:** solo cierra una entrada de hoy que tenga hora de entrada. Ya no falla con una falta (AUSENTE) ni cierra entradas de días anteriores; muestra un mensaje claro.

## Reglas de registro de asistencia

Se aplican al personal que **tiene horario detallado** (configurado en *Gestión de Horarios*):

| Situación | Resultado |
|---|---|
| Marca un día que no está en su horario (ej. domingo) | **Bloqueado**: "no tiene horario asignado para hoy". No se registra nada. |
| Marca más de 30 min antes del inicio del bloque | **Bloqueado**: indica desde qué hora puede marcar. |
| Marca desde 30 min antes hasta el inicio + 5 min de tolerancia | Se registra como **PRESENTE**. |
| Marca después de la tolerancia y antes del fin del bloque | Se registra como **TARDANZA**. |
| Marca cuando el bloque ya terminó | **Bloqueado**: "su horario de hoy ya terminó". |

**Horas que se pagan** (al registrar la salida):
- Se cuentan **desde la hora de inicio del bloque**, aunque llegue antes (llegar temprano no suma horas).
- Si llega **dentro de la tolerancia** (hasta 5 min tarde), se considera puntual y se le paga desde el inicio del bloque. Si llega más tarde, se le paga desde su hora real de entrada.
- Lo que pase del fin del bloque es hora extra **solo si son 15 min o más**.
- Se reconocen como **máximo 2 h extra** por bloque al día (protege si alguien olvida marcar la salida).

El personal **sin ningún horario detallado** sigue marcando como antes (sin restricción, jornada de referencia de 9 h), con el mismo mínimo de 15 min y tope de 2 h para las horas extra.

Los valores se pueden cambiar en `ControladorAsistencia.java`: `VENTANA_ANTES_MINUTOS`, `TOLERANCIA_MINUTOS`, `MINIMO_EXTRAS_MINUTOS` y `TOPE_EXTRAS_MINUTOS`.

## Modo simulación (para demostraciones)

Permite registrar entradas y salidas con **otra fecha y hora**, sin cambiar el reloj de la computadora. Solo lo ve el usuario **administrador** (`admin`).

1. Ingresa como `admin` y abre **Registrar Asistencia**.
2. En el recuadro naranja *Modo simulación*, marca **"Registrar con otra fecha y hora"**. El título se pone naranja mientras está activo.
3. Busca al docente por DNI, elige la **fecha** y la **hora**, y pulsa **Registrar Entrada**. Luego cambia la hora y pulsa **Registrar Salida**. En modo simulación la persona queda cargada para hacer varios registros seguidos.
4. Se aplican las mismas reglas reales: bloqueo fuera de horario, tolerancia, tardanza, mínimo de 15 min y tope de 2 h de horas extra.
5. Abre **Reportes** (Asistencia, Tardanzas, Horas Extras) con el rango de fechas simulado para mostrar los resultados.
6. Al terminar, pulsa **"Borrar registros simulados"**: elimina solo los registros marcados como `[SIMULADO]` y no toca los reales.

**Ejemplo de demostración** (docente con horario lunes, martes y jueves de 08:00 a 15:00, S/ 15 por hora):

| Fecha y hora | Acción | Resultado |
|---|---|---|
| Lun 19/10 07:50 → 16:00 | Entrada y salida | PRESENTE · 8 h a pagar · 1 h extra |
| Mar 20/10 08:20 → 15:10 | Entrada y salida | TARDANZA 15 min · 10 min después no cuentan como extra |
| Jue 22/10 08:00 → 23:00 | Entrada y salida | 2 h extra (se aplica el tope) |
| Dom 25/10 23:00 | Entrada | Bloqueado: no tiene horario ese día |
| Lun 26/10 06:30 | Entrada | Bloqueado: puede marcar desde las 07:30 |

Reporte de horas extras: 3 h → **S/ 56.25** (3 × 15 × 1.25).

## Política salarial y cálculo de planillas (D. Leg. 713)

Todo el personal (**docente y administrativo**) cuenta con un **sueldo mensual fijo**:
- El sueldo mensual pactado es fijo y **no sube salvo por horas extras** (calculadas al 125% del valor hora ordinario).
- **Inasistencias (faltas):** se descuenta el día de trabajo (sueldo / 30) más la parte alícuota del descanso semanal remunerado (DSR: 1/N). Si hay inasistencias masivas o abandono, el pago devengado se liquida estrictamente según los días trabajados más su DSR proporcional ganado.
- **Tardanzas:** se descuentan minuto a minuto a su valor hora ordinario (sueldo / 30 / 8).

## Reporte de Tardanzas y Faltas (alertas)

Muestra por persona el número de tardanzas, faltas y minutos de retraso del periodo. Quien acumula **3 o más incidencias** (tardanzas + faltas) aparece **en rojo como "EN ALERTA"**, ordenado primero. Sirve para que RR. HH. haga seguimiento sin afectar el pago.

Al generar el reporte, se detectan automáticamente las faltas de los bloques que ya terminaron, igual que al generar la planilla. No se marcan faltas antes de la fecha de contratación. El umbral se puede cambiar en `ControladorReporte.UMBRAL_ALERTA`.

## Reportes individuales en PDF (una sola persona)

En **Reporte de Asistencias, Consulta de Asistencias, Tardanzas y Faltas, Horas Extras y Consulta de Planillas** se puede exportar un PDF solo con los datos de una persona:

1. Genera el reporte y **haz clic en la fila** de la persona.
2. Pulsa **"PDF individual"** (en planillas, **"Boleta individual"**). También puedes hacer **doble clic** en la fila o usar **clic derecho → Exportar PDF de esta persona**.

Cada PDF lleva una ficha con el nombre, DNI, tipo, cargo y tarifa o sueldo de la persona, más:

| Reporte | Contenido del PDF individual |
|---|---|
| Asistencias | Resumen (registros, presentes, tardanzas, ausentes) y el detalle día por día, en orden cronológico y con el día de la semana. |
| Tardanzas y Faltas | Número de tardanzas y faltas, tiempo total de tardanza, situación (NORMAL / EN ALERTA) y el detalle de cada día. |
| Horas Extras | Días con horas extra, total de horas, monto estimado y el detalle por día con su monto. |
| Planilla | **Boleta de pago**: asistencia del periodo, ingresos, descuentos, **neto a pagar**, explicación de cómo se calculó y líneas de firma. |

## Seguridad y robustez

| Tema | Cómo funciona |
|---|---|
| **Contraseñas** | Se guardan con hash **PBKDF2-HMAC-SHA256** (120 000 iteraciones) y sal aleatoria (`util/Seguridad.java`), nunca en texto plano. Si una base antigua tiene contraseñas en texto plano, se aceptan una vez y se convierten a hash automáticamente. Para generar un hash a mano: `java -cp target/classes util.Seguridad MiClave`. |
| **Planilla atómica** | La planilla y todos sus detalles se guardan en **una transacción** (`commit`/`rollback`). Si algo falla a la mitad, no queda nada guardado y se puede volver a generar. |
| **Registros simulados** | Si el periodo tiene registros `[SIMULADO]`, el sistema avisa antes de generar la planilla. Por defecto cancela; solo se genera si se elige "Generar de todos modos (demostración)". |
| **Conexión** | Si MySQL se cae, el sistema muestra *"No hay conexión con la base de datos"* en lugar de fallar con errores internos, y se reconecta solo cuando MySQL vuelve. |
| **Feriados** | *Mantenimiento → Gestión de Feriados*. En un feriado no se marcan faltas ni se descuenta al personal administrativo. Al declarar feriado un día que ya tenía faltas automáticas, se quitan. Incluye los feriados nacionales del Perú 2025-2027; los de años siguientes se agregan desde la ventana. |
| **Duplicados sin horario** | Para el personal sin horario detallado, un *trigger* de la base de datos impide dos registros de asistencia el mismo día (la clave única no lo cubre porque `id_horario` es `NULL`). |
| **Periodo de planilla** | Desde 2020-01 hasta el mes actual; no se puede generar la planilla de un mes que todavía no empieza. |
| **Ingreso a mitad de mes** | Al personal administrativo contratado durante el mes se le paga proporcional, sobre 30 días: si entra el día 16, cobra 15/30 del sueldo. El personal contratado después del periodo no aparece en esa planilla. |

**Limitación conocida:** los horarios no pueden cruzar la medianoche, porque la hora de fin debe ser posterior a la de inicio. Para un turno nocturno, regístrelo como dos bloques, por ejemplo 18:00-23:59 y 00:00-02:00 del día siguiente.
