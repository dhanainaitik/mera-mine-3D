package com.minesafe.ar.ar

import com.google.android.filament.Engine
import com.google.android.filament.MaterialInstance
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.Node
import kotlin.random.Random

class MineEnvironmentNode(
    engine: Engine,
    rockMaterial: MaterialInstance,
    floorMaterial: MaterialInstance,
    woodMaterial: MaterialInstance,
    metalMaterial: MaterialInstance,
    lampMaterial: MaterialInstance
) : Node(engine) {

    init {
        // Enclose the starting area behind the doorway
        buildTunnelSegment(engine, 0f, 4f, 20f, 180f, 5.0f, 3.5f, false, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)

        // Main Corridor
        val (x1, z1) = buildTunnelSegment(engine, 0f, 4f, 30f, 0f, 5.0f, 3.5f, true, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)
        val (x2, z2) = buildTunnelSegment(engine, x1, z1, 40f, 15f, 5.5f, 3.8f, true, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)
        val (x3, z3) = buildTunnelSegment(engine, x2, z2, 50f, -20f, 4.5f, 3.2f, true, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)
        val (x4, z4) = buildTunnelSegment(engine, x3, z3, 60f, -40f, 4.0f, 3.0f, true, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)
        buildTunnelSegment(engine, x4, z4, 80f, -10f, 4.0f, 3.0f, true, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)

        // Branches based on Heatmap
        // Left branch (Area 5/6)
        buildTunnelSegment(engine, x1, z1, 40f, -55f, 4.0f, 3.0f, false, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)

        // Right branch (Area 1 - Main Excavation)
        buildTunnelSegment(engine, x2, z2, 50f, 75f, 8.0f, 5.0f, false, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)

        // Right branch deeper (Area 2 - Lava Trap)
        buildTunnelSegment(engine, x3, z3, 40f, 60f, 4.5f, 3.0f, false, rockMaterial, floorMaterial, woodMaterial, metalMaterial, lampMaterial)
    }

    private fun buildTunnelSegment(
        engine: Engine,
        startX: Float,
        startZ: Float,
        length: Float,
        angleDeg: Float,
        width: Float,
        height: Float,
        hasRails: Boolean,
        rockMaterial: MaterialInstance,
        floorMaterial: MaterialInstance,
        woodMaterial: MaterialInstance,
        metalMaterial: MaterialInstance,
        lampMaterial: MaterialInstance
    ): Pair<Float, Float> {
        val rad = Math.toRadians(angleDeg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()

        var d = 0f
        while (d <= length) {
            val cx = startX + sin * d
            val cz = startZ - cos * d

            // Floor
            val floor = CubeNode(engine, size = Float3(width + 1f, 0.2f, 2.2f), materialInstance = floorMaterial).apply {
                position = Float3(cx, -0.1f, cz)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(floor)

            // Ceiling (Uneven)
            val ceilingY = height + Random.nextFloat() * 0.2f
            val ceiling = CubeNode(engine, size = Float3(width + 1f, 0.4f, 2.2f), materialInstance = rockMaterial).apply {
                position = Float3(cx, ceilingY, cz)
                rotation = Float3((Random.nextFloat() * 4 - 2), angleDeg, (Random.nextFloat() * 4 - 2))
            }
            addChildNode(ceiling)

            // Left Wall (Uneven blocks)
            val lx = cx - (width / 2f) * cos
            val lz = cz - (width / 2f) * sin
            val leftWall = CubeNode(engine, size = Float3(0.8f + Random.nextFloat() * 0.5f, height + 0.5f, 2.2f), materialInstance = rockMaterial).apply {
                position = Float3(lx, height / 2f, lz)
                rotation = Float3(Random.nextFloat() * 10 - 5, angleDeg + Random.nextFloat() * 10 - 5, Random.nextFloat() * 10 - 5)
            }
            addChildNode(leftWall)

            // Right Wall (Uneven blocks)
            val rx = cx + (width / 2f) * cos
            val rz = cz + (width / 2f) * sin
            val rightWall = CubeNode(engine, size = Float3(0.8f + Random.nextFloat() * 0.5f, height + 0.5f, 2.2f), materialInstance = rockMaterial).apply {
                position = Float3(rx, height / 2f, rz)
                rotation = Float3(Random.nextFloat() * 10 - 5, angleDeg + Random.nextFloat() * 10 - 5, Random.nextFloat() * 10 - 5)
            }
            addChildNode(rightWall)

            // Wooden Supports (Every 4 meters approx)
            if (d % 4f < 1f) {
                val supportWidth = 0.2f
                // Left Pillar
                val lPillarX = cx - (width / 2.2f) * cos
                val lPillarZ = cz - (width / 2.2f) * sin
                val leftPillar = CubeNode(engine, size = Float3(supportWidth, height, supportWidth), materialInstance = woodMaterial).apply {
                    position = Float3(lPillarX, height / 2f, lPillarZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(leftPillar)

                // Right Pillar
                val rPillarX = cx + (width / 2.2f) * cos
                val rPillarZ = cz + (width / 2.2f) * sin
                val rightPillar = CubeNode(engine, size = Float3(supportWidth, height, supportWidth), materialInstance = woodMaterial).apply {
                    position = Float3(rPillarX, height / 2f, rPillarZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(rightPillar)

                // Top Beam
                val topBeam = CubeNode(engine, size = Float3(width, supportWidth, supportWidth), materialInstance = woodMaterial).apply {
                    position = Float3(cx, height - 0.1f, cz)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(topBeam)
            }

            // Rails
            if (hasRails) {
                val railWidth = 0.1f
                val railSpacing = 1.2f
                // Left rail
                val lRailX = cx - (railSpacing / 2f) * cos
                val lRailZ = cz - (railSpacing / 2f) * sin
                val leftRail = CubeNode(engine, size = Float3(railWidth, 0.05f, 2.2f), materialInstance = metalMaterial).apply {
                    position = Float3(lRailX, 0.05f, lRailZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(leftRail)

                // Right rail
                val rRailX = cx + (railSpacing / 2f) * cos
                val rRailZ = cz + (railSpacing / 2f) * sin
                val rightRail = CubeNode(engine, size = Float3(railWidth, 0.05f, 2.2f), materialInstance = metalMaterial).apply {
                    position = Float3(rRailX, 0.05f, rRailZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(rightRail)

                // Wooden Tie
                if (d % 2f < 1f) {
                    val tie = CubeNode(engine, size = Float3(railSpacing + 0.4f, 0.05f, 0.3f), materialInstance = woodMaterial).apply {
                        position = Float3(cx, 0.02f, cz)
                        rotation = Float3(0f, angleDeg, 0f)
                    }
                    addChildNode(tie)
                }
            }

            // Ceiling Lamps (Every 10 meters)
            if (d > 0f && d % 10f < 1f) {
                val lamp = CubeNode(engine, size = Float3(0.3f, 0.1f, 0.3f), materialInstance = lampMaterial).apply {
                    position = Float3(cx, height - 0.05f, cz)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(lamp)
            }

            d += 2f
        }

        return Pair(startX + sin * length, startZ - cos * length)
    }
}
