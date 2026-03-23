package suwayomi.tachidesk.launcher

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.nio.file.Path

object ServerUpdater {
    private val logger = KotlinLogging.logger {}
    private val client = OkHttpClient()
    private val jarUrlRegex = """"browser_download_url"\s*:\s*"([^"]+Suwayomi-Server[^"]+\.jar)"""".toRegex()

    suspend fun updateServerJar(destPath: Path): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = fetchLatestJarDownloadUrl() ?: error("Download URL not found")
                downloadJar(url, destPath)
            }.onFailure { logger.error(it) { "Failed updating server jar" } }
        }

    private fun fetchLatestJarDownloadUrl(): String? =
        Request
            .Builder()
            .url("https://api.github.com/repos/xkana-shii/Suwayomi-Server/releases/latest")
            .build()
            .let(client::newCall)
            .execute()
            .use {
                it.body
                    ?.string()
                    ?.let(jarUrlRegex::find)
                    ?.groups
                    ?.get(1)
                    ?.value
            }

    private fun downloadJar(
        url: String,
        destPath: Path,
    ) {
        destPath.parent.toFile().mkdirs()
        Request
            .Builder()
            .url(url)
            .build()
            .let(client::newCall)
            .execute()
            .use { response ->
                if (!response.isSuccessful) error("Failed to download jar (${response.code})")
                destPath.toFile().outputStream().use { response.body?.byteStream()?.copyTo(it) }
            }
    }
}
