package io.legado.app.help.storage

import io.legado.app.App
import io.legado.app.data.entities.BookProgress
import io.legado.app.lib.webdav.WebDav
import io.legado.app.utils.FileUtils
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray

object SyncBookProgress {
    private val file = FileUtils.createFileIfNotExist(App.INSTANCE.cacheDir, "bookProgress.json")
    private val webDavUrl = WebDavHelp.getWebDavUrl() + "legado/bookProgress.json"

    fun uploadBookProgress() {
        val value = App.db.bookDao().allBookProgress
        if (value.isNotEmpty()) {
            val json = GSON.toJson(value)
            file.writeText(json)
            if (WebDavHelp.initWebDav()) {
                WebDav(WebDavHelp.getWebDavUrl() + "legado").makeAsDir()
                WebDav(webDavUrl).upload(file.absolutePath)
            }
        }
    }


    fun downloadBookProgress() {
        WebDav(webDavUrl).downloadTo(file.absolutePath, true)
        if (file.exists()) {
            val json = file.readText()
            GSON.fromJsonArray<BookProgress>(json)?.forEach {
                App.db.bookDao().upBookProgress(
                    it.bookUrl,
                    it.durChapterIndex,
                    it.durChapterPos
                )
            }
        }
    }

}