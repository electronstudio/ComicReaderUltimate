package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import java.util.zip.ZipFile
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit


class ZipComicThreaded(filename: String):Comic(filename) {
    override val pages = ArrayList<Page>()
    val zipFile = ZipFile(filename)
    val zippedImages = zipFile.entries().toList().sortedBy { it.name }.filter { imageRegex.matches(it.name) }


    private val es = Executors.newFixedThreadPool(numThreads)

    init {
            zippedImages.forEach {
                pages.add(Page(null))
            }
        if (pages.isEmpty()) throw Exception("no pages found in comic")
    }

    val errorPixmap =  Pixmap(Gdx.files.internal("badlogic.jpg"))

    override fun loadPixmaps() {
        App.app.log.info("zip open, $numThreads threads")
        zippedImages.forEachIndexed() {i, it ->
            App.app.log.info("zipentry: $it")
                es.submit {
                    println("loading pixmap for page $i")
                    zipFile.getInputStream(it).buffered().use {
                        val bytes = it.readBytes()
                        try{
                            val pixmap = Pixmap(bytes, 0, bytes.size)
                            pages[i]=(Page(pixmap))
                        }catch (e: Exception){
                            println("EXCEPTION "+e.message)
                            e.printStackTrace()


                            pages[i]=(Page(errorPixmap))
                        }

                        Gdx.graphics.isContinuousRendering = true
                        Gdx.graphics.requestRendering()
                        //println("${bytes.size} $it")

                    }
                }
        }
            App.app.log.info("waiting for termination")
            es.shutdown()
            es.awaitTermination(999999, TimeUnit.SECONDS)
            zipFile.close()

        }

}