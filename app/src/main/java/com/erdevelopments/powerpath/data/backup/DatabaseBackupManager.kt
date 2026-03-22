package com.erdevelopments.powerpath.data.backup

import android.content.Context
import android.net.Uri
import com.erdevelopments.powerpath.data.local.POWER_PATH_DB_NAME
import com.erdevelopments.powerpath.data.local.PowerPathDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: PowerPathDatabase
) {

    suspend fun exportTo(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            checkpointWalIfPossible()

            val main = context.getDatabasePath(POWER_PATH_DB_NAME)
            val wal = File(main.path + "-wal")
            val shm = File(main.path + "-shm")

            if (!main.exists()) {
                error("Database file not found.")
            }

            context.contentResolver.openOutputStream(uri)?.use { rawOutput ->
                ZipOutputStream(BufferedOutputStream(rawOutput)).use { zip ->
                    addFileToZip(zip, main)
                    if (wal.exists()) addFileToZip(zip, wal)
                    if (shm.exists()) addFileToZip(zip, shm)
                }
            } ?: error("Could not open export destination.")
        }
    }

    suspend fun importFrom(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "powerpath_import_${UUID.randomUUID()}").apply {
            mkdirs()
        }

        runCatching {
            unzipIntoTempDir(uri, tempDir)

            val importedMain = File(tempDir, POWER_PATH_DB_NAME)
            val importedWal = File(tempDir, "$POWER_PATH_DB_NAME-wal")
            val importedShm = File(tempDir, "$POWER_PATH_DB_NAME-shm")

            if (!importedMain.exists()) {
                error("This file is not a valid PowerPath backup.")
            }

            val targetMain = context.getDatabasePath(POWER_PATH_DB_NAME)
            val targetWal = File(targetMain.path + "-wal")
            val targetShm = File(targetMain.path + "-shm")
            targetMain.parentFile?.mkdirs()

            db.close()

            deleteIfExists(targetMain)
            deleteIfExists(targetWal)
            deleteIfExists(targetShm)

            importedMain.copyTo(targetMain, overwrite = true)
            if (importedWal.exists()) importedWal.copyTo(targetWal, overwrite = true)
            if (importedShm.exists()) importedShm.copyTo(targetShm, overwrite = true)
        }.also {
            tempDir.deleteRecursively()
        }
    }

    private fun checkpointWalIfPossible() {
        runCatching {
            db.query("PRAGMA wal_checkpoint(FULL)", emptyArray<Any?>()).use { }
        }
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File) {
        file.inputStream().use { input ->
            zip.putNextEntry(ZipEntry(file.name))
            input.copyTo(zip)
            zip.closeEntry()
        }
    }

    private fun unzipIntoTempDir(uri: Uri, tempDir: File) {
        val allowedNames = setOf(
            POWER_PATH_DB_NAME,
            "$POWER_PATH_DB_NAME-wal",
            "$POWER_PATH_DB_NAME-shm"
        )

        val tempRoot = tempDir.canonicalFile

        context.contentResolver.openInputStream(uri)?.use { rawInput ->
            ZipInputStream(BufferedInputStream(rawInput)).use { zip ->
                var entry = zip.nextEntry

                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryName = entry.name.substringAfterLast('/')

                        if (entryName in allowedNames) {
                            val outFile = File(tempRoot, entryName).canonicalFile
                            if (!outFile.path.startsWith(tempRoot.path + File.separator)) {
                                error("Unsafe backup file.")
                            }

                            outFile.outputStream().use { output ->
                                zip.copyTo(output)
                            }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("Could not open the selected backup file.")
    }

    private fun deleteIfExists(file: File) {
        if (file.exists() && !file.delete()) {
            error("Could not replace ${file.name}.")
        }
    }
}