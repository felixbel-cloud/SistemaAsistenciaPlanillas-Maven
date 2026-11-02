-- =====================================================================
-- migrar_correcciones.sql
-- Aplica las correcciones a una base 'instituto_americano' YA EXISTENTE
-- (sin borrar datos). Si vas a importar la base desde cero con
-- instituto_americano.sql, NO necesitas este script.
--
-- Ejecutar en MySQL Workbench o con:  mysql -u root -p < migrar_correcciones.sql
-- Es seguro ejecutarlo más de una vez.
-- =====================================================================
SET NAMES utf8mb4;
USE instituto_americano;
-- MySQL Workbench trae activado "Safe Updates", que bloquea estos UPDATE/DELETE (Error 1175).
-- Se desactiva solo durante este script y se vuelve a activar al final.
SET @safe_updates_previo = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------
-- 1) Reparar tildes y eñes dañadas (ej: 'P├®rez' -> 'Pérez')
-- ---------------------------------------------------------------------
UPDATE `personal` SET
    `nombres` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`nombres`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ'),
    `apellidos` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`apellidos`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ'),
    `cargo` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`cargo`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ')
WHERE `nombres` LIKE '%├%' OR `apellidos` LIKE '%├%' OR `cargo` LIKE '%├%';

UPDATE `rol` SET
    `descripcion` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`descripcion`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ')
WHERE `descripcion` LIKE '%├%';

UPDATE `horario_personal` SET
    `descripcion_bloque` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`descripcion_bloque`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ')
WHERE `descripcion_bloque` LIKE '%├%';

UPDATE `asistencia` SET
    `observaciones` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`observaciones`, '├¡', 'í'), '├®', 'é'), '├í', 'á'), '├ü', 'Á'), '├│', 'ó'), '├║', 'ú'), '├▒', 'ñ')
WHERE `observaciones` LIKE '%├%';

-- ---------------------------------------------------------------------
-- 2) Permitir varios bloques de horario por día:
--    la clave única pasa de (id_personal, fecha) a (id_personal, fecha, id_horario)
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS migrar_unique_asistencia;
DELIMITER //
CREATE PROCEDURE migrar_unique_asistencia()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.statistics
               WHERE table_schema = DATABASE() AND table_name = 'asistencia'
                 AND index_name = 'unique_asistencia') THEN
        ALTER TABLE asistencia DROP INDEX unique_asistencia;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics
                   WHERE table_schema = DATABASE() AND table_name = 'asistencia'
                     AND index_name = 'unique_asistencia_bloque') THEN
        ALTER TABLE asistencia
            ADD UNIQUE KEY unique_asistencia_bloque (id_personal, fecha, id_horario);
    END IF;
END //
DELIMITER ;
CALL migrar_unique_asistencia();
DROP PROCEDURE migrar_unique_asistencia;

-- ---------------------------------------------------------------------
-- 3) Todo el personal (administrativos y docentes) tiene sueldo mensual fijo.
--    No se divide por horas; el salario_base representa el sueldo mensual contratado.
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- 4) Faltas registradas por error en bloques que aún no habían terminado.
--    Se eliminan las faltas automáticas de HOY cuyo bloque todavía no
--    termina, para que esas personas puedan marcar su entrada.
-- ---------------------------------------------------------------------
DELETE a FROM asistencia a
JOIN horario_personal h ON h.id_horario = a.id_horario
WHERE a.estado = 'AUSENTE'
  AND a.hora_entrada IS NULL
  AND a.fecha = CURDATE()
  AND h.hora_fin > CURTIME()
  AND a.observaciones LIKE 'Falta detectada automáticamente%';

