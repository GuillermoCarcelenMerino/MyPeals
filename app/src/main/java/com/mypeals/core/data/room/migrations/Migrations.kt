package com.mypeals.core.data.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: la tabla `peals` (nombre, cantidad, tipo) pasa a `medications` con unidades por
 * caja, frecuencia y hora de primera toma, y se crea el historial `intake_log`.
 *
 * Los datos antiguos no tenían horario, así que se migran con una frecuencia diaria
 * provisional y en pausa (`active = 0`): no generarán avisos hasta que el usuario les asigne
 * un horario. La cantidad antigua contaba unidades, por eso se conserva como una única caja
 * de esa cantidad.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `medications` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`units_per_box` INTEGER, " +
                "`remaining_units` INTEGER NOT NULL, " +
                "`dose_per_intake` INTEGER NOT NULL, " +
                "`frequency_interval` INTEGER NOT NULL, " +
                "`frequency_unit` TEXT NOT NULL, " +
                "`days_of_week` INTEGER, " +
                "`first_intake_at` INTEGER NOT NULL, " +
                "`active` INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO `medications` (`id`, `name`, `type`, `units_per_box`, `remaining_units`, " +
                "`dose_per_intake`, `frequency_interval`, `frequency_unit`, `days_of_week`, " +
                "`first_intake_at`, `active`) " +
                "SELECT `id`, `peal_name`, " +
                "CASE `type` WHEN 'INJECTION' THEN 'INJECTION' ELSE 'PILL' END, " +
                "CASE WHEN `quantity` > 0 THEN `quantity` ELSE NULL END, " +
                "MAX(`quantity`, 0), 1, 1, 'DAYS', NULL, " +
                "CAST(strftime('%s', 'now') AS INTEGER) * 1000, 0 " +
                "FROM `peals`"
        )
        db.execSQL("DROP TABLE `peals`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `intake_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`medication_id` INTEGER NOT NULL, " +
                "`scheduled_at` INTEGER NOT NULL, " +
                "`taken_at` INTEGER, " +
                "`status` TEXT NOT NULL, " +
                "FOREIGN KEY(`medication_id`) REFERENCES `medications`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_intake_log_medication_id` " +
                "ON `intake_log` (`medication_id`)"
        )
    }
}
