package com.faddyy.breakinggreen.city

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.faddyy.breakinggreen.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private val components = mutableMapOf<Coordinates, Component>()

    private lateinit var sharedPreferences: SharedPreferences

    private val componentRenderables = mutableMapOf<String, ModelRenderable>()
    private lateinit var storedComponents: List<ComponentStored>

    private lateinit var arFragment: CustomArFragment
    private lateinit var desertRenderable: ModelRenderable
    private lateinit var city: Node
    private var chosenComponent: ComponentData? = ComponentData.all["Palace"]

    private var greenness: Double by Delegates.observable(0.0) { property, oldValue, newValue ->
        if(newValue >= 3) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Pollutant values have reached their maximum optimum level (3 factories).")
            val alert = builder.create()
            alert.show()
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val cityjson = "cityjson"
        const val cityKey = "cityKey"
        const val LOAD_SAVED = "LOAD_SAVED"

        val desertDimensions = SVector3(3f, .1f, 1.5f)
    }

    private fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
        val assetManager = context.assets

        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open("images/$filePath")
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(cityjson, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(
                cityKey,
                gson.toJson(
                    listOf(
                        ComponentStored("Palace", Coordinates(0f, 0f))
                    )
                )
            )
            .commit()

        if (intent.getBooleanExtra(LOAD_SAVED, false) && sharedPreferences.contains(cityKey)) {
            val runTimeType = object : TypeToken<List<ComponentStored>>() {}.type
            storedComponents = gson.fromJson<List<ComponentStored>>(
                sharedPreferences.getString(
                    cityKey,
                    "[]"
                ), runTimeType
            )
        } else {
            storedComponents = arrayListOf(ComponentStored("Palace", Coordinates(0f, 0f)))
        }


        for ((key, value) in ComponentData.all) {
            if (ComponentData.main.containsKey(key)) continue

            val layout = LinearLayout(this)
            layout.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layout.orientation = LinearLayout.VERTICAL

            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(500, 500)
            params.gravity = Gravity.CENTER
            imageView.layoutParams = params
            imageView.setImageBitmap(getBitmapFromAsset(this, "${key}.png"))
            layout.addView(imageView)


            val textView = TextView(this)
            val params2 = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params2.setMargins(0, -100, 0, 0)
            params2.gravity = Gravity.CENTER
            textView.layoutParams = params2
            textView.text = value.displayName
            textView.setTextAppearance(R.style.TextAppearance_AppCompat_Caption)
            layout.addView(textView)

            layout.setOnClickListener {
                showText("${value.displayName} chosen")
                chosenComponent = value
            }

            componentsLayout.addView(layout)
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment
        if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        loadRenderables()
        MaterialFactory.makeOpaqueWithColor(
            this, Color(intent.getIntExtra("color", 0xF0E68C))
        ).thenAccept {
            desertRenderable = ShapeFactory.makeCube(
                desertDimensions,
                SVector3(0f, 0f, 0f),
                it
            )
        }

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (componentRenderables.isEmpty()) /* did not load yet */ return@setOnTapArPlaneListener
            Log.i(TAG, "Just hit a plane")

            val frame = arFragment.arSceneView.arFrame
            frame?.also {

                val node = AnchorNode(hitResult.createAnchor())

                Log.i(TAG, "Created node")
                node.setParent(arFragment.arSceneView.scene)
                if (!this::city.isInitialized) {
                    tryPlaceCity(node)
                }

            }
        }
    }

    private fun showText(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun tryPlaceComponent(componentData: ComponentData, node: Node) {
        node.setParent(city)

        val anchorNode = AnchorNode()
        anchorNode.setParent(node)

        val component = Component(
            this,
            componentData,
            componentRenderables[componentData.renderableName]!!, View.OnTouchListener { v, event ->
                node.setParent(null)
                greenness -= componentData.greenness
                Log.i(TAG, "removed child")
                true
            })

        val oldScale = component.localScale
        component.localScale =
            SVector3(oldScale.x, oldScale.y, oldScale.z, isObject = true)

        val transformableNode = TransformableNode(
            arFragment.transformationSystem
        )
        transformableNode.scaleController.isEnabled = false
        component.setParent(transformableNode)
        transformableNode.setParent(anchorNode)

        greenness += componentData.greenness

        val collisions =
            arFragment.arSceneView.scene.overlapTestAll(component).filterIsInstance<Component>()
        if (collisions.isNotEmpty()) {
            city.removeChild(node)
            greenness -= componentData.greenness
            showText("An overlap occurred! Please try a different location.")
        }
    }

    private fun tryPlaceCity(node: Node) {
        node.setParent(arFragment.arSceneView.scene)
        val baseNode = createCity()
        node.addChild(baseNode)
    }

    private fun createCity(): Node {
        val base = Node()

        val desert = Node()
        desert.setParent(base)
        desert.renderable = desertRenderable
        desert.localPosition = SVector3(0f, 0f, 0f)
        desert.setOnTapListener { _, motionEvent ->
            Log.i(TAG, "putting component")

            val frame = arFragment.arSceneView.arFrame
            val hitResults = frame!!.hitTest(motionEvent)
            Log.i(TAG, "hit results are $hitResults")
            val anchorNode = AnchorNode(hitResults[0].createAnchor())
            val node = Node()
            val oldPosition = anchorNode.worldPosition
            node.localPosition = Vector3(
                oldPosition.x - city.worldPosition.x,
                0f,
                oldPosition.z - city.worldPosition.z
            )
            arFragment.arSceneView.scene.addChild(node)
            node.setParent(city)

            tryPlaceComponent(
                chosenComponent!!, node
            )
        }

        city = Node()
        city.setParent(base)
        city.localPosition = Vector3(0f, desertDimensions.y, 0f)

        for (component in storedComponents) {
            val anchor = AnchorNode()
            anchor.localPosition = component.coordinates.toSVector3()
            tryPlaceComponent(
                ComponentData.all[component.renderableName] ?: error("Renderable not found"),
                anchor
            )
        }

        return base
    }

    private fun loadRenderables() {
        val componentFutures = mutableMapOf<String, CompletableFuture<ModelRenderable>>()

        for (component in ComponentData.all.keys) {
            componentFutures[component] =
                ModelRenderable.builder().setSource(this, Uri.parse("$component.sfb")).build()
        }

        CompletableFuture.allOf(*componentFutures.values.toTypedArray())
            .handle { _, throwable ->
                if (throwable != null) {
                    DemoUtils.displayError(
                        this,
                        "Unable to load renderable",
                        throwable
                    )
                    return@handle null
                }

                try {
                    for ((key, component) in componentFutures.entries) {
                        componentRenderables[key] = component.get()
                    }
                } catch (e: Exception) {
                    DemoUtils.displayError(
                        this,
                        "Unable to load renderables",
                        e
                    )
                }

                return@handle null
            }

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to save current city?")
            .setPositiveButton("Yes") { dialog, id ->
                saveComponents()
                this.finish()
            }
            .setNegativeButton("No") { dialog, which ->
                this.finish()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun saveComponents() {
        val componentsStored = components.values.map {
            val p = it.localPosition

            ComponentStored(it.componentData.renderableName, Coordinates(p.x, p.z))
        }
        sharedPreferences.edit().putString(cityKey, gson.toJson(componentsStored)).commit()
    }
}
