package com.minesafe.ar.ar

import dev.romainguy.kotlin.math.Float3
import com.google.android.filament.Engine
import com.google.android.filament.MaterialInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.Node

class ExtinguisherNode(engine: Engine, bodyMaterial: MaterialInstance?, handleMaterial: MaterialInstance?) : Node(engine) {
    
    val safetyPin: CubeNode
    val nozzle: CubeNode
    
    init {
        // Body (Red cylinder)
        val body = CylinderNode(engine, radius = 0.12f, height = 0.6f, materialInstance = bodyMaterial).apply {
            position = Float3(0.0f, 0.3f, 0.0f)
        }
        
        // Handle
        val handle = CubeNode(engine, size = Float3(0.05f, 0.1f, 0.2f), materialInstance = handleMaterial).apply {
            position = Float3(0.0f, 0.65f, 0.05f)
        }
        
        // Safety Pin (Interactive)
        safetyPin = CubeNode(engine, size = Float3(0.15f, 0.02f, 0.02f), materialInstance = handleMaterial).apply {
            position = Float3(0.0f, 0.62f, 0.0f)
        }
        
        // Nozzle (Interactive)
        nozzle = CubeNode(engine, size = Float3(0.06f, 0.06f, 0.3f), materialInstance = handleMaterial).apply {
            position = Float3(0.0f, 0.5f, 0.2f)
        }

        addChildNode(body)
        addChildNode(handle)
        addChildNode(safetyPin)
        addChildNode(nozzle)
    }
    
    fun removeSafetyPin() {
        safetyPin.isVisible = false
    }
}
