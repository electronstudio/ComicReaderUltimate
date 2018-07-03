package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import junrar.Archive
import junrar.impl.FileVolumeManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RarComicThreaded(filename: String): Comic(filename) {

    private val es = Executors.newFixedThreadPool(2)

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
             //  Thread.sleep(1000)
                es.submit{

                    val rarFile2 = Archive(FileVolumeManager(File(filename)))
                    println("header ${it.fileNameString}")
                    val os = ByteArrayOutputStream()
                    rarFile.extractFile(it, os)


                    val pixmap = Pixmap(os.toByteArray(), 0, os.size())
                    pages.add(Page(pixmap))

                    Gdx.graphics.isContinuousRendering = true
                    Gdx.graphics.requestRendering()
                }




              //  println("size ${os.size()}")

            }
            es.shutdown()
            es.awaitTermination(60, TimeUnit.SECONDS)

        }





    }
}