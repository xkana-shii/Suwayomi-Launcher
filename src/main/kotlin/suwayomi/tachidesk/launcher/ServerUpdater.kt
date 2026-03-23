package suwayomi.tachidesk.launcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.nio.file.Path

object ServerUpdater {
    private val logger = LoggerFactory.getLogger(ServerUpdater::class.java)
    private val client = OkHttpClient()

    suspend fun updateServerJar(destPath: Path): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val downloadUrl =
                    fetchLatestJarDownloadUrl()
                        ?: return@withContext Result.failure(Exception("Download url not found"))
                downloadJar(downloadUrl, destPath)
                Result.success(Unit)
            } catch (e: Exception) {
                logger.error("Failed updating server jar", e)
                Result.failure(e)
            }
        }

    private fun fetchLatestJarDownloadUrl(): String? {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/repos/xkana-shii/Suwayomi-Server/releases/latest")
                .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val regex = """"browser_download_url"\s*:\s*"([^"]+Suwayomi-Server[^"]+\.jar)"""".toRegex()
            val match = regex.find(body)
            return match?.groups?.get(1)?.value
        }
    }

    private fun downloadJar(
        downloadUrl: String,
        destPath: Path,
    ) {
        val request = Request.Builder().url(downloadUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to download jar (${response.code})")
            destPath.toFile().outputStream().use { output ->
                response.body?.byteStream()?.copyTo(output)
            }
        }
    }
}
