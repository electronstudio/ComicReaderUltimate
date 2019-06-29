package uk.co.electronstudio

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
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration
import kotlin.concurrent.thread
import de.tomgrill.gdxdialogs.core.GDXDialogsSystem
import de.tomgrill.gdxdialogs.core.GDXDialogs
import de.tomgrill.gdxdialogs.core.listener.ButtonClickListener
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import java.util.logging.Level


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

class ViewScreen(val app: App, fileToLoad: String?, var currentPage: Int=0) : ScreenAdapter(), InputProcessor {

    private var batch: SpriteBatch = SpriteBatch() //5000, createDefaultShaderGL3())
    private var realCam: OrthographicCamera = OrthographicCamera()
    private var goalCam: OrthographicCamera = OrthographicCamera()
    val font =  BitmapFont()
    val textBatch = SpriteBatch()
    var prefs = Gdx.app.getPreferences("uk.co.electronstudio.comicreaderultimate")

    var dialogs = GDXDialogsSystem.install()

    private var comic: Comic? = null
    private var totalPageHeights = 0f

    private var scrollDown = false
    private var scrollUp = false
    private var scrollLeft = false
    private var scrollRight = false
    private var zoomIn = false
    private var zoomOut = false

    private var doublePage = false
    private var continuousScroll = false
    private val background: Color = Color.BLACK
    private val zoomSpeed = 0.1f // 0.01 - 0.10
    private val scrollSpeed = 60f
    private val zoomSens = 1.1f
    private val mouseSens = 5f
    private val mouseSmoothing = false
    private val quitAtEnd = true
    private val spaceBarAdvanceAmount = 0.5f
    private val mouseAcceleration = 1.1f // 1 to 1.99


    init {
        //if passed comic, attempt to load it
        //else attempt to load previous comic
        if (fileToLoad != null) {
            loadComic(fileToLoad)
        } else {
            requestFile()
        }
    }

    override fun resize(width: Int, height: Int) {
        app.log.info("resize")
        super.resize(width, height)
        realCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        goalCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        goalCam.zoom = 1f

        realCam.update()
        goalCam.update()
        Gdx.input.inputProcessor = this

        app.log.info("CAM X "+realCam.position.x)
        app.log.info("CAM Y "+realCam.position.y)
    }

    override fun show() {
        app.log.info("show")
        super.show()
        Gdx.input.inputProcessor = this
    }


