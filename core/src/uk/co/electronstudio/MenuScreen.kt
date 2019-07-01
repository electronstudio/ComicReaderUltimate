package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.utils.Array
import ktx.scene2d.*
import ktx.actors.*
import net.dermetfan.gdx.scenes.scene2d.ui.FileChooser
import net.dermetfan.gdx.scenes.scene2d.ui.ListFileChooser
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration
import com.badlogic.gdx.scenes.scene2d.InputListener





class MenuScreen(val app: App): ScreenAdapter() {
    val config = app.config
    private var stage: Stage? = null
    private var table: Table? = null

    init {
        app.log.info("menuscreen init")
        stage = Stage()


        val s = Skin(Gdx.files.internal("skin/uiskin.json"))

        val nameLabel = Label("Name:", s)

        Scene2DSkin.defaultSkin = s

        val chooser = ListFileChooser(s,"default",  object: FileChooser.Listener{
            override fun choose(file: FileHandle?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun choose(files: Array<FileHandle>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun cancel() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        table = table {
            button { cell ->
                // Changing button properties - button is "this":
                color = Color.CORAL
                // Changing cell properties:
                cell.fillX().row()
            }
            table {
                // Changing nested table properties:
                defaults().pad(2f)
                // Adding table children:
                label("Nested")
                label("table")
                label("of")
                label("labels.")
                // Cell of the nested actor is also available through "it":
                it.spaceBottom(10f).row()
            }
            textButton(text = "Click me!")
            // Packing the root window:
           // pack()
        }




        val window = window(title = "Settings") {
//            button { cell ->
//                // Changing button properties - button is "this":
//                color = Color.CORAL
//                // Changing cell properties:
//                cell.fillX().row()
//            }
//            table {
//                // Changing nested table properties:
//                defaults().pad(2f)
//                // Adding table children:
//                label("Nested")
//                label("table")
//                label("of")
//                label("labels.")
//                // Cell of the nested actor is also available through "it":
//                it.spaceBottom(10f).row()
//            }
            textButton(text = "TOGGLE MENU (ESC)").onClick {
                app.setScreen(app.viewScreen)
            }

            textButton(text = "LOAD FILE (L)").onClick {
                app.viewScreen.requestFile()
            }
            row()
            textButton(text = "QUIT (Q)").onClick {
                app.viewScreen.quit()
            }
            textButton(text = "RESET DEFAULTS").onClick {
                app.viewScreen.quit()
            }
            label("Double pages")
            checkBox(""){
                onChange {
                    config.doublePage=isChecked
                }
            }
            row()
            label("Mouse scrollwheel sensitivity")
            val sens = slider(min=10f, max=100f, step=10f)
            row()
            label("Mouse movement sensitivity")
            slider(min=10f, max=100f, step=10f){

            }
            row()
            label("Number of columns")
            slider(min=1f, max=10f, step=1f)
            row()
            label("Keyboard scroll speed")
            slider(min=10f, max=100f, step=10f)
            row()
            label("Zoom speed")
            slider(min=10f, max=100f, step=10f)
            row()


            // Packing the root window:
            pack()
        }





        val window2 = window(title = "Settings") {
            chooser
            pack()
        }

      //  window2.add(chooser)
      //  window2.pack()

        stage!!.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                when(keycode){
                    Input.Keys.ESCAPE -> app.setScreen(app.viewScreen)
                    Input.Keys.Q -> app.viewScreen.quit()
                    Input.Keys.L -> app.viewScreen.requestFile()
                }
                return super.keyDown(event, keycode)
            }
        })
        chooser.setFillParent(true)
       window.setFillParent(true)
        table!!.setFillParent(true)
        stage!!.addActor(window)



        //table!!.setDebug(true) // This is optional, but enables debug lines for tables.

        // Add widgets to the table here.
    }

    override fun resize(width: Int, height: Int) {
        app.log.info("menuscreen resize")
        stage?.getViewport()?.update(width, height, true)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f,0f,0f,255f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage?.act(Gdx.graphics.deltaTime)
        stage?.draw()
    }

    override fun show() {
        app.log.info("menuscreen show")
        Gdx.input.isCursorCatched = false
        Gdx.input.inputProcessor = stage
    }

    override fun dispose() {
        app.log.info("menuscreen dispose")
        stage!!.dispose()
    }
}