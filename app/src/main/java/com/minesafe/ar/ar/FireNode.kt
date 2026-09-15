package com.minesafe.ar.ar

import dev.romainguy.kotlin.math.Float3
import com.google.android.filament.Engine
import com.google.android.filament.MaterialInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.Node

class FireNode(engine: Engine, materialInstance: MaterialInstance?) : Node(engine) {
    val fireCube: CubeNode

    init {
        fireCube = CubeNode(engine, size = Float3(0.4f, 0.4f, 0.4f), materialInstance = materialInstance).apply {
            position = Float3(0.0f, 0.2f, 0.0f)
        }
        addChildNode(fireCube)
    }

    fun setFireScale(scale: Float) {
        fireCube.scale = Float3(scale, scale, scale)
    }
}