    private fun requestFile() {
        val conf = NativeFileChooserConfiguration()

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
            prefs.putString("lastFile", filename)
            prefs.flush()
        }catch (e: Throwable){
            app.log.log(Level.SEVERE, "error loading comic", e)
            e.printStackTrace()
            val bDialog = dialogs.newDialog(GDXButtonDialog::class.java)
            bDialog.setTitle(e.message)
            bDialog.setMessage(e.stackTrace.joinToString(separator = "\n") {it.toString()})

            bDialog.setClickListener {
                bDialog.dismiss()
                Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
                Gdx.input.isCursorCatched = true
            }

            bDialog.addButton("OK")
            Gdx.input.isCursorCatched = false
            Gdx.graphics.setWindowedMode(10, 10)

            bDialog.build().show()
        }
    }

    private fun calculateTotalPageHeights(c: Comic): Float {
        return c.pages.map { it.height() }.sum()
    }

    override fun render(delta: Float) {
      //  app.log.info("render")
        Gdx.graphics.isContinuousRendering = false
        comic?.let {
            it.loadPreviewTexturesFromPixmaps()
            it.loadUnloadedTexturesFromPixmaps()
            currentPage=MathUtils.clamp(currentPage, 0, it.pages.lastIndex)
        }
        processKeyEvents()
        moveRealCamTowardsGoalCam()
        constrainScrolling()
        draw()
    }



    private fun constrainScrolling(){

        comic?.let {
            val pageWidth = it.pages[if(continuousScroll) 0 else currentPage].width()
            if(pageWidth==null) return
            val contentWidth = if(doublePage) pageWidth*2 else pageWidth //fixme wrong if one page is wider than others
            if(contentWidth/realCam.zoom < Gdx.graphics.width.toFloat()){
                realCam.position.x=contentWidth/2f
                goalCam.position.x=contentWidth/2f
            }else{
                realCam.position.x= MathUtils.clamp(  realCam.position.x, Gdx.graphics.width*realCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*realCam.zoom)
                goalCam.position.x= MathUtils.clamp(  goalCam.position.x, Gdx.graphics.width*goalCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*goalCam.zoom)
            }
            realCam.position.y=Math.max( realCam.position.y, Gdx.graphics.height*realCam.zoom/2f)
            goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.height*goalCam.zoom/2f)

            //add up all the page heights to find bottom scroll limit
            //fixme tidy
            //fixme work for double page mode
            if(continuousScroll && totalPageHeights>0f) {
                realCam.position.y =
                    Math.min(realCam.position.y, totalPageHeights - (Gdx.graphics.height / 2f) * realCam.zoom)
                goalCam.position.y =
                    Math.min(goalCam.position.y, totalPageHeights - (Gdx.graphics.height / 2f) * goalCam.zoom)
            }


            if(!continuousScroll){
                val pageHeight = it.pages[currentPage].height()
                if(pageHeight==null) return
                if(pageHeight/realCam.zoom < Gdx.graphics.height.toFloat()){
                    realCam.position.y=pageHeight/2f
                    goalCam.position.y=pageHeight/2f
                }else {
                    realCam.position.y =
                            Math.min(realCam.position.y, pageHeight - (Gdx.graphics.height / 2f) * realCam.zoom)
                    goalCam.position.y =
                            Math.min(goalCam.position.y, pageHeight - (Gdx.graphics.height / 2f) * goalCam.zoom)
                }
               // goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.height*goalCam.zoom/2f)
            }

        }
    }




    private fun draw() {
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


        textBatch.begin()
        font.draw(textBatch, "${currentPage+1}/${comic?.pages?.size}", 0f, 11f);
        textBatch.end()

        goalCam.update()
        realCam.update()
        batch.projectionMatrix = realCam.combined


        batch.begin()
        comic?.let {
            if(continuousScroll){
                renderAll(it, batch, if(doublePage) 2 else 1)
            }else{
               renderSingle(it, batch)
            }
        }
        batch.end()


        textBatch.begin()
        font.setColor(Color.RED)
        font.draw(textBatch, "${currentPage+1}/${comic?.pages?.size} ${realCam.position.y} ${realCam.zoom}", 0f, 11f);
        font.setColor(Color.WHITE)
        textBatch.end()


    }



    fun renderSingle(comic: Comic, batch: SpriteBatch) {
        val page=comic.pages.get(currentPage)
        val tex = page.texture ?: page.previewTexture
        tex?.let {
            tex.texture?.setFilter(comic.filter, comic.filter)
            batch.draw(it, 0f, 0f, page.width(), page.height())
        }
        if(doublePage && currentPage<comic.pages.lastIndex){
            val page2=comic.pages.get(currentPage+1)
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
            val tex = if (realCam.zoom > 10f) page.previewTexture else (page.texture ?: page.previewTexture)
            if (tex == null) return@forEach
            tex.texture?.setFilter(comic.filter, comic.filter)
            if (realCam.frustum.sphereInFrustum(x + pixmap.width / 2,
                    y + pixmap.height / 2,
                    0f,
                    pixmap.height.toFloat())
            ) {
                val xOffset = (comic.pages[0].width()-page.width())/2  //fixme: this is wrong for doulble page
                                                                        //fixme: also the contraints are based on first page not current page
                batch.draw(tex, x+xOffset, y, page.width(), page.height())//pixmap.width.toFloat(), pixmap.height.toFloat())
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

        val xd: Float = if (scrollLeft) -scrollSpeed else if (scrollRight) scrollSpeed else 0f
        val yd: Float = if (scrollUp) -scrollSpeed else if (scrollDown) scrollSpeed else 0f
        val zd: Float = if (zoomIn) 1 - zoomSpeed else if (zoomOut) 1 + zoomSpeed else 1f

        realCam.translate(xd, yd)
        goalCam.translate(xd, yd)

        realCam.zoom = realCam.zoom * zd
        goalCam.zoom = goalCam.zoom * zd
    }


    private fun moveRealCamTowardsGoalCam() {
        if (goalCam.zoom < realCam.zoom) {
            realCam.zoom *= (1 - zoomSpeed)
            if (goalCam.zoom > realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        } else if (goalCam.zoom > realCam.zoom) {
            realCam.zoom *= (1 + zoomSpeed)
            if (goalCam.zoom < realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        }

        if (goalCam.position.y < realCam.position.y) {
            realCam.position.y -= scrollSpeed
            if (goalCam.position.y > realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        } else if (goalCam.position.y > realCam.position.y) {
            realCam.position.y += scrollSpeed
            if (goalCam.position.y < realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        }
        if(continuousScroll){
            currentPage=convertScrollAmountToPageNumber(realCam.position.y)


          //  currentPage=(realCam.position.y /
         //   ((comic?.pages?.get(currentPage)?.height() ?: 0f) - (Gdx.graphics.height / 2f) * realCam.zoom)
         //           ).toInt()
        }
    }

    private fun convertScrollAmountToPageNumber(scroll: Float): Int {
        var y = (Gdx.graphics.height/2f) * goalCam.zoom
        var i=0
        comic?.let {
            for(page in it.pages){
                y=y+page.height()
                if(y>scroll) break
                i++
            }
        }
        return i
    }

    private fun convertPageNumberToScrollAmount(n: Int): Float {
        var h=(Gdx.graphics.height/2f) * goalCam.zoom
        for(i in 0 until n){
            comic?.let {
                h=h+it.pages[i].height()
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
        val x = MathUtils.clamp((Math.pow(Math.abs(mx), mouseAcceleration.toDouble()) * realCam.zoom * mouseSens).toFloat() * if(mx>0f) 1f else -1f, -500f, 500f)
        //MathUtils.clamp((Math.pow(mx, mouseAcceleration.toDouble()) * realCam.zoom * mouseSens).toFloat(), -5f, 5f)
        val y = MathUtils.clamp((Math.pow(Math.abs(my), mouseAcceleration.toDouble()) * realCam.zoom * mouseSens).toFloat() * if(my>0f) 1f else -1f, -500f, 500f)


        // * (if (my<0) 1 else -1)

//        val linearity=1f
//        val x = ((1f/(0+linearity)) * mx * (Math.abs(mx)*linearity)).toFloat();
//        val y = ((1f/(0+linearity)) * my * (Math.abs(my)*linearity)).toFloat();

        if(mouseSmoothing) {
            app.log.info("mouse moved y $y")
            mouseHistoryX.add(x)
            mouseHistoryY.add(y)
            if(mouseHistoryX.size>10) mouseHistoryX.removeAt(0)
            if(mouseHistoryY.size>10) mouseHistoryY.removeAt(0)
            val averageX = mouseHistoryX.average().toFloat()
            val averageY = mouseHistoryY.average().toFloat()
            realCam.translate(averageX, averageY)
            goalCam.translate(averageX, averageY)
        }else{
            realCam.translate(x, y)
            goalCam.translate(x, y)
        }
        App.pleaseRender()
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        app.log.info("scrolled $amount")

        if (amount > 0) {
            goalCam.zoom *= zoomSens
        } else {
            goalCam.zoom /= zoomSens
        }
        App.pleaseRender()
        return true

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
            Input.Keys.ESCAPE -> {
                quit()
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
            Input.Keys.D -> doublePage = !doublePage
            Input.Keys.PAGE_DOWN -> nextPage()
            Input.Keys.PAGE_UP -> prevPage()
            Input.Keys.O -> requestFile()
            Input.Keys.SPACE -> advance()
            Input.Keys.B -> comic?.swapFilter()
            Input.Keys.C -> {
                continuousScroll = !continuousScroll
                if(continuousScroll){
                    goalCam.position.y=convertPageNumberToScrollAmount(currentPage)
                    realCam.position.y=goalCam.position.y
                }
            }
            Input.Keys.Z -> zoomToFit()
            Input.Keys.R -> oneOneZoom()
            Input.Keys.HOME -> firstPage()
            Input.Keys.END -> lastPage()
        }
        App.pleaseRender()
        return true
    }

    private fun quit() {
        prefs.putInteger("currentPage", currentPage)
        prefs.flush()
        System.exit(0)
    }

    var oldZoom=1f
    var oldX=0f
    var oldY=0f

    private fun zoomToFit() {
        oldZoom=goalCam.zoom
        oldX=goalCam.position.x
        oldY=goalCam.position.y
        comic?.let {
            goalCam.zoom = it.pages[currentPage].height()/Gdx.graphics.height
        }
    }

    private fun restoreOldZoom(){
        goalCam.zoom = oldZoom
        goalCam.position.x=oldX
        goalCam.position.y=oldY
    }

    private fun oneOneZoom() {
        goalCam.zoom=1f
    }

    fun firstPage(){
        currentPage=0
        moveCameraToStartPosition()
    }

    fun lastPage(){
        comic?.let {
            currentPage=it.pages.lastIndex
        }
        moveCameraToStartPosition()
    }

    fun nextPage(){
        comic?.let {
            if(it.pages.lastIndex == currentPage && quitAtEnd){
                prefs.remove("lastFile")
                quit()
            }
        }
        if(continuousScroll){
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height() ?: 0f) * 1f )
        }else {
            currentPage = MathUtils.clamp(currentPage + 1, 0, comic?.pages?.lastIndex ?: 0)
            moveCameraToStartPosition()
        }
    }
    fun prevPage(){
        if(continuousScroll){
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height() ?: 0f) * -1f )
        }else {
            currentPage = MathUtils.clamp(currentPage - 1, 0, comic?.pages?.lastIndex ?: 0)
            moveCameraToStartPosition()
        }
    }

    fun moveCameraToStartPosition(){
        goalCam.position.x=(Gdx.graphics.width/2f)*goalCam.zoom
        realCam.position.x=(Gdx.graphics.width/2f)*realCam.zoom
        goalCam.position.y=(Gdx.graphics.height/2f)*goalCam.zoom
        realCam.position.y=(Gdx.graphics.height/2f)*realCam.zoom
    }

    fun advance(){
        if(!continuousScroll && goalCam.position.y >= ((comic?.pages?.get(currentPage)?.height() ?: 0f) - (Gdx.graphics.height / 2f) * realCam.zoom)-1){
            nextPage()
        }else{
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height() ?: 0f) * spaceBarAdvanceAmount )//goalCam.zoom)
        }
        App.pleaseRender()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when(button){
            0->advance()
            1->zoomToFit()
        }
        App.pleaseRender()
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when(button){
            1->restoreOldZoom()
        }
        App.pleaseRender()
        return true
    }




}