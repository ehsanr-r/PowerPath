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

data class BackupPreview(
    val imageCount: Int,
    val previewImagePaths: List<String>
)

@Singleton
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: PowerPathDatabase
) {

    companion object {
        private const val IMAGES_DIR_NAME = "workout_images"
        private const val PREVIEW_DIR_NAME = "backup_preview"
        private const val MAX_PREVIEW_IMAGES = 8
    }

    suspend fun exportTo(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            checkpointWalIfPossible()

            val main = context.getDatabasePath(POWER_PATH_DB_NAME)
            val wal = File(main.path + "-wal")
            val shm = File(main.path + "-shm")
            val imagesDir = File(context.filesDir, IMAGES_DIR_NAME)

            if (!main.exists()) error("Database file not found.")

            context.contentResolver.openOutputStream(uri)?.use { rawOutput ->
                ZipOutputStream(BufferedOutputStream(rawOutput)).use { zip ->
                    addFileToZip(zip, main, main.name)
                    if (wal.exists()) addFileToZip(zip, wal, wal.name)
                    if (shm.exists()) addFileToZip(zip, shm, shm.name)

                    if (imagesDir.exists()) {
                        imagesDir.walkTopDown()
                            .filter { it.isFile }
                            .forEach { file ->
                                val relativePath = file.relativeTo(imagesDir).invariantSeparatorsPath
                                addFileToZip(
                                    zip = zip,
                                    file = file,
                                    zipPath = "$IMAGES_DIR_NAME/$relativePath"
                                )
                            }
                    }
                }
            } ?: error("Could not open export destination.")
        }
    }

    suspend fun previewImport(uri: Uri): Result<BackupPreview> = withContext(Dispatchers.IO) {
        runCatching {
            clearPreviewCache()

            val previewDir = File(context.cacheDir, PREVIEW_DIR_NAME).apply { mkdirs() }
            val allowedDbNames = setOf(
                POWER_PATH_DB_NAME,
                "$POWER_PATH_DB_NAME-wal",
                "$POWER_PATH_DB_NAME-shm"
            )

            var hasDatabase = false
            var imageCount = 0
            val previewPaths = mutableListOf<String>()

            context.contentResolver.openInputStream(uri)?.use { rawInput ->
                ZipInputStream(BufferedInputStream(rawInput)).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        val normalizedName = normalizeEntryName(entry.name)

                        if (!entry.isDirectory) {
                            val fileName = normalizedName.substringAfterLast('/')

                            when {
                                fileName in allowedDbNames -> {
                                    hasDatabase = true
                                    drainEntry(zip)
                                }

                                normalizedName.startsWith("$IMAGES_DIR_NAME/") -> {
                                    imageCount++

                                    if (previewPaths.size < MAX_PREVIEW_IMAGES) {
                                        val safeName = "${previewPaths.size}_${UUID.randomUUID()}_$fileName"
                                        val outFile = File(previewDir, safeName)
                                        outFile.outputStream().use { output ->
                                            zip.copyTo(output)
                                        }
                                        previewPaths += outFile.absolutePath
                                    } else {
                                        drainEntry(zip)
                                    }
                                }

                                else -> {
                                    drainEntry(zip)
                                }
                            }
                        }

                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            } ?: error("Could not open the selected backup file.")

            if (!hasDatabase) {
                clearPreviewCache()
                error("This file is not a valid PowerPath backup.")
            }

            BackupPreview(
                imageCount = imageCount,
                previewImagePaths = previewPaths
            )
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
            val importedImagesDir = File(tempDir, IMAGES_DIR_NAME)

            if (!importedMain.exists()) {
                error("This file is not a valid PowerPath backup.")
            }

            val targetMain = context.getDatabasePath(POWER_PATH_DB_NAME)
            val targetWal = File(targetMain.path + "-wal")
            val targetShm = File(targetMain.path + "-shm")
            val targetImagesDir = File(context.filesDir, IMAGES_DIR_NAME)

            targetMain.parentFile?.mkdirs()

            db.close()

            deleteIfExists(targetMain)
            deleteIfExists(targetWal)
            deleteIfExists(targetShm)
            deleteRecursivelyIfExists(targetImagesDir)

            importedMain.copyTo(targetMain, overwrite = true)
            if (importedWal.exists()) importedWal.copyTo(targetWal, overwrite = true)
            if (importedShm.exists()) importedShm.copyTo(targetShm, overwrite = true)

            if (importedImagesDir.exists()) {
                copyDirectory(importedImagesDir, targetImagesDir)
            }

            clearPreviewCache()
        }.also {
            tempDir.deleteRecursively()
        }
    }

    fun clearPreviewCache() {
        runCatching {
            File(context.cacheDir, PREVIEW_DIR_NAME).deleteRecursively()
        }
    }

    private fun checkpointWalIfPossible() {
        runCatching {
            db.query("PRAGMA wal_checkpoint(FULL)", null).use { }
        }
    }

    private fun addFileToZip(
        zip: ZipOutputStream,
        file: File,
        zipPath: String
    ) {
        file.inputStream().use { input ->
            zip.putNextEntry(ZipEntry(zipPath))
            input.copyTo(zip)
            zip.closeEntry()
        }
    }

    private fun unzipIntoTempDir(uri: Uri, tempDir: File) {
        val allowedDbNames = setOf(
            POWER_PATH_DB_NAME,
            "$POWER_PATH_DB_NAME-wal",
            "$POWER_PATH_DB_NAME-shm"
        )

        val tempRoot = tempDir.canonicalFile
        val imagesRoot = File(tempRoot, IMAGES_DIR_NAME).canonicalFile

        context.contentResolver.openInputStream(uri)?.use { rawInput ->
            ZipInputStream(BufferedInputStream(rawInput)).use { zip ->
                var entry = zip.nextEntry

                while (entry != null) {
                    val normalizedName = normalizeEntryName(entry.name)

                    if (!entry.isDirectory) {
                        val fileName = normalizedName.substringAfterLast('/')

                        when {
                            fileName in allowedDbNames -> {
                                val outFile = File(tempRoot, fileName).canonicalFile
                                if (!outFile.path.startsWith(tempRoot.path + File.separator)) {
                                    error("Unsafe backup file.")
                                }
                                outFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                            }

                            normalizedName.startsWith("$IMAGES_DIR_NAME/") -> {
                                val relativePath = normalizedName.removePrefix("$IMAGES_DIR_NAME/")
                                if (relativePath.isNotBlank()) {
                                    val outFile = File(imagesRoot, relativePath).canonicalFile
                                    if (!outFile.path.startsWith(imagesRoot.path + File.separator)) {
                                        error("Unsafe backup image path.")
                                    }
                                    outFile.parentFile?.mkdirs()
                                    outFile.outputStream().use { output ->
                                        zip.copyTo(output)
                                    }
                                } else {
                                    drainEntry(zip)
                                }
                            }

                            else -> {
                                drainEntry(zip)
                            }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("Could not open the selected backup file.")
    }

    private fun normalizeEntryName(name: String): String {
        return name.replace('\\', '/').trimStart('/')
    }

    private fun drainEntry(zip: ZipInputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (zip.read(buffer) != -1) {
            // drain ignored entry
        }
    }

    private fun deleteIfExists(file: File) {
        if (file.exists() && !file.delete()) {
            error("Could not replace ${file.name}.")
        }
    }

    private fun deleteRecursivelyIfExists(file: File) {
        if (file.exists() && !file.deleteRecursively()) {
            error("Could not replace ${file.name}.")
        }
    }

    private fun copyDirectory(source: File, target: File) {
        source.walkTopDown().forEach { file ->
            val relative = file.relativeTo(source)
            val targetFile = File(target, relative.path)

            if (file.isDirectory) {
                targetFile.mkdirs()
            } else {
                targetFile.parentFile?.mkdirs()
                file.copyTo(targetFile, overwrite = true)
            }
        }
    }
}