package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.concurrent.thread

class ViewScreen(val app: App): ScreenAdapter(), InputProcessor {


    private var batch: SpriteBatch = SpriteBatch()

    private val zoomSens = 1.1f
    private val mouseSens = 2f

    private var scrollDown = false
    private var scrollUp = false
    private var scrollLeft = false
    private var scrollRight = false
    private var zoomIn = false
    private var zoomOut = false

    private var realCam: OrthographicCamera = OrthographicCamera()
    private var goalCam: OrthographicCamera = OrthographicCamera()


    //var comic = Comic("/Volumes/Home/rich/test.cbz")
    private var comic = Comic.factory("/Volumes/Home/rich/test.cbz")


    private val cols = 1

    private val background: Color = Color.BLACK

    private val zoomSpeed = 0.04f // 0.01 - 0.10

    private val scrollSpeed = 20f


    init {

        thread(start = true) {
            println("starting")
            comic.loadPixmaps()
            println("loaded")
        }



        realCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        goalCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        //    Gdx.graphics.isContinuousRendering = false;
        //   Gdx.graphics.requestRendering();

        Gdx.input.inputProcessor = this

        realCam.update()
        goalCam.update()
    }


    override fun render(delta: Float) {
        Gdx.graphics.isContinuousRendering = false
        comic.loadUnloadedTexturesFromPixmaps()
        processKeyEvents()
        processMouseEvents()
        draw()
    }

    private fun draw() {
        goalCam.update()
        realCam.update()
        batch.projectionMatrix = realCam.combined
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        comic.render(batch, cols)
        batch.end()
    }

    private fun processKeyEvents() {
        if (scrollLeft || scrollUp || scrollDown || scrollRight || zoomOut || zoomIn) {
            Gdx.graphics.isContinuousRendering = true
        }

        val xd: Float = if (scrollLeft) -scrollSpeed else if (scrollRight) scrollSpeed else 0f
        val yd: Float = if (scrollUp) -scrollSpeed else if (scrollDown) -scrollSpeed else 0f
        val zd: Float = if (zoomIn) 1 - zoomSpeed else if (zoomOut) 1 + zoomSpeed else 1f

        realCam.translate(xd, yd)
        goalCam.translate(xd, yd)

        realCam.zoom = realCam.zoom * zd
        goalCam.zoom = goalCam.zoom * zd
    }


    private fun processMouseEvents() {
        if (goalCam.zoom < realCam.zoom) {
            realCam.zoom *= (1 - zoomSpeed)
            if (goalCam.zoom > realCam.zoom) realCam.zoom = goalCam.zoom
            Gdx.graphics.isContinuousRendering = true
        } else if (goalCam.zoom > realCam.zoom) {
            realCam.zoom *= (1 + zoomSpeed)
            if (goalCam.zoom < realCam.zoom) realCam.zoom = goalCam.zoom
            Gdx.graphics.isContinuousRendering = true
        }

        if (goalCam.position.y < realCam.position.y) {
            realCam.position.y -= scrollSpeed
            if (goalCam.position.y > realCam.position.y) realCam.position.y = goalCam.position.y
            Gdx.graphics.isContinuousRendering = true
        } else if (goalCam.position.y > realCam.position.y) {
            realCam.position.y += scrollSpeed
            if (goalCam.position.y < realCam.position.y) realCam.position.y = goalCam.position.y
            Gdx.graphics.isContinuousRendering = true
        }
    }
    override fun dispose() {
        batch.dispose()
        //   img.dispose()
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val x = Gdx.input.deltaX.toFloat() * realCam.zoom * mouseSens
        val y = Gdx.input.deltaY.toFloat() * realCam.zoom * mouseSens
        realCam.translate(x, y)
        goalCam.translate(x, y)
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        println(amount)

        if (amount > 0) {
            goalCam.zoom *= zoomSens
        } else {
            goalCam.zoom /= zoomSens
        }
        Gdx.graphics.isContinuousRendering = true
        return true

    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SPACE -> goalCam.translate(0f, 1000f)
            Input.Keys.B -> comic.swapFilter()
            Input.Keys.DOWN -> scrollDown = false
            Input.Keys.UP -> scrollUp = false
            Input.Keys.LEFT -> scrollLeft = false
            Input.Keys.RIGHT -> scrollRight = false
            Input.Keys.EQUALS -> zoomIn = false
            Input.Keys.MINUS -> zoomOut = false
        }
        Gdx.graphics.isContinuousRendering = true


        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return true
    }


    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> {
                System.exit(0)
            }
            Input.Keys.Q -> {
                System.exit(0)
            }
            Input.Keys.DOWN -> scrollDown = true
            Input.Keys.UP -> scrollUp = true
            Input.Keys.LEFT -> scrollLeft = true
            Input.Keys.RIGHT -> scrollRight = true
            Input.Keys.EQUALS -> zoomIn = true
            Input.Keys.MINUS -> zoomOut = true
        }
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return true
    }





}