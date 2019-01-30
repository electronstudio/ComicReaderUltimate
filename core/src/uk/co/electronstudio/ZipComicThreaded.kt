package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import java.util.zip.ZipFile
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit


class ZipComicThreaded(filename: String):Comic(filename) {

    val threads = Runtime.getRuntime().availableProcessors()

    private val es = Executors.newFixedThreadPool(threads)

    override fun loadPixmaps() {





        val zipFile = ZipFile(filename)
        println("zip open, $threads threads")
        zipFile.use {
            it.entries().toList().filter { imageRegex.matches(it.name) }.forEach {
                println("zip entry")
                es.submit({
                    zipFile.getInputStream(it).buffered().use {
                        println("readbytes")
                        val bytes = it.readBytes(10000000)
                        println("makepixmap")
                        val pixmap = Pixmap(bytes, 0, bytes.size)
                        pages.add(Page(pixmap))
                        Gdx.graphics.isContinuousRendering = true
                        Gdx.graphics.requestRendering()
                        println("${bytes.size} $it")

                    }
                })
            }
            println("waiting for termination")
            es.shutdown()
            es.awaitTermination(60, TimeUnit.SECONDS)

        }
    }
}