-- ---------------------------------------------------------------------
-- 5) Feriados: tabla y feriados nacionales del Perú 2025-2027
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `feriado` (
  `fecha` date NOT NULL,
  `descripcion` varchar(100) COLLATE utf8mb4_spanish_ci NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO `feriado` (`fecha`, `descripcion`) VALUES
('2025-01-01','Año Nuevo'),
('2025-04-17','Jueves Santo'),
('2025-04-18','Viernes Santo'),
('2025-05-01','Día del Trabajo'),
('2025-06-07','Batalla de Arica y Día de la Bandera'),
('2025-06-29','San Pedro y San Pablo'),
('2025-07-23','Día de la Fuerza Aérea del Perú'),
('2025-07-28','Fiestas Patrias'),
('2025-07-29','Fiestas Patrias'),
('2025-08-06','Batalla de Junín'),
('2025-08-30','Santa Rosa de Lima'),
('2025-10-08','Combate de Angamos'),
('2025-11-01','Día de Todos los Santos'),
('2025-12-08','Inmaculada Concepción'),
('2025-12-09','Batalla de Ayacucho'),
('2025-12-25','Navidad'),
('2026-01-01','Año Nuevo'),
('2026-04-02','Jueves Santo'),
('2026-04-03','Viernes Santo'),
('2026-05-01','Día del Trabajo'),
('2026-06-07','Batalla de Arica y Día de la Bandera'),
('2026-06-29','San Pedro y San Pablo'),
('2026-07-23','Día de la Fuerza Aérea del Perú'),
('2026-07-28','Fiestas Patrias'),
('2026-07-29','Fiestas Patrias'),
('2026-08-06','Batalla de Junín'),
('2026-08-30','Santa Rosa de Lima'),
('2026-10-08','Combate de Angamos'),
('2026-11-01','Día de Todos los Santos'),
('2026-12-08','Inmaculada Concepción'),
('2026-12-09','Batalla de Ayacucho'),
('2026-12-25','Navidad'),
('2027-01-01','Año Nuevo'),
('2027-03-25','Jueves Santo'),
('2027-03-26','Viernes Santo'),
('2027-05-01','Día del Trabajo'),
('2027-06-07','Batalla de Arica y Día de la Bandera'),
('2027-06-29','San Pedro y San Pablo'),
('2027-07-23','Día de la Fuerza Aérea del Perú'),
('2027-07-28','Fiestas Patrias'),
('2027-07-29','Fiestas Patrias'),
('2027-08-06','Batalla de Junín'),
('2027-08-30','Santa Rosa de Lima'),
('2027-10-08','Combate de Angamos'),
('2027-11-01','Día de Todos los Santos'),
('2027-12-08','Inmaculada Concepción'),
('2027-12-09','Batalla de Ayacucho'),
('2027-12-25','Navidad');

-- Quitar faltas automáticas que se hayan registrado en feriados
DELETE a FROM asistencia a
JOIN feriado f ON f.fecha = a.fecha
WHERE a.estado = 'AUSENTE' AND a.hora_entrada IS NULL;

-- ---------------------------------------------------------------------
-- 6) Evitar dos entradas el mismo día del personal sin horario detallado
--    (id_horario NULL). Primero se listan los duplicados que ya existan
--    (si aparece alguno, revíselo y borre el que sobre).
-- ---------------------------------------------------------------------
SELECT a.id_asistencia, a.id_personal, a.fecha, a.hora_entrada, a.hora_salida,
       'DUPLICADO: revise y elimine el que sobre' AS aviso
FROM asistencia a
JOIN (SELECT id_personal, fecha FROM asistencia WHERE id_horario IS NULL
      GROUP BY id_personal, fecha HAVING COUNT(*) > 1) d
  ON d.id_personal = a.id_personal AND d.fecha = a.fecha
WHERE a.id_horario IS NULL
ORDER BY a.id_personal, a.fecha, a.id_asistencia;

-- Personal SIN horario detallado: id_horario es NULL y una clave única no compara valores NULL.
-- Estos triggers hacen que la base de datos impida dos registros del mismo día para la misma
-- persona en ese caso (con horario, ya lo impide la clave unique_asistencia_bloque).
DROP TRIGGER IF EXISTS `asistencia_sin_horario_unica_ins`;
DROP TRIGGER IF EXISTS `asistencia_sin_horario_unica_upd`;
DELIMITER //
CREATE TRIGGER `asistencia_sin_horario_unica_ins` BEFORE INSERT ON `asistencia` FOR EACH ROW
BEGIN
  IF NEW.id_horario IS NULL AND EXISTS (
      SELECT 1 FROM asistencia
      WHERE id_personal = NEW.id_personal AND fecha = NEW.fecha AND id_horario IS NULL) THEN
    SIGNAL SQLSTATE '23000'
      SET MESSAGE_TEXT = 'Ya existe un registro de asistencia de esta persona para ese dia (sin horario detallado).';
  END IF;
END //
CREATE TRIGGER `asistencia_sin_horario_unica_upd` BEFORE UPDATE ON `asistencia` FOR EACH ROW
BEGIN
  IF NEW.id_horario IS NULL AND EXISTS (
      SELECT 1 FROM asistencia
      WHERE id_personal = NEW.id_personal AND fecha = NEW.fecha AND id_horario IS NULL
        AND id_asistencia <> NEW.id_asistencia) THEN
    SIGNAL SQLSTATE '23000'
      SET MESSAGE_TEXT = 'Ya existe un registro de asistencia de esta persona para ese dia (sin horario detallado).';
  END IF;
END //
DELIMITER ;

-- Restaurar la configuración de Safe Updates
SET SQL_SAFE_UPDATES = @safe_updates_previo;
