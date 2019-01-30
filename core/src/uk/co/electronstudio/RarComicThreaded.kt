package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import junrar.Archive
import junrar.impl.FileVolumeManager
import junrar.rarfile.FileHeader
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.concurrent.ConcurrentLinkedQueue

class RarComicThreaded(filename: String) : Comic(filename) {

    val numThreads = Runtime.getRuntime().availableProcessors() - 1
    val queue = ConcurrentLinkedQueue<Int>()


    override fun loadPixmaps() {
        println("rar")
        printFreeMemory()

        val rarFile = Archive(FileVolumeManager(File(filename)))




        println("mainheader ${rarFile.mainHeader}")


        //  val headers=ArrayList<FileHeader>()
        //  it.fileHeaders.forEach{headers.add(it) }

        val headers = getRarHeaders(rarFile)
        println("headers: ${headers.size}")


        for (i in 0..headers.lastIndex) {
            queue.add(i)
        }

        val threads: Array<Thread> = Array(numThreads) {
            Thread(RarWorker(this))
        }

        threads.forEach { it.start() }

        threads.forEach { it.join() }

        printFreeMemory()

    }


    fun getRarHeaders(archive: Archive): List<FileHeader> {
        return archive.fileHeaders.toTypedArray().sortedBy { it.fileNameString }
            .filter { imageRegex.matches(it.fileNameString) }
    }

    fun printFreeMemory(){
        val allocatedMemory      = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        val presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
        println("USED RAM: ${allocatedMemory/1000000} FREE RAM: ${presumableFreeMemory/1000000}")
    }
}

class RarWorker(val comic: RarComicThreaded) : Runnable {

    override fun run() {

        val rarFile = Archive(FileVolumeManager(File(comic.filename)))

        println("mainheader ${rarFile.mainHeader}")

        val headers = comic.getRarHeaders(rarFile)

        println("rarFile.headers.size ${rarFile.headers.size}")

        var i = comic.queue.poll()
        while (i != null) {

            println("header number $i of ${headers.size}")
            val os = ByteArrayOutputStream()

            try {
                rarFile.extractFile(headers[i], os)
            } catch (e: Exception) {
                println("exception $e")
            }
            val extracted = os.toByteArray()
            //   println("extracted ${extracted.size}")
            val pixmap = Pixmap(extracted, 0, os.size())
            comic.pages.add(Page(pixmap))

            Gdx.graphics.isContinuousRendering = true
            Gdx.graphics.requestRendering()
            i = comic.queue.poll()
        }
    }
}

