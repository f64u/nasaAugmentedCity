package com.faddyy.breakinggreen

import android.view.MotionEvent
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import android.widget.TextView
import android.R
import android.content.Context
import android.view.View
import android.widget.Button
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.math.Vector3

data class ComponentData(
    val displayName: String,
    val renderableName: String,
    val greenness: Double,
    val production: Double
) {
    companion object {
        val main = mapOf("Palace" to ComponentData("Palace", "Palace", 0.0, 0.0))
        val urban = mapOf(
            "SM_Skyscraper_02" to ComponentData("Brown Skyscraper", "SM_Skyscraper_02", 0.0, 0.0),
            "ParkingLot" to ComponentData("Parking Lot", "ParkingLot", 0.0, 0.0),
            "FourSmallHouses" to ComponentData("Four Small Houses", "FourSmallHouses", 0.0, 0.0),
            "GasStation" to ComponentData("Gas Station", "GasStation", 0.0, 0.0),
            "ModernHouse" to ComponentData("Modern House", "ModernHouse", 0.0, 0.0),
            "Factory_2" to ComponentData("Factory 2", "Factory_2", 1.0, 0.0),
            "Factory_1" to ComponentData("Factory 1", "Factory_1", 1.0, 0.0),
            "Factory_3" to ComponentData("Factory 3", "Factory_3", 1.0, 0.0)

        )
        val rural = mapOf(
            "Skyscraper" to ComponentData("Empire State Building", "Skyscraper", 0.0, 0.0),
            "flowers" to ComponentData("Flowers", "flowers", 0.0, .1),
            "Coffee_Shop" to ComponentData("Coffee Shop", "Coffee_Shop", 0.0, .5),
            "Coast_1261" to ComponentData("Lake", "Coast_1261", 0.0, .5),
            "trees" to ComponentData("Trees", "trees", 0.0, 0.0)
        )

        val all: Map<String, ComponentData>
            get() {
                val allEateries = mutableMapOf<String, ComponentData>()
                allEateries.putAll(main)
                allEateries.putAll(urban)
                allEateries.putAll(rural)
                return allEateries
            }
    }
}

data class ComponentStored(val renderableName: String, val coordinates: Coordinates)

class Component(
    val context: Context,
    val componentData: ComponentData,
    mRenderable: ModelRenderable,
    val onDelete: View.OnTouchListener
) : Node(), Node.OnTapListener {
    private val INFO_CARD_Y_POS_COEFF = 0.55f

    private lateinit var deleteCard: Node

    init {
        setOnTapListener(this)
        renderable = mRenderable
    }

    override fun onActivate() {
        if (!this::deleteCard.isInitialized) {
            deleteCard = Node()
            deleteCard.setParent(this)
            deleteCard.isEnabled = false
            deleteCard.localPosition = SVector3(0.0f, 2f + this.localScale.y, 0.0f)

            val button = Button(context)
            button.text = "Delete"
            button.width = 450
            button.height = 300
            button.setOnTouchListener(onDelete)

            ViewRenderable.builder()
                .setView(context, button)
                .build()
                .thenAccept { renderable ->
                    deleteCard.setRenderable(renderable)
                }
                .exceptionally(
                    { throwable ->
                        throw AssertionError(
                            "Could not load plane card view.",
                            throwable
                        )
                    })
        }
    }


    override fun onTap(hitTestResult: HitTestResult, motionEvent: MotionEvent) {
        if (!this::deleteCard.isInitialized) {
            return
        }

        deleteCard.isEnabled = !deleteCard.isEnabled
    }

}
