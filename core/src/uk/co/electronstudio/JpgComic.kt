package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

class JpgComic(filename: String):Comic(filename) {
    override val pages= ArrayList<Page>()

    private val es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1)

    val file = File(filename)
    val dir = if(file.isDirectory) file else file.parentFile

    init {
        println("file $file")
        println("dir $dir")
        dir.listFiles().filter {  imageRegex.matches(it.name) }.forEach {
            pages.add(Page(null))
        }
    }

    override fun loadPixmaps() {






        dir.listFiles().filter {  imageRegex.matches(it.name) }.forEachIndexed() {i, it ->
            es.submit {
                val pixmap = Pixmap(FileHandle(it)) //FIXME works on desktop only!
                pages[i].pixmap = pixmap
                App.pleaseRender()
            }
        }


        es.shutdown()
        es.awaitTermination(60, TimeUnit.SECONDS)


    }
}