package uk.co.electronstudio

//import de.tomgrill.gdxdialogs.core.GDXDialogsSystem
//import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.logging.Level
import kotlin.concurrent.thread


/**
 * zooming is jerky - add momentum?
 * two cameras is redundant.  encapsulate them in our own camera class?  or just get rid of goalcam altogether
 * do people want to hold down zoom/cursors movement? certainly in single page view you only ever tap it.
 * FIXED? i vol 2 has an extra wide page, doesnt look good in continuous mode
 * FIXED? in single page, zoom out then back again changes position
 * priroty should be given to loading current page if its not the first one
 * double page non continuous should advance two pages, not one
 * remember page for all previously opened comics, not just last one?
 */

class ViewScreen(val app: App, var fileToLoad: String?, var currentPage: Int = 0) : ScreenAdapter(),
    InputProcessor {

    private var batch: SpriteBatch = SpriteBatch() //5000, createDefaultShaderGL3())
    private var realCam: OrthographicCamera = OrthographicCamera()
    private var goalCam: OrthographicCamera = OrthographicCamera()
    val font = BitmapFont()
    var textBatch = SpriteBatch()
    val fixedFont = BitmapFont()
    var lastError = "No file loaded"
    val help =
        """
| Function          | Keys        |
| ----------------  | ----        |
| Quit              | Q           |
| Open file         | O, L        |
| Menu              | Escape, Tab |
|                   |             |
| Scroll            | Cursor keys |
| First page        | Home        |
| Last page         | End         |
| Next page         | Page Down   |
| Prev page         | Page Up     |
| Next screen       | Space       |
|                   |             |
| Zoom in           | +           |
| Zoom out          | -           |
| Zoom to fit       | Z           |
| Zoom default      | R           |
|                   |             |
| Double pages      | D           |
| Bilinear filter   | B           |
| Continuous scroll | C           |
            """.trimIndent()

    init {
        fixedFont.setFixedWidthGlyphs("ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz ,+-")
    }

    //var dialogs = GDXDialogsSystem.install()

    private var comic: Comic? = null
    private var totalPageHeights = 0f

    private var scrollDown = false
    private var scrollUp = false
    private var scrollLeft = false
    private var scrollRight = false
    private var zoomIn = false
    private var zoomOut = false

    val config = app.config


    override fun resize(width: Int, height: Int) {
        app.log.info("resize $width $height")
        textBatch = SpriteBatch()
        super.resize(width, height)

    }

    override fun show() {
        app.log.info("viewscreen show")
        Gdx.input.inputProcessor = this
        Gdx.input.isCursorCatched = true


        //font = BitmapFont()

        batch = SpriteBatch()


        val w = Gdx.graphics.displayMode.width.toFloat()
        val h = Gdx.graphics.displayMode.height.toFloat()

        app.log.info("Setting up camera " + w + " " + h)

        if (realCam.viewportWidth != w || realCam.viewportHeight != h) {
            realCam.setToOrtho(true, w, h)
        }
        if (goalCam.viewportWidth != w || goalCam.viewportHeight != h) {
            goalCam.setToOrtho(true, w, h)
        }

        //goalCam.zoom = 1f

        realCam.update()
        goalCam.update()
        Gdx.input.inputProcessor = this

        app.log.info("realcam position ${realCam.position.x} ${realCam.position.y}")
    }


    fun requestFile() {
        app.setScreen(this)
        val conf = NativeFileChooserConfiguration()

        comic = null

        //  conf.directory = Gdx.files.absolute(System.getProperty("user.home"))

        conf.title = "Choose cbr/cbz/jpg"


        app.fileChooser.chooseFile(conf, object : NativeFileChooserCallback {
            override fun onFileChosen(file: FileHandle) {
                loadComic(file.path())
                currentPage = 0
            }

            override fun onCancellation() {

            }

            override fun onError(exception: Exception) {
                throw exception
            }
        })
    }


    fun loadComic(filename: String) {
        try {
            app.log.info("loadcomic $filename")

            val c = Comic.factory(filename)

            thread(start = true) {
                app.log.info("starting pixmap load")
                var time = System.nanoTime()
                c.loadPixmaps()
                val x = ((System.nanoTime() - time) / 1000000f).toInt()
                app.log.info("loaded all pixmaps from archive in $x ms")
                totalPageHeights = calculateTotalPageHeights(c)
            }

            comic?.dispose()
            comic = c

            moveCameraToStartPosition()
            config.prefs.putString("lastFile", filename)
            config.prefs.flush()
        } catch (e: Throwable) {
            app.log.log(Level.SEVERE, "error loading comic", e)
            e.printStackTrace()
            lastError =
                e.message + "\n\n" + e.stackTrace.joinToString(separator = "\n") { it.toString() }
            //val errorScreen = ErrorScreen(app, e.message, e.stackTrace.joinToString(separator = "\n") { it.toString() } )
            //app.setScreen(errorScreen)
//            val bDialog = dialogs.newDialog(GDXButtonDialog::class.java)
//            bDialog.setTitle(e.message)
//            bDialog.setMessage(e.stackTrace.joinToString(separator = "\n") { it.toString() })
//
//            bDialog.setClickListener {
//                bDialog.dismiss()
//                Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
//                Gdx.input.isCursorCatched = true
//            }
//
//            bDialog.addButton("OK")
//            Gdx.input.isCursorCatched = false
//            Gdx.graphics.setWindowedMode(10, 10)
//
//            bDialog.build().show()
        }
    }

    private fun calculateTotalPageHeights(c: Comic): Float {
        return c.pages.map { it.height() }.sum()
    }

    var loadCompleted = false
    var initialZoomToFitDone = false

    override fun render(delta: Float) {
        app.log.info("currentPage $currentPage total pages ${comic?.pages?.size}")
        val f = fileToLoad
        if (f != null) {
            fileToLoad = null
            loadComic(f)

        }
        Gdx.graphics.isContinuousRendering = false
        //if (comic == null) app.setScreen(MenuScreen(app))
        comic?.let {
            if (config.previews) {
                it.loadPreviewTexturesFromPixmaps()
                if (it.allPreviewsAreLoaded()) {
                    it.loadUnloadedTexturesFromPixmaps()
                }
            } else {
                it.loadUnloadedTexturesFromPixmaps()
            }
            currentPage = MathUtils.clamp(currentPage, 0, it.pages.lastIndex)
        }
        processKeyEvents()
        moveRealCamTowardsGoalCam()
        constrainScrolling()
        if (comic?.allPreviewsAreLoaded() == true && !loadCompleted) {
            loadCompleted = true
            scrollToCurrentPageIfNecessary()
        }
        if (comic?.pages?.get(currentPage)?.previewTexture != null && !initialZoomToFitDone) {
            zoomToFit()
            initialZoomToFitDone = true
        }
        draw()
    }


    private fun constrainScrolling() {

        comic?.let {
            val pageWidth = it.pages[if (config.continuousScroll) 0 else currentPage].width()
            if (pageWidth == null) return
            val contentWidth =
                if (config.doublePage) pageWidth * 2 else pageWidth //fixme wrong if one page is wider than others
            if (contentWidth / realCam.zoom < Gdx.graphics.displayMode.width.toFloat()) {
                realCam.position.x = contentWidth / 2f
                goalCam.position.x = contentWidth / 2f
            } else {
                realCam.position.x = MathUtils.clamp(
                    realCam.position.x,
                    Gdx.graphics.displayMode.width * realCam.zoom / 2f,
                    contentWidth - (Gdx.graphics.displayMode.width / 2f) * realCam.zoom
                )
                goalCam.position.x = MathUtils.clamp(
                    goalCam.position.x,
                    Gdx.graphics.displayMode.width * goalCam.zoom / 2f,
                    contentWidth - (Gdx.graphics.displayMode.width / 2f) * goalCam.zoom
                )
            }
            realCam.position.y =
                Math.max(realCam.position.y, Gdx.graphics.displayMode.height * realCam.zoom / 2f)
            goalCam.position.y =
                Math.max(goalCam.position.y, Gdx.graphics.displayMode.height * goalCam.zoom / 2f)

            //add up all the page heights to find bottom scrollComic limit
            //fixme tidy
            //fixme work for double page mode
            if (config.continuousScroll && totalPageHeights > 0f) {
                realCam.position.y =
                    Math.min(
                        realCam.position.y,
                        totalPageHeights - (Gdx.graphics.displayMode.height / 2f) * realCam.zoom
                    )
                goalCam.position.y =
                    Math.min(
                        goalCam.position.y,
                        totalPageHeights - (Gdx.graphics.displayMode.height / 2f) * goalCam.zoom
                    )
            }


            if (!config.continuousScroll) {
                val pageHeight = it.pages[currentPage].height()
                if (pageHeight == null) return
                if (pageHeight / realCam.zoom < Gdx.graphics.displayMode.height.toFloat()) {
                    realCam.position.y = pageHeight / 2f
                    goalCam.position.y = pageHeight / 2f
                } else {
                    realCam.position.y =
                        Math.min(
                            realCam.position.y,
                            pageHeight - (Gdx.graphics.displayMode.height / 2f) * realCam.zoom
                        )
                    goalCam.position.y =
                        Math.min(
                            goalCam.position.y,
                            pageHeight - (Gdx.graphics.displayMode.height / 2f) * goalCam.zoom
                        )
                }
                // goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.displayMode.height*goalCam.zoom/2f)
            }

        }
    }


    private fun draw() {
        Gdx.gl.glClearColor(
            config.background.r,
            config.background.g,
            config.background.b,
            config.background.a
        )
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


        textBatch.begin()
        font.setColor(Color.WHITE)
        font.draw(textBatch, "${currentPage + 1}/${comic?.pages?.size}", 0f, 11f);
        textBatch.end()

        goalCam.update()
        realCam.update()
        batch.projectionMatrix = realCam.combined


        batch.begin()
        comic?.let {
            if (config.continuousScroll) {
                renderAll(it, batch, if (config.doublePage) 2 else 1)
            } else {
                renderSingle(it, batch)
            }
        }
        batch.end()


        textBatch.begin()
        if (config.showDebug) {

            Gdx.gl.glGetIntegerv(0x9049, buffer)
            val vram = (buffer[0] / 1024.0).toInt().toString() + "M"
            Gdx.gl.glGetIntegerv(0x9048, buffer)
            val vram2 = (buffer[0] / 1024.0).toInt().toString() + "M"

            val allocatedMemory =
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            val presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
            val ram = presumableFreeMemory / 1000000

            font.setColor(Color.RED)
            font.draw(
                textBatch,
                "page: ${currentPage + 1} total: ${comic?.pages?.size} loaded: ${comic?.loadedTextures} previews: ${comic?.loadedPreviews} camera: ${realCam.position.x.toInt()}  ${realCam.position.y.toInt()} screen: ${Gdx.graphics.displayMode.width} ${Gdx.graphics.displayMode.height} zoom: ${realCam.zoom.toInt()} VRAM: ${vram} / $vram2 RAM: $ram version $VERSION",
                0f,
                11f
            );

        }
        if (comic == null) {
            font.setColor(Color.WHITE)
            font.draw(textBatch, lastError, 10f, Gdx.graphics.displayMode.height.toFloat() / 2f)
            fixedFont.draw(
                textBatch,
                help,
                Gdx.graphics.displayMode.width / 2f,
                Gdx.graphics.displayMode.height.toFloat() - 100f
            );
        }
        textBatch.end()


    }

    val buffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer()

    fun renderSingle(comic: Comic, batch: SpriteBatch) {
        val page = comic.pages.get(currentPage)
        val tex = page.texture ?: page.previewTexture
        tex?.let {
            tex.texture?.setFilter(comic.filter, comic.filter)
            batch.draw(it, 0f, 0f, page.width(), page.height())
        }
        if (config.doublePage && currentPage < comic.pages.lastIndex) {
            val page2 = comic.pages.get(currentPage + 1)
            val tex2 = page2.texture ?: page2.previewTexture
            tex2?.let {
                tex2.texture?.setFilter(comic.filter, comic.filter)
                batch.draw(it, page.width(), 0f, page2.width(), page2.height())
            }
        }
    }

    fun renderAll(comic: Comic, batch: SpriteBatch, cols: Int) {
        var x = 0f
        var y = 0f
        var col = 0
        var c = 0
        comic.pages.forEach { page: Page ->
            val pixmap = page.pixmap
            if (pixmap == null) return@forEach
            val tex = if (realCam.zoom > 10f) page.previewTexture else (page.texture
                ?: page.previewTexture)
            if (tex == null) return@forEach
            tex.texture?.setFilter(comic.filter, comic.filter)
            if (realCam.frustum.sphereInFrustum(
                    x + pixmap.width / 2,
                    y + pixmap.height / 2,
                    0f,
                    pixmap.height.toFloat()
                )
            ) {
                val xOffset =
                    (comic.pages[0].width() - page.width()) / 2  //fixme: this is wrong for doulble page
                //fixme: also the contraints are based on first page not current page
                batch.draw(
                    tex,
                    x + xOffset,
                    y,
                    page.width(),
                    page.height()
                ) //pixmap.width.toFloat(), pixmap.height.toFloat())
            }
            x += pixmap.width
            col++
            if (col == cols) {
                col = 0
                x = 0f
                y += pixmap.height
            }
        }

    }

    private fun processKeyEvents() {
        if (scrollLeft || scrollUp || scrollDown || scrollRight || zoomOut || zoomIn) {
            App.pleaseRender()
        }

        val xd: Float =
            if (scrollLeft) -config.scrollSpeed else if (scrollRight) config.scrollSpeed else 0f
        val yd: Float =
            if (scrollUp) -config.scrollSpeed else if (scrollDown) config.scrollSpeed else 0f
        val zd: Float =
            if (zoomIn) 1 - config.zoomSpeed else if (zoomOut) 1 + config.zoomSpeed else 1f

        realCam.translate(xd, yd)
        goalCam.translate(xd, yd)

        realCam.zoom = realCam.zoom * zd
        goalCam.zoom = goalCam.zoom * zd
    }


    private fun moveRealCamTowardsGoalCam() {
        if (goalCam.zoom < realCam.zoom) {
            realCam.zoom *= (1 - config.zoomSpeed)
            if (goalCam.zoom > realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        } else if (goalCam.zoom > realCam.zoom) {
            realCam.zoom *= (1 + config.zoomSpeed)
            if (goalCam.zoom < realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        }

        if (goalCam.position.y < realCam.position.y) {
            realCam.position.y -= config.scrollSpeed
            if (goalCam.position.y > realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        } else if (goalCam.position.y > realCam.position.y) {
            realCam.position.y += config.scrollSpeed
            if (goalCam.position.y < realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        }
        if (config.continuousScroll && loadCompleted) {
            currentPage = convertScrollAmountToPageNumber(realCam.position.y)


            //  currentPage=(realCam.position.y /
            //   ((comic?.pages?.get(currentPage)?.height() ?: 0f) - (Gdx.graphics.displayMode.height / 2f) * realCam.zoom)
            //           ).toInt()
        }
    }

    private fun convertScrollAmountToPageNumber(scroll: Float): Int {
        var y = (Gdx.graphics.displayMode.height / 2f) * goalCam.zoom
        var i = 0
        comic?.let {
            for (page in it.pages) {
                y = y + page.height()
                if (y > scroll) break
                i++
            }
        }
        return i
    }

    private fun convertPageNumberToScrollAmount(n: Int): Float {
        var h = (Gdx.graphics.displayMode.height / 2f) * goalCam.zoom
        for (i in 0 until n) {
            comic?.let {
                h = h + it.pages[i].height()
            }
        }
        return h
    }

    override fun dispose() {
        batch.dispose()
        comic?.dispose()
    }

    val mouseHistoryX = ArrayList<Float>()
    val mouseHistoryY = ArrayList<Float>()

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val mx = Gdx.input.deltaX.toDouble()
        val my = Gdx.input.deltaY.toDouble()
        if (config.useMouseMoveForZoom) {
            zoom(my.toInt())
        } else {
            scrollComic(mx, my)
        }
        return true
    }

    private fun scrollComic(mx: Double, my: Double) {
        val x = MathUtils.clamp(
            (Math.pow(
                Math.abs(mx),
                config.mouseAcceleration.toDouble()
            ) * realCam.zoom * config.mouseSens).toFloat() * if (mx > 0f) 1f else -1f,
            -500f,
            500f
        )
        //MathUtils.clamp((Math.pow(mx, mouseAcceleration.toDouble()) * realCam.zoom * mouseSens).toFloat(), -5f, 5f)
        var y = MathUtils.clamp(
            (Math.pow(
                Math.abs(my),
                config.mouseAcceleration.toDouble()
            ) * realCam.zoom * config.mouseSens).toFloat() * if (my > 0f) 1f else -1f,
            -500f,
            500f
        )

        if (config.reverseScroll) y = -y


        // * (if (my<0) 1 else -1)

        //        val linearity=1f
        //        val x = ((1f/(0+linearity)) * mx * (Math.abs(mx)*linearity)).toFloat();
        //        val y = ((1f/(0+linearity)) * my * (Math.abs(my)*linearity)).toFloat();

        if (config.mouseSmoothing) {  //fixme should this apply to zooming too?
            app.log.info("mouse moved y $y")
            mouseHistoryX.add(x)
            mouseHistoryY.add(y)
            if (mouseHistoryX.size > 10) mouseHistoryX.removeAt(0)
            if (mouseHistoryY.size > 10) mouseHistoryY.removeAt(0)
            val averageX = mouseHistoryX.average().toFloat()
            val averageY = mouseHistoryY.average().toFloat()
            realCam.translate(averageX, averageY)
            goalCam.translate(averageX, averageY)
        } else {
            realCam.translate(x, y)
            goalCam.translate(x, y)
        }
        App.pleaseRender()
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        app.log.info("scrolled $amount")
        if (config.useMouseWheelForZoom) {
            zoom(amount)
        } else {
            scrollComic(0.0, amount.toDouble() * 4.0)
        }


        return true

    }

    fun zoom(amount: Int) {
        if (amount > 0) {
            goalCam.zoom *= config.zoomSens
        } else {
            goalCam.zoom /= config.zoomSens
        }
        App.pleaseRender()
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.DOWN -> scrollDown = false
            Input.Keys.UP -> scrollUp = false
            Input.Keys.LEFT -> scrollLeft = false
            Input.Keys.RIGHT -> scrollRight = false
            Input.Keys.EQUALS -> zoomIn = false
            Input.Keys.MINUS -> zoomOut = false
        }
        App.pleaseRender()


        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        App.pleaseRender()
        return true
    }


    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.TAB -> {
                app.setScreen(MenuScreen(app))
            }
            Input.Keys.Q -> {
                quit()
            }
            Input.Keys.DOWN -> scrollDown = true
            Input.Keys.UP -> scrollUp = true
            Input.Keys.LEFT -> scrollLeft = true
            Input.Keys.RIGHT -> scrollRight = true
            Input.Keys.EQUALS -> zoomIn = true
            Input.Keys.MINUS -> zoomOut = true
            Input.Keys.D -> config.doublePage = !config.doublePage
            Input.Keys.PAGE_DOWN -> nextPage()
            Input.Keys.PAGE_UP -> prevPage()
            Input.Keys.L, Input.Keys.O -> requestFile()
            Input.Keys.SPACE -> advance()
            Input.Keys.B -> comic?.swapFilter()
            Input.Keys.C -> {
                config.continuousScroll = !config.continuousScroll
                scrollToCurrentPageIfNecessary()
            }
            Input.Keys.Z -> zoomToFit()
            Input.Keys.R -> oneOneZoom()
            Input.Keys.HOME -> firstPage()
            Input.Keys.END -> lastPage()
        }
        App.pleaseRender()
        return true
    }

    private fun scrollToCurrentPageIfNecessary() {
        if (config.continuousScroll) {
            goalCam.position.y = convertPageNumberToScrollAmount(currentPage)
            realCam.position.y = goalCam.position.y
            app.log.info("scrollToCurrentPageIfNecessary $currentPage ${realCam.position.y}")
        }
    }

    fun quit() {
        config.prefs.putInteger("currentPage", currentPage)
        config.savePrefs()
        Gdx.app.exit()
        System.exit(0)
    }

    var oldZoom = 1f
    var oldX = 0f
    var oldY = 0f

    private fun zoomToFit() {
        oldZoom = goalCam.zoom
        oldX = goalCam.position.x
        oldY = goalCam.position.y
        comic?.let {
            goalCam.zoom = it.pages[currentPage].height() / Gdx.graphics.displayMode.height
        }
    }

    private fun restoreOldZoom() {
        goalCam.zoom = oldZoom
        goalCam.position.x = oldX
        goalCam.position.y = oldY
    }

    private fun oneOneZoom() {
        goalCam.zoom = 1f
    }

    fun firstPage() {
        currentPage = 0
        moveCameraToStartPosition()
    }

    fun lastPage() {
        comic?.let {
            currentPage = it.pages.lastIndex
        }
        moveCameraToStartPosition()
    }

    fun nextPage() {
        comic?.let {
            if (it.pages.lastIndex == currentPage && config.quitAtEnd) {
                config.prefs.remove("lastFile")
                quit()
            }
        }
        if (config.continuousScroll) {
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height() ?: 0f) * 1f)
        } else {
            currentPage = MathUtils.clamp(currentPage + 1, 0, comic?.pages?.lastIndex ?: 0)
            moveCameraToStartPosition()
        }
    }

    fun prevPage() {
        if (config.continuousScroll) {
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height() ?: 0f) * -1f)
        } else {
            currentPage = MathUtils.clamp(currentPage - 1, 0, comic?.pages?.lastIndex ?: 0)
            moveCameraToStartPosition()
        }
    }

    fun moveCameraToStartPosition() {
        goalCam.position.x = (Gdx.graphics.displayMode.width / 2f) * goalCam.zoom
        realCam.position.x = (Gdx.graphics.displayMode.width / 2f) * realCam.zoom
        goalCam.position.y = (Gdx.graphics.displayMode.height / 2f) * goalCam.zoom
        realCam.position.y = (Gdx.graphics.displayMode.height / 2f) * realCam.zoom
    }

    fun advance() {
        if (!config.continuousScroll && goalCam.position.y >= ((comic?.pages?.get(currentPage)
                ?.height()
                ?: 0f) - (Gdx.graphics.displayMode.height / 2f) * realCam.zoom) - 1
        ) {
            nextPage()
        } else {
            goalCam.translate(
                0f,
                (comic?.pages?.get(currentPage)?.height() ?: 0f) * config.spaceBarAdvanceAmount
            ) //goalCam.zoom)
        }
        App.pleaseRender()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            0 -> advance()
            1 -> zoomToFit()
        }
        App.pleaseRender()
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            1 -> restoreOldZoom()
        }
        App.pleaseRender()
        return true
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

}