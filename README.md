# Sistema de Asistencia y Planillas — Instituto Americano

Proyecto convertido de **NetBeans (Ant)** a **Maven**, listo para abrir y ejecutar en **Visual Studio Code**.

## Requisitos previos

1. **JDK 25** instalado (`javac -version` debe mostrar 25.x).
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
├── migrar_horarios.sql         # Migración segura para bases ya existentes
├── migrar_asistencia_horas_trabajadas.sql # Campo faltante en esquemas antiguos
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
