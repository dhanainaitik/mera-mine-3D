package com.minesafe.ar.ar

import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.LightManager
import com.google.android.filament.MaterialInstance
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlin.random.Random

/**
 * Authentic Underground Coal Mine Environment.
 *
 * Replaces the old procedural doorway and synthetic corridor with:
 * 1. mine_entrance.glb as the ACTUAL entrance portal, positioned at the AR anchor (Z = 0)
 *    at human scale (~2.2m - 2.6m clear height), with zero old procedural doorway geometry.
 * 2. positanos_tunnel_optimized.glb connecting DIRECTLY to the inner opening of mine_entrance.glb
 *    with an overlapping seamless fit (zero bluish gap, zero empty space).
 * 3. Warm/neutral underground practical lighting (2800K - 3200K) revealing the rock geometry.
 * 4. Deep rockfall bulkhead sealing the far terminus around the natural curve.
 */
class MineEnvironmentNode(
    engine: Engine,
    modelLoader: ModelLoader? = null,
    rockMaterial: MaterialInstance? = null,
    floorMaterial: MaterialInstance? = null,
    woodMaterial: MaterialInstance? = null,
    metalMaterial: MaterialInstance? = null,
    lampMaterial: MaterialInstance? = null,
    hazardYellowMaterial: MaterialInstance? = null,
    pipeMaterial: MaterialInstance? = null,
    railMaterial: MaterialInstance? = metalMaterial,
    wetWalkwayMaterial: MaterialInstance? = floorMaterial,
    ventDuctMaterial: MaterialInstance? = hazardYellowMaterial
) : Node(engine) {

    private val tag = "MineEnvironmentNode"

    init {
        if (modelLoader != null) {
            // =========================================================================
            // 1. MINE ENTRANCE (mine_entrance.glb)
            // Replaces the old fake doorway completely.
            // Placed so its front timber beam is right at Z = 0.0m (the AR placement point).
            // Scale 0.36 produces a human-scale clear opening (~1.63m wide x 2.62m high).
            // Inner adit tunnel extends back to Z ≈ -1.76m ... -2.01m.
            // =========================================================================
            try {
                val entranceInstance = modelLoader.createModelInstance("models/mine_entrance.glb")
                if (entranceInstance != null) {
                    val entranceNode = ModelNode(
                        modelInstance = entranceInstance,
                        autoAnimate = false
                    ).apply {
                        scale = Float3(0.36f, 0.36f, 0.36f)
                        position = Float3(0.0f, -0.036f, -1.008f)
                        rotation = Float3(0.0f, 0.0f, 0.0f)
                    }
                    addChildNode(entranceNode)
                    Log.d(tag, "Successfully loaded mine_entrance.glb as portal entrance")
                } else {
                    Log.w(tag, "createModelInstance for mine_entrance.glb returned null")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading mine_entrance.glb: ${e.message}", e)
            }

            // =========================================================================
            // 2. UNDERGROUND TUNNEL (positanos_tunnel_optimized.glb)
            // Begins DIRECTLY from the inner opening of mine_entrance.glb.
            // Opening (End B) connects at Z = -1.75m, overlapping 25cm into the entrance adit.
            // Continuous human-scale 3D enclosure (2.7m ceiling, 3.1m width) extending ~30m deep.
            // Zero bluish gap, zero empty space, fully surrounds user once inside.
            // =========================================================================
            try {
                val tunnelInstance = modelLoader.createModelInstance("models/positanos_tunnel_optimized.glb")
                if (tunnelInstance != null) {
                    val tunnelNode = ModelNode(
                        modelInstance = tunnelInstance,
                        autoAnimate = false
                    ).apply {
                        scale = Float3(5.8f, 5.8f, 5.8f)
                        rotation = Float3(0.0f, -126.79f, 0.0f)
                        position = Float3(-0.559f, 0.187f, -19.262f)
                    }
                    addChildNode(tunnelNode)
                    Log.d(tag, "Successfully connected positanos_tunnel_optimized.glb directly to entrance")
                } else {
                    Log.w(tag, "createModelInstance for positanos_tunnel_optimized.glb returned null")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading positanos_tunnel_optimized.glb: ${e.message}", e)
            }
        }

        // =========================================================================
        // 3. SUBTLE NEUTRAL/WARM UNDERGROUND MINE LIGHTING
        // Eliminates any bluish cast. Uses realistic warm mine light (2800K - 3200K)
        // spaced along the entrance and tunnel path so rock surfaces are visible.
        // =========================================================================
        val lights = listOf(
            // Light 1: Portal entrance threshold (replaces old doorway lanterns)
            Triple(Float3(0.0f, 2.0f, -0.3f), 55000f, 6.0f),
            // Light 2: Direct junction between entrance and tunnel (illuminates the seamless seam)
            Triple(Float3(0.0f, 2.1f, -1.8f), 50000f, 6.5f),
            // Light 3: First tunnel section
            Triple(Float3(0.12f, 2.1f, -7.0f), 45000f, 7.0f),
            // Light 4: Mid tunnel section
            Triple(Float3(0.18f, 2.1f, -14.0f), 40000f, 7.5f),
            // Light 5: Deep underground drift
            Triple(Float3(0.24f, 2.1f, -21.0f), 35000f, 8.0f),
            // Light 6: Terminus approach
            Triple(Float3(0.28f, 2.1f, -28.0f), 30000f, 8.0f)
        )

        for ((pos, intensity, falloff) in lights) {
            try {
                val lightBuilder = LightManager.Builder(LightManager.Type.POINT)
                    .color(1.0f, 0.82f, 0.55f)
                    .intensity(intensity)
                    .falloff(falloff)
                val lightNode = LightNode(engine, builder = lightBuilder).apply {
                    position = pos
                }
                addChildNode(lightNode)
            } catch (e: Exception) {
                Log.w(tag, "Failed to create light at $pos: ${e.message}")
            }
        }

        // =========================================================================
        // 4. NATURAL ROCK TERMINUS (Zero Flat Walls, Zero Room Leaks)
        // Deep at the end of the tunnel drift (~31.5m back), an organic coal rockfall
        // bulkhead completely seals the drift so no external world/sky is visible.
        // =========================================================================
        val rng = Random(101)
        val termX = 0.25f
        val termZ = -31.5f
        val rockCount = 12

        for (i in 0 until rockCount) {
            val rw = 1.6f + rng.nextFloat() * 0.8f
            val rh = 1.2f + rng.nextFloat() * 0.6f
            val rx = termX + (rng.nextFloat() * 3.4f - 1.7f)
            val rz = termZ - (rng.nextFloat() * 1.5f)
            val ry = (i * 0.30f) + 0.20f

            val rockBlock = CubeNode(engine, size = Float3(rw, rh, 1.8f), materialInstance = rockMaterial).apply {
                position = Float3(rx, ry, rz)
                rotation = Float3(
                    rng.nextFloat() * 24f - 12f,
                    rng.nextFloat() * 36f - 18f,
                    rng.nextFloat() * 16f - 8f
                )
            }
            addChildNode(rockBlock)
        }

        val fallenTimber = CubeNode(engine, size = Float3(0.24f, 2.6f, 0.24f), materialInstance = woodMaterial).apply {
            position = Float3(termX + 0.2f, 1.0f, termZ + 0.4f)
            rotation = Float3(30f, 25f, -35f)
        }
        addChildNode(fallenTimber)
    }
}
