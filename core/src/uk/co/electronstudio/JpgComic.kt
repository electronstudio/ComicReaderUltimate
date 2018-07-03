package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

class JpgComic(filename: String):Comic(filename) {

    private val es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1)

    val file = File(filename)
    val dir = file.parentFile

    override fun loadPixmaps() {






        dir.listFiles().filter {  imageRegex.matches(it.name) }.forEach {
            es.submit {
                val pixmap = Pixmap(FileHandle(it)) //FIXME works on desktop only!
                pages.add(Page(pixmap))
                App.pleaseRender()
            }
        }


        es.shutdown()
        es.awaitTermination(60, TimeUnit.SECONDS)


    }
}