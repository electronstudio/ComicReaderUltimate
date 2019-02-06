package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import ktx.scene2d.*
import net.dermetfan.gdx.scenes.scene2d.ui.FileChooser
import net.dermetfan.gdx.scenes.scene2d.ui.ListFileChooser
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration



class MenuScreen(val app: App): ScreenAdapter() {
    private var stage: Stage? = null
    private var table: Table? = null

    init {
        stage = Stage()
        Gdx.input.inputProcessor = stage

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
            textButton(text = "LOAD (L)")
            textButton(text = "QUIT (Q)")
            row()
            label("Mouse scrollwheel sensitivity")
            slider(min=10f, max=100f, step=10f)
            row()
            label("Mouse movement sensitivity")
            slider(min=10f, max=100f, step=10f)
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

        chooser.setFillParent(true)
       window.setFillParent(true)
        table!!.setFillParent(true)
        stage!!.addActor(window)

        //table!!.setDebug(true) // This is optional, but enables debug lines for tables.

        // Add widgets to the table here.
    }

    override fun resize(width: Int, height: Int) {
        stage?.getViewport()?.update(width, height, true)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f,0f,0f,255f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage?.act(Gdx.graphics.deltaTime)
        stage?.draw()
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)){
            System.exit(0)
        }

    }


    override fun dispose() {
        stage!!.dispose()
    }
}