package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import junrar.Archive
import junrar.impl.FileVolumeManager
import java.io.ByteArrayOutputStream
import java.io.File

class RarComic(filename: String): Comic(filename) {
    override fun loadPixmaps(){
        println("rar")
        val rarFile = Archive(FileVolumeManager(File(filename)))
        rarFile.use {
            println("mainheader ${it.mainHeader}")



          //  val headers=ArrayList<FileHeader>()
          //  it.fileHeaders.forEach{headers.add(it) }

            val headers = it.fileHeaders.toTypedArray().
                    sortedBy { it.fileNameString }.
                    filter { imageRegex.matches(it.fileNameString) }


            headers.forEach{
                println("header ${it.fileNameString}")
                val os = ByteArrayOutputStream()
                rarFile.extractFile(it, os)

                println("makepixmap")
                val pixmap = Pixmap(os.toByteArray(), 0, os.size())
                pages.add(Page(pixmap))
                Gdx.graphics.isContinuousRendering = true
                Gdx.graphics.requestRendering()

                println("size ${os.size()}")

            }

        }




        println("done")
    }
}