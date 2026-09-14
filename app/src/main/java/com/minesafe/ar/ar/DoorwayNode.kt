package com.minesafe.ar.ar

import com.google.android.filament.Engine
import com.google.android.filament.LightManager
import com.google.android.filament.MaterialInstance
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.Node
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Realistic industrial coal-mine entrance doorway matching Reference Image 2.
 * Human-scale dimensions:
 * - Clear opening: 1.20m wide x 2.20m high.
 * - Outer rock formation: ~2.8m - 3.2m wide, ~3.3m high, ~0.8m deep.
 * - Heavy structural timber posts with cast-iron bolted base plates and corner gusset straps.
 * - Overhead carved wooden signboard: "MINE SAFE AR -- ENTER TO EXPLORE --".
 * - Dual explosion-proof warm amber mine lanterns (2800K) with Filament point lights.
 * - Ground railway tracks and wooden sleepers passing through the threshold.
 */
class DoorwayNode(
    engine: Engine,
    rockMaterial: MaterialInstance?,
    woodMaterial: MaterialInstance?,
    metalMaterial: MaterialInstance?,
    lampEmissiveMaterial: MaterialInstance?,
    signMaterial: MaterialInstance?
) : Node(engine) {

    init {
        val clearWidth = 1.20f
        val clearHeight = 2.20f
        val postWidth = 0.22f
        val postDepth = 0.25f

        val leftPostX = -(clearWidth / 2f + postWidth / 2f) // -0.71m
        val rightPostX = (clearWidth / 2f + postWidth / 2f)  // +0.71m
        val postCenterY = clearHeight / 2f                   // 1.10m

        // ==========================================
        // 1. STRUCTURAL TIMBER POSTS & LINTEL
        // ==========================================
        // Left Vertical Timber Post
        val leftPost = CubeNode(engine, size = Float3(postWidth, clearHeight, postDepth), materialInstance = woodMaterial).apply {
            position = Float3(leftPostX, postCenterY, 0.0f)
        }
        addChildNode(leftPost)

        // Right Vertical Timber Post
        val rightPost = CubeNode(engine, size = Float3(postWidth, clearHeight, postDepth), materialInstance = woodMaterial).apply {
            position = Float3(rightPostX, postCenterY, 0.0f)
        }
        addChildNode(rightPost)

        // Top Lintel Crossbeam
        val beamWidth = clearWidth + (postWidth * 2f) + 0.16f // ~1.80m
        val beamHeight = 0.24f
        val beamY = clearHeight + (beamHeight / 2f)           // 2.32m
        val topBeam = CubeNode(engine, size = Float3(beamWidth, beamHeight, postDepth + 0.04f), materialInstance = woodMaterial).apply {
            position = Float3(0.0f, beamY, 0.0f)
        }
        addChildNode(topBeam)

        // Diagonal Knee-Braces (Corner support struts)
        val braceSize = Float3(0.12f, 0.35f, 0.16f)
        val leftBrace = CubeNode(engine, size = braceSize, materialInstance = woodMaterial).apply {
            position = Float3(-0.48f, 2.05f, 0.0f)
            rotation = Float3(0.0f, 0.0f, -45.0f)
        }
        val rightBrace = CubeNode(engine, size = braceSize, materialInstance = woodMaterial).apply {
            position = Float3(0.48f, 2.05f, 0.0f)
            rotation = Float3(0.0f, 0.0f, 45.0f)
        }
        addChildNode(leftBrace)
        addChildNode(rightBrace)

        // ==========================================
        // 2. STEEL REINFORCEMENTS & BASE BRACKETS
        // ==========================================
        val baseSize = Float3(postWidth + 0.06f, 0.25f, postDepth + 0.06f)
        val leftBase = CubeNode(engine, size = baseSize, materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, 0.125f, 0.0f)
        }
        val rightBase = CubeNode(engine, size = baseSize, materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, 0.125f, 0.0f)
        }
        addChildNode(leftBase)
        addChildNode(rightBase)

        // Mid-height steel reinforcing straps & corner gussets
        val strapSize = Float3(postWidth + 0.02f, 0.08f, postDepth + 0.02f)
        val leftStrap = CubeNode(engine, size = strapSize, materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, 1.25f, 0.0f)
        }
        val rightStrap = CubeNode(engine, size = strapSize, materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, 1.25f, 0.0f)
        }
        addChildNode(leftStrap)
        addChildNode(rightStrap)

        // Corner steel plates
        val plateSize = Float3(0.24f, 0.24f, 0.02f)
        val leftCornerPlate = CubeNode(engine, size = plateSize, materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, 2.20f, (postDepth / 2f) + 0.02f)
        }
        val rightCornerPlate = CubeNode(engine, size = plateSize, materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, 2.20f, (postDepth / 2f) + 0.02f)
        }
        addChildNode(leftCornerPlate)
        addChildNode(rightCornerPlate)

        // ==========================================
        // 3. CARVED WOODEN SIGNBOARD PLAQUE
        // "MINE SAFE AR -- ENTER TO EXPLORE --"
        // ==========================================
        val signBoard = CubeNode(engine, size = Float3(1.45f, 0.36f, 0.08f), materialInstance = woodMaterial).apply {
            position = Float3(0.0f, 2.62f, 0.04f)
        }
        val signBorderTop = CubeNode(engine, size = Float3(1.49f, 0.03f, 0.10f), materialInstance = metalMaterial).apply {
            position = Float3(0.0f, 2.80f, 0.04f)
        }
        val signBorderBottom = CubeNode(engine, size = Float3(1.49f, 0.03f, 0.10f), materialInstance = metalMaterial).apply {
            position = Float3(0.0f, 2.44f, 0.04f)
        }
        // Plaque center emblem bar / lettering relief
        val signTextBar = CubeNode(engine, size = Float3(1.10f, 0.14f, 0.03f), materialInstance = signMaterial).apply {
            position = Float3(0.0f, 2.65f, 0.09f)
        }
        val signSubBar = CubeNode(engine, size = Float3(0.80f, 0.05f, 0.02f), materialInstance = signMaterial).apply {
            position = Float3(0.0f, 2.52f, 0.09f)
        }
        addChildNode(signBoard)
        addChildNode(signBorderTop)
        addChildNode(signBorderBottom)
        addChildNode(signTextBar)
        addChildNode(signSubBar)

        // ==========================================
        // 4. NATURAL COAL-BEARING ROCK SURROUND
        // Massive, faceted, irregular rock facade framing the entrance
        // ==========================================
        val rockRng = Random(1024)
        val rockFacadeWidth = 3.1f
        val rockFacadeHeight = 3.4f
        val rockDepth = 0.75f

        // Left Rock Wall Formations (multiple staggered blocks creating natural rock face)
        for (i in 0 until 8) {
            val h = 0.45f + rockRng.nextFloat() * 0.15f
            val y = i * 0.40f + 0.22f
            val w = 0.70f + rockRng.nextFloat() * 0.35f
            val x = -(clearWidth / 2f + postWidth + (w / 2f) - 0.08f)
            val z = (rockRng.nextFloat() * 0.25f) - 0.15f
            val rotY = rockRng.nextFloat() * 20f - 10f
            val rotZ = rockRng.nextFloat() * 8f - 4f

            val rockBlock = CubeNode(engine, size = Float3(w, h, rockDepth + rockRng.nextFloat() * 0.2f), materialInstance = rockMaterial).apply {
                position = Float3(x, y, z)
                rotation = Float3(rockRng.nextFloat() * 6f - 3f, rotY, rotZ)
            }
            addChildNode(rockBlock)
        }

        // Right Rock Wall Formations
        for (i in 0 until 8) {
            val h = 0.45f + rockRng.nextFloat() * 0.15f
            val y = i * 0.40f + 0.22f
            val w = 0.70f + rockRng.nextFloat() * 0.35f
            val x = (clearWidth / 2f + postWidth + (w / 2f) - 0.08f)
            val z = (rockRng.nextFloat() * 0.25f) - 0.15f
            val rotY = rockRng.nextFloat() * 20f - 10f
            val rotZ = rockRng.nextFloat() * 8f - 4f

            val rockBlock = CubeNode(engine, size = Float3(w, h, rockDepth + rockRng.nextFloat() * 0.2f), materialInstance = rockMaterial).apply {
                position = Float3(x, y, z)
                rotation = Float3(rockRng.nextFloat() * 6f - 3f, rotY, rotZ)
            }
            addChildNode(rockBlock)
        }

        // Overhead Rock Arch & Crest (Arching over the lintel and sign plaque)
        for (i in 0 until 7) {
            val archX = -1.2f + (i * 0.40f)
            val archY = 2.95f + sin(i / 6.0 * Math.PI).toFloat() * 0.35f + rockRng.nextFloat() * 0.12f
            val archW = 0.55f + rockRng.nextFloat() * 0.2f
            val archH = 0.50f + rockRng.nextFloat() * 0.2f

            val crestRock = CubeNode(engine, size = Float3(archW, archH, rockDepth + 0.15f), materialInstance = rockMaterial).apply {
                position = Float3(archX, archY, (rockRng.nextFloat() * 0.2f) - 0.1f)
                rotation = Float3(rockRng.nextFloat() * 12f - 6f, rockRng.nextFloat() * 16f - 8f, rockRng.nextFloat() * 10f - 5f)
            }
            addChildNode(crestRock)
        }

        // ==========================================
        // 5. WARM AMBER MINE LANTERNS (2800K)
        // Vintage industrial explosion-proof cage sconces on both posts
        // ==========================================
        val lanternY = 1.65f
        val lanternZ = (postDepth / 2f) + 0.12f

        // Left Lantern Assembly
        val leftSconceArm = CubeNode(engine, size = Float3(0.04f, 0.04f, 0.14f), materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, lanternY + 0.12f, lanternZ - 0.06f)
        }
        val leftLanternCap = CylinderNode(engine, radius = 0.08f, height = 0.04f, materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, lanternY + 0.10f, lanternZ)
        }
        val leftLanternBulb = CylinderNode(engine, radius = 0.06f, height = 0.16f, materialInstance = lampEmissiveMaterial).apply {
            position = Float3(leftPostX, lanternY, lanternZ)
        }
        val leftLanternBase = CylinderNode(engine, radius = 0.08f, height = 0.04f, materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, lanternY - 0.10f, lanternZ)
        }
        // Protective cage bars
        val leftCageBar1 = CubeNode(engine, size = Float3(0.015f, 0.20f, 0.14f), materialInstance = metalMaterial).apply {
            position = Float3(leftPostX, lanternY, lanternZ)
        }
        addChildNode(leftSconceArm)
        addChildNode(leftLanternCap)
        addChildNode(leftLanternBulb)
        addChildNode(leftLanternBase)
        addChildNode(leftCageBar1)

        // Left Point Light (Warm Amber ~2800K, R:1.0, G:0.76, B:0.38)
        try {
            val leftBuilder = LightManager.Builder(LightManager.Type.POINT)
                .color(1.0f, 0.76f, 0.38f)
                .intensity(110000f)
                .falloff(7.5f)
            val leftLightNode = LightNode(engine, builder = leftBuilder).apply {
                position = Float3(leftPostX, lanternY, lanternZ)
            }
            addChildNode(leftLightNode)
        } catch (_: Exception) {}

        // Right Lantern Assembly
        val rightSconceArm = CubeNode(engine, size = Float3(0.04f, 0.04f, 0.14f), materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, lanternY + 0.12f, lanternZ - 0.06f)
        }
        val rightLanternCap = CylinderNode(engine, radius = 0.08f, height = 0.04f, materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, lanternY + 0.10f, lanternZ)
        }
        val rightLanternBulb = CylinderNode(engine, radius = 0.06f, height = 0.16f, materialInstance = lampEmissiveMaterial).apply {
            position = Float3(rightPostX, lanternY, lanternZ)
        }
        val rightLanternBase = CylinderNode(engine, radius = 0.08f, height = 0.04f, materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, lanternY - 0.10f, lanternZ)
        }
        val rightCageBar1 = CubeNode(engine, size = Float3(0.015f, 0.20f, 0.14f), materialInstance = metalMaterial).apply {
            position = Float3(rightPostX, lanternY, lanternZ)
        }
        addChildNode(rightSconceArm)
        addChildNode(rightLanternCap)
        addChildNode(rightLanternBulb)
        addChildNode(rightLanternBase)
        addChildNode(rightCageBar1)

        // Right Point Light
        try {
            val rightBuilder = LightManager.Builder(LightManager.Type.POINT)
                .color(1.0f, 0.76f, 0.38f)
                .intensity(110000f)
                .falloff(7.5f)
            val rightLightNode = LightNode(engine, builder = rightBuilder).apply {
                position = Float3(rightPostX, lanternY, lanternZ)
            }
            addChildNode(rightLightNode)
        } catch (_: Exception) {}

        // ==========================================
        // 6. THRESHOLD TRACKS & SLEEPERS
        // Passing through the 1.2m entrance opening
        // ==========================================
        val railSpacing = 0.85f
        val railWidth = 0.06f
        val railHeight = 0.05f
        val entranceDepth = 1.4f

        // Left Rail
        val leftRail = CubeNode(engine, size = Float3(railWidth, railHeight, entranceDepth), materialInstance = metalMaterial).apply {
            position = Float3(-railSpacing / 2f, 0.035f, -entranceDepth / 4f)
        }
        // Right Rail
        val rightRail = CubeNode(engine, size = Float3(railWidth, railHeight, entranceDepth), materialInstance = metalMaterial).apply {
            position = Float3(railSpacing / 2f, 0.035f, -entranceDepth / 4f)
        }
        addChildNode(leftRail)
        addChildNode(rightRail)

        // Cross Sleepers (Ties)
        val sleeperWidth = 1.15f
        val sleeperHeight = 0.04f
        val sleeperDepth = 0.16f
        for (zOffset in listOf(0.3f, 0.0f, -0.3f, -0.6f, -0.9f)) {
            val tie = CubeNode(engine, size = Float3(sleeperWidth, sleeperHeight, sleeperDepth), materialInstance = woodMaterial).apply {
                position = Float3(0.0f, 0.015f, zOffset)
            }
            addChildNode(tie)
        }

        // Floor threshold transition plate
        val thresholdPlate = CubeNode(engine, size = Float3(clearWidth + 0.1f, 0.015f, 0.35f), materialInstance = rockMaterial).apply {
            position = Float3(0.0f, 0.008f, 0.0f)
        }
        addChildNode(thresholdPlate)
    }
}
