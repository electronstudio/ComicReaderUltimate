package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import java.util.zip.ZipFile

class ZipComic(filename: String):Comic(filename) {

    override fun loadPages() {




        val zipFile = ZipFile(filename)
        println("zip open")
        zipFile.use {
            it.entries().toList().filter { imageRegex.matches(it.name) }.forEach {
                println("zip entry")
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
            }

        }
    }
}