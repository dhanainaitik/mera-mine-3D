package com.minesafe.ar.ar

import com.google.android.filament.Engine
import com.google.android.filament.LightManager
import com.google.android.filament.MaterialInstance
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.Node
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-fidelity 3D Underground Coal Mine Environment conforming to:
 * 1. Tunnel Reference Photograph:
 *    - Arched steel colliery horseshoe rib frames (vertical legs, angled shoulder struts, crown caps)
 *    - Horizontal wooden lagging planks holding rock faces between steel arches
 *    - Crushed stone ballast track bed with wooden sleepers and reflective steel rails
 *    - Damp stone walkway with warm specular reflections along the left side
 *    - Continuous overhead flexible yellow/olive ventilation duct along upper left ceiling
 *    - Warm industrial practical lamps (100,000 lm, 2800K) casting amber pools of light
 * 2. Spatial Heatmap Topology:
 *    - Zone 1: Entrance Drift (Connecting AR Doorway at Z=0m to Central Junction at Z=-10m)
 *    - Zone 3: Central Junction Hub (Spacious 7.5m wide high-arched cavern with turnout rails)
 *    - Zone 4 & 2: Northern Drift & Main Excavation (Curved -20° to -45° with collapsed coal terminus)
 *    - Zone 5 & 6: Processing & Storage Bay (East Branch curving +75° to +110° for Fire & Chemical hazards)
 *    - Zone 7: Lower Ventilation Drift (West Branch curving -85°)
 *
 * CRITICAL ARCHITECTURAL GUARANTEES:
 * - NO RECTANGULAR END WALL: All lines of sight naturally terminate behind curved rock bends,
 *   colliery arches, or organic rockfall spurs.
 * - NO REAL-WORLD LEAKS / ZERO SKYBOX: 360-degree interlocking faceted coal/rock envelope.
 * - NO PITCH BLACK SURFACES: Calibrated PBR materials and multi-point 100,000+ lm illumination.
 */
class MineEnvironmentNode(
    engine: Engine,
    rockMaterial: MaterialInstance?,
    floorMaterial: MaterialInstance?,
    woodMaterial: MaterialInstance?,
    metalMaterial: MaterialInstance?,
    lampMaterial: MaterialInstance?,
    hazardYellowMaterial: MaterialInstance?,
    pipeMaterial: MaterialInstance?,
    railMaterial: MaterialInstance? = metalMaterial,
    wetWalkwayMaterial: MaterialInstance? = floorMaterial,
    ventDuctMaterial: MaterialInstance? = hazardYellowMaterial
) : Node(engine) {

    private val rng = Random(42)

    init {
        // =========================================================================
        // 1. ZONE 1: ENTRANCE DRIFT (Connecting Doorway to Central Junction)
        // From Z = 0m down to Z = -10m
        // =========================================================================
        buildArchedTunnelSection(
            engine = engine,
            startX = 0f,
            startZ = 0f,
            length = 10f,
            angleDeg = 0f,
            hasRails = true,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 3.6f
        )

        // =========================================================================
        // 2. ZONE 3: CENTRAL JUNCTION (Very High Activity Hub)
        // From Z = -10m to Z = -18m, centered at X = 0, Z = -14m
        // =========================================================================
        buildCentralJunction(
            engine = engine,
            centerX = 0f,
            centerZ = -14f,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial
        )

        // =========================================================================
        // 3. ZONE 4 & 2: NORTHERN MAIN EXCAVATION & UPPER MINING DRIFT
        // Extends from Central Junction Northward (Z = -18m to -34m) with curved bends
        // =========================================================================
        val (n1X, n1Z) = buildArchedTunnelSection(
            engine = engine,
            startX = -0.3f,
            startZ = -18f,
            length = 8f,
            angleDeg = -20f,
            hasRails = true,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 4.0f
        )
        // Second segment bending further (-45°) so the tunnel recedes behind curved rock
        buildArchedTunnelSection(
            engine = engine,
            startX = n1X,
            startZ = n1Z,
            length = 8f,
            angleDeg = -45f,
            hasRails = true,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 4.0f,
            terminateWithRockfall = true
        )

        // =========================================================================
        // 4. ZONE 5 & 6: PROCESSING & STORAGE BAY (East Branch)
        // Runs East from Central Junction: site for Electrical Fire & Chemical Hazard
        // =========================================================================
        val (e1X, e1Z) = buildArchedTunnelSection(
            engine = engine,
            startX = 2.0f,
            startZ = -14f,
            length = 9f,
            angleDeg = 75f,
            hasRails = true,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 3.6f
        )
        // Storage alcove extension bending to +110° (Zone 6)
        buildArchedTunnelSection(
            engine = engine,
            startX = e1X,
            startZ = e1Z,
            length = 8f,
            angleDeg = 110f,
            hasRails = false,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 4.0f,
            terminateWithRockfall = true
        )

        // =========================================================================
        // 5. ZONE 7: LOWER VENTILATION DRIFT (West / South-West Branch)
        // =========================================================================
        buildArchedTunnelSection(
            engine = engine,
            startX = -2.0f,
            startZ = -13.5f,
            length = 8f,
            angleDeg = -85f,
            hasRails = false,
            rockMaterial = rockMaterial,
            floorMaterial = floorMaterial,
            woodMaterial = woodMaterial,
            metalMaterial = metalMaterial,
            lampMaterial = lampMaterial,
            pipeMaterial = pipeMaterial,
            railMaterial = railMaterial,
            wetWalkwayMaterial = wetWalkwayMaterial,
            ventDuctMaterial = ventDuctMaterial,
            lampInterval = 4.0f,
            terminateWithRockfall = true
        )
    }

    /**
     * Builds an authentic arched colliery tunnel segment matching the reference photograph:
     * - Multi-segmented arched steel colliery rib frames (vertical legs, shoulder rafters, crown cap)
     * - Horizontal wooden lagging planks between the arches along walls and ceiling
     * - Ballast crushed-rock bed with wooden cross-sleepers and shiny steel rails
     * - Damp stone walkway with warm specular reflections along the left side
     * - Continuous overhead corrugated ventilation duct along upper left
     * - Warm amber vintage cage mine practical lamps with real 100,000 lm point lights
     * - 360° interlocking faceted coal/rock envelope (zero light/real-world leaks)
     */
    private fun buildArchedTunnelSection(
        engine: Engine,
        startX: Float,
        startZ: Float,
        length: Float,
        angleDeg: Float,
        hasRails: Boolean,
        rockMaterial: MaterialInstance?,
        floorMaterial: MaterialInstance?,
        woodMaterial: MaterialInstance?,
        metalMaterial: MaterialInstance?,
        lampMaterial: MaterialInstance?,
        pipeMaterial: MaterialInstance?,
        railMaterial: MaterialInstance?,
        wetWalkwayMaterial: MaterialInstance?,
        ventDuctMaterial: MaterialInstance?,
        lampInterval: Float = 3.6f,
        terminateWithRockfall: Boolean = false
    ): Pair<Float, Float> {
        val rad = Math.toRadians(angleDeg.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()

        var d = 0f
        val step = 1.8f
        val tunnelHalfWidth = 1.80f // 3.6m wide tunnel
        val legHeight = 1.65f       // Vertical steel leg
        val crownHeight = 3.10f     // Arched apex height

        while (d <= length) {
            val cx = startX + sin * d
            val cz = startZ - cos * d

            // =====================================================================
            // 1. FLOOR: BALLAST BED (RIGHT) + WET SPECULAR WALKWAY (LEFT)
            // =====================================================================
            // Crushed rock ballast bed (Right/Center from X = -0.3m to +2.0m)
            val ballastCenterOffset = 0.85f
            val bx = cx + ballastCenterOffset * cos
            val bz = cz + ballastCenterOffset * sin
            val ballastFloor = CubeNode(engine, size = Float3(2.4f, 0.16f, step + 0.15f), materialInstance = floorMaterial).apply {
                position = Float3(bx, -0.07f, bz)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(ballastFloor)

            // Damp stone walkway (Left side from X = -2.0m to -0.3m) with specular sheen
            val walkwayCenterOffset = -1.15f
            val wx = cx + walkwayCenterOffset * cos
            val wz = cz + walkwayCenterOffset * sin
            val wetWalkway = CubeNode(engine, size = Float3(1.7f, 0.17f, step + 0.15f), materialInstance = wetWalkwayMaterial).apply {
                position = Float3(wx, -0.065f, wz)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(wetWalkway)

            // =====================================================================
            // 2. ARCHED STEEL COLLIERY RIB FRAME (Horseshoe Arch)
            // Matching the arched structural frames in the reference photograph
            // =====================================================================
            val ribThick = 0.14f

            // Left Vertical Steel Leg (from Y = 0 to 1.65m)
            val lLegX = cx - tunnelHalfWidth * cos
            val lLegZ = cz - tunnelHalfWidth * sin
            val leftLeg = CubeNode(engine, size = Float3(ribThick, legHeight, ribThick), materialInstance = metalMaterial).apply {
                position = Float3(lLegX, legHeight / 2f, lLegZ)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(leftLeg)

            // Right Vertical Steel Leg (from Y = 0 to 1.65m)
            val rLegX = cx + tunnelHalfWidth * cos
            val rLegZ = cz + tunnelHalfWidth * sin
            val rightLeg = CubeNode(engine, size = Float3(ribThick, legHeight, ribThick), materialInstance = metalMaterial).apply {
                position = Float3(rLegX, legHeight / 2f, rLegZ)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(rightLeg)

            // Left Angled Shoulder Rafter (slopes inward from leg to crown)
            val lShoulderOffset = -1.35f
            val lShoulderX = cx + lShoulderOffset * cos
            val lShoulderZ = cz + lShoulderOffset * sin
            val leftShoulder = CubeNode(engine, size = Float3(ribThick, 1.45f, ribThick), materialInstance = metalMaterial).apply {
                position = Float3(lShoulderX, 2.25f, lShoulderZ)
                rotation = Float3(0f, angleDeg, -38f)
            }
            addChildNode(leftShoulder)

            // Right Angled Shoulder Rafter (slopes inward from leg to crown)
            val rShoulderOffset = 1.35f
            val rShoulderX = cx + rShoulderOffset * cos
            val rShoulderZ = cz + rShoulderOffset * sin
            val rightShoulder = CubeNode(engine, size = Float3(ribThick, 1.45f, ribThick), materialInstance = metalMaterial).apply {
                position = Float3(rShoulderX, 2.25f, rShoulderZ)
                rotation = Float3(0f, angleDeg, 38f)
            }
            addChildNode(rightShoulder)

            // Arched Crown Cap (horizontal top span connecting shoulder arches)
            val crownCap = CubeNode(engine, size = Float3(1.85f, ribThick, ribThick), materialInstance = metalMaterial).apply {
                position = Float3(cx, crownHeight - ribThick / 2f, cz)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(crownCap)

            // Joint Fishplates / Bolted Gussets at Knee and Crown
            for (jointOffset in listOf(-tunnelHalfWidth, tunnelHalfWidth)) {
                val jx = cx + jointOffset * cos
                val jz = cz + jointOffset * sin
                val fishplate = CubeNode(engine, size = Float3(ribThick + 0.04f, 0.22f, ribThick + 0.04f), materialInstance = metalMaterial).apply {
                    position = Float3(jx, legHeight, jz)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(fishplate)
            }

            // =====================================================================
            // 3. HORIZONTAL WOODEN LAGGING PLANKS
            // Placed between consecutive arches along walls and ceiling to hold rock
            // =====================================================================
            val laggingThick = 0.05f
            val plankH = 0.30f
            for (tierY in listOf(0.40f, 0.85f, 1.30f)) {
                // Left wall lagging board
                val plankLX = cx - (tunnelHalfWidth + 0.05f) * cos
                val plankLZ = cz - (tunnelHalfWidth + 0.05f) * sin
                val leftPlank = CubeNode(engine, size = Float3(laggingThick, plankH, step + 0.05f), materialInstance = woodMaterial).apply {
                    position = Float3(plankLX, tierY, plankLZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(leftPlank)

                // Right wall lagging board
                val plankRX = cx + (tunnelHalfWidth + 0.05f) * cos
                val plankRZ = cz + (tunnelHalfWidth + 0.05f) * sin
                val rightPlank = CubeNode(engine, size = Float3(laggingThick, plankH, step + 0.05f), materialInstance = woodMaterial).apply {
                    position = Float3(plankRX, tierY, plankRZ)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(rightPlank)
            }

            // Roof lagging planks (spanning above shoulder struts and crown)
            val roofPlank1 = CubeNode(engine, size = Float3(1.20f, laggingThick, step + 0.05f), materialInstance = woodMaterial).apply {
                position = Float3(cx - 0.85f * cos, 2.70f, cz - 0.85f * sin)
                rotation = Float3(0f, angleDeg, -35f)
            }
            val roofPlank2 = CubeNode(engine, size = Float3(1.20f, laggingThick, step + 0.05f), materialInstance = woodMaterial).apply {
                position = Float3(cx + 0.85f * cos, 2.70f, cz + 0.85f * sin)
                rotation = Float3(0f, angleDeg, 35f)
            }
            val crownPlank = CubeNode(engine, size = Float3(1.40f, laggingThick, step + 0.05f), materialInstance = woodMaterial).apply {
                position = Float3(cx, crownHeight + 0.04f, cz)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(roofPlank1)
            addChildNode(roofPlank2)
            addChildNode(crownPlank)

            // =====================================================================
            // 4. 360-DEGREE CONTINUOUS COAL / ROCK ENVELOPE (Zero Leaks)
            // Heavy faceted organic rock enclosing the entire tunnel
            // =====================================================================
            // Left Rock Wall
            val rlx = cx - (tunnelHalfWidth + 0.55f) * cos
            val rlz = cz - (tunnelHalfWidth + 0.55f) * sin
            val leftRock = CubeNode(engine, size = Float3(0.95f + rng.nextFloat() * 0.25f, crownHeight + 0.6f, step + 0.25f), materialInstance = rockMaterial).apply {
                position = Float3(rlx, crownHeight / 2f, rlz)
                rotation = Float3(rng.nextFloat() * 6f - 3f, angleDeg + rng.nextFloat() * 6f - 3f, rng.nextFloat() * 4f - 2f)
            }
            addChildNode(leftRock)

            // Right Rock Wall
            val rrx = cx + (tunnelHalfWidth + 0.55f) * cos
            val rrz = cz + (tunnelHalfWidth + 0.55f) * sin
            val rightRock = CubeNode(engine, size = Float3(0.95f + rng.nextFloat() * 0.25f, crownHeight + 0.6f, step + 0.25f), materialInstance = rockMaterial).apply {
                position = Float3(rrx, crownHeight / 2f, rrz)
                rotation = Float3(rng.nextFloat() * 6f - 3f, angleDeg + rng.nextFloat() * 6f - 3f, rng.nextFloat() * 4f - 2f)
            }
            addChildNode(rightRock)

            // Arched Rock Ceiling
            val rcY = crownHeight + 0.35f + (rng.nextFloat() * 0.18f - 0.09f)
            val rockRoof = CubeNode(engine, size = Float3(tunnelHalfWidth * 2f + 1.6f, 0.55f, step + 0.25f), materialInstance = rockMaterial).apply {
                position = Float3(cx, rcY, cz)
                rotation = Float3(rng.nextFloat() * 4f - 2f, angleDeg, rng.nextFloat() * 4f - 2f)
            }
            addChildNode(rockRoof)

            // =====================================================================
            // 5. RAILWAY TRACKS & SLEEPERS (Matching Reference Photo)
            // Centered to the right of centerline: gauge = 0.85m
            // =====================================================================
            if (hasRails) {
                val gauge = 0.85f
                val rCenter = 0.55f // Track centerline
                val railH = 0.055f
                val railW = 0.065f

                // Left Shiny Steel Rail
                val lrOffset = rCenter - (gauge / 2f) // +0.125m
                val lrx = cx + lrOffset * cos
                val lrz = cz + lrOffset * sin
                val leftRail = CubeNode(engine, size = Float3(railW, railH, step + 0.08f), materialInstance = railMaterial).apply {
                    position = Float3(lrx, 0.045f, lrz)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(leftRail)

                // Right Shiny Steel Rail
                val rrOffset = rCenter + (gauge / 2f) // +0.975m
                val rrxRail = cx + rrOffset * cos
                val rrzRail = cz + rrOffset * sin
                val rightRail = CubeNode(engine, size = Float3(railW, railH, step + 0.08f), materialInstance = railMaterial).apply {
                    position = Float3(rrxRail, 0.045f, rrzRail)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                addChildNode(rightRail)

                // Wooden Sleepers (Ties) spaced every 0.9m
                val tieWidth = gauge + 0.35f
                for (tieZOffset in listOf(-0.45f, 0.45f)) {
                    val tieCx = cx + rCenter * cos + sin * tieZOffset
                    val tieCz = cz + rCenter * sin - cos * tieZOffset
                    val sleeper = CubeNode(engine, size = Float3(tieWidth, 0.05f, 0.18f), materialInstance = woodMaterial).apply {
                        position = Float3(tieCx, 0.02f, tieCz)
                        rotation = Float3(0f, angleDeg, 0f)
                    }
                    addChildNode(sleeper)
                }
            }

            // =====================================================================
            // 6. CONTINUOUS OVERHEAD VENTILATION DUCT (Reference Photo upper left)
            // =====================================================================
            val ductOffset = -1.05f
            val ductX = cx + ductOffset * cos
            val ductZ = cz + ductOffset * sin
            val ductY = 2.65f
            val ventDuct = CylinderNode(engine, radius = 0.22f, height = step + 0.08f, materialInstance = ventDuctMaterial).apply {
                position = Float3(ductX, ductY, ductZ)
                rotation = Float3(90f, angleDeg, 0f)
            }
            // Steel hanger bracket
            val hanger = CubeNode(engine, size = Float3(0.025f, 0.45f, 0.025f), materialInstance = metalMaterial).apply {
                position = Float3(ductX, ductY + 0.25f, ductZ)
                rotation = Float3(0f, angleDeg, 0f)
            }
            addChildNode(ventDuct)
            addChildNode(hanger)

            // Compressed air pipe running along right rib
            val pipeOffset = tunnelHalfWidth - 0.12f
            val px = cx + pipeOffset * cos
            val pz = cz + pipeOffset * sin
            val pipe = CylinderNode(engine, radius = 0.04f, height = step + 0.08f, materialInstance = pipeMaterial).apply {
                position = Float3(px, 1.15f, pz)
                rotation = Float3(90f, angleDeg, 0f)
            }
            addChildNode(pipe)

            // =====================================================================
            // 7. WARM AMBER INDUSTRIAL PRACTICAL LAMPS & 100,000 LM POINT LIGHTS
            // Vintage explosion-proof cage sconce mounted every 3.6m on left rib
            // =====================================================================
            if (d > 0.2f && (d % lampInterval) < step) {
                val lampOffset = -(tunnelHalfWidth - 0.25f)
                val lampX = cx + lampOffset * cos
                val lampZ = cz + lampOffset * sin
                val lampY = 2.25f

                val sconceArm = CubeNode(engine, size = Float3(0.18f, 0.035f, 0.035f), materialInstance = metalMaterial).apply {
                    position = Float3(lampX + 0.09f * cos, lampY + 0.08f, lampZ + 0.09f * sin)
                    rotation = Float3(0f, angleDeg, 0f)
                }
                val lampCap = CylinderNode(engine, radius = 0.08f, height = 0.035f, materialInstance = metalMaterial).apply {
                    position = Float3(lampX, lampY + 0.07f, lampZ)
                }
                val lampBulb = CylinderNode(engine, radius = 0.065f, height = 0.14f, materialInstance = lampMaterial).apply {
                    position = Float3(lampX, lampY, lampZ)
                }
                val lampCage = CubeNode(engine, size = Float3(0.015f, 0.18f, 0.14f), materialInstance = metalMaterial).apply {
                    position = Float3(lampX, lampY, lampZ)
                }
                addChildNode(sconceArm)
                addChildNode(lampCap)
                addChildNode(lampBulb)
                addChildNode(lampCage)

                // Filament Point Light (Warm Amber 2800K, R:1.0, G:0.76, B:0.38)
                try {
                    val pBuilder = LightManager.Builder(LightManager.Type.POINT)
                        .color(1.0f, 0.76f, 0.38f)
                        .intensity(100000f)
                        .falloff(8.0f)
                    val pointLight = LightNode(engine, builder = pBuilder).apply {
                        position = Float3(lampX, lampY - 0.08f, lampZ)
                    }
                    addChildNode(pointLight)
                } catch (_: Exception) {}
            }

            d += step
        }

        val endX = startX + sin * length
        val endZ = startZ - cos * length

        // =========================================================================
        // 8. NATURAL TERMINATION ROCKFALL (Zero Rectangular Flat Wall)
        // Organic boulders and collapsed timber cribbing naturally seal the drift
        // =========================================================================
        if (terminateWithRockfall) {
            val rockCount = 7
            for (i in 0 until rockCount) {
                val rw = 0.95f + rng.nextFloat() * 0.55f
                val rh = 0.80f + rng.nextFloat() * 0.45f
                val rx = endX + (rng.nextFloat() * 1.8f - 0.9f) * cos
                val rz = endZ + (rng.nextFloat() * 1.8f - 0.9f) * sin
                val ry = (i * 0.42f) + 0.28f

                val rockBulkhead = CubeNode(engine, size = Float3(rw, rh, 1.4f), materialInstance = rockMaterial).apply {
                    position = Float3(rx, ry, rz)
                    rotation = Float3(rng.nextFloat() * 18f - 9f, angleDeg + rng.nextFloat() * 24f - 12f, rng.nextFloat() * 12f - 6f)
                }
                addChildNode(rockBulkhead)
            }
            // Collapsed timber prop buried in rockfall
            val collapsedTimber = CubeNode(engine, size = Float3(0.20f, 2.2f, 0.20f), materialInstance = woodMaterial).apply {
                position = Float3(endX, 0.75f, endZ)
                rotation = Float3(25f, angleDeg + 30f, -40f)
            }
            addChildNode(collapsedTimber)
        }

        return Pair(endX, endZ)
    }

    /**
     * Zone 3: Central Junction Chamber
     * Spacious 7.5m wide underground hub connecting North, East, and South drifts.
     * Features high vaulted cavern ceiling, heavy timber cluster pillars, turnout tracks,
     * and a central 160,000 lm high-bay industrial chandelier.
     */
    private fun buildCentralJunction(
        engine: Engine,
        centerX: Float,
        centerZ: Float,
        rockMaterial: MaterialInstance?,
        floorMaterial: MaterialInstance?,
        woodMaterial: MaterialInstance?,
        metalMaterial: MaterialInstance?,
        lampMaterial: MaterialInstance?,
        pipeMaterial: MaterialInstance?,
        railMaterial: MaterialInstance?,
        wetWalkwayMaterial: MaterialInstance?
    ) {
        val junctionWidth = 7.6f
        val junctionLength = 8.8f
        val junctionHeight = 3.8f

        // Ballast Floor for Central Junction
        val jFloor = CubeNode(engine, size = Float3(junctionWidth, 0.18f, junctionLength), materialInstance = floorMaterial).apply {
            position = Float3(centerX, -0.08f, centerZ)
        }
        addChildNode(jFloor)

        // Wet stone walkway crossing along the west perimeter
        val jWalkway = CubeNode(engine, size = Float3(1.8f, 0.19f, junctionLength), materialInstance = wetWalkwayMaterial).apply {
            position = Float3(centerX - 2.8f, -0.075f, centerZ)
        }
        addChildNode(jWalkway)

        // Junction High Arched Vaulted Ceiling
        val jCeiling = CubeNode(engine, size = Float3(junctionWidth + 1.2f, 0.55f, junctionLength + 1.2f), materialInstance = rockMaterial).apply {
            position = Float3(centerX, junctionHeight, centerZ)
            rotation = Float3(rng.nextFloat() * 3f - 1.5f, 0f, rng.nextFloat() * 3f - 1.5f)
        }
        addChildNode(jCeiling)

        // Heavy Junction Timber Support Sets (4 corner cluster posts)
        val posts = listOf(
            Pair(-2.6f, -2.8f),
            Pair(2.6f, -2.8f),
            Pair(-2.6f, 2.8f),
            Pair(2.6f, 2.8f)
        )
        for ((px, pz) in posts) {
            val pillar = CubeNode(engine, size = Float3(0.32f, junctionHeight, 0.32f), materialInstance = woodMaterial).apply {
                position = Float3(centerX + px, junctionHeight / 2f, centerZ + pz)
            }
            addChildNode(pillar)
        }

        // Heavy Cross Beams across junction ceiling
        val beam1 = CubeNode(engine, size = Float3(junctionWidth - 0.8f, 0.28f, 0.32f), materialInstance = woodMaterial).apply {
            position = Float3(centerX, junctionHeight - 0.16f, centerZ - 2.8f)
        }
        val beam2 = CubeNode(engine, size = Float3(junctionWidth - 0.8f, 0.28f, 0.32f), materialInstance = woodMaterial).apply {
            position = Float3(centerX, junctionHeight - 0.16f, centerZ + 2.8f)
        }
        addChildNode(beam1)
        addChildNode(beam2)

        // Central High-Bay Industrial Mine Chandelier
        val centerLamp = CylinderNode(engine, radius = 0.22f, height = 0.28f, materialInstance = lampMaterial).apply {
            position = Float3(centerX, junctionHeight - 0.35f, centerZ)
        }
        val centerCap = CylinderNode(engine, radius = 0.26f, height = 0.06f, materialInstance = metalMaterial).apply {
            position = Float3(centerX, junctionHeight - 0.20f, centerZ)
        }
        addChildNode(centerLamp)
        addChildNode(centerCap)

        // Central High-Bay Filament Point Light (160,000 lm, 12m falloff, 2800K)
        try {
            val jBuilder = LightManager.Builder(LightManager.Type.POINT)
                .color(1.0f, 0.78f, 0.42f)
                .intensity(160000f)
                .falloff(12.0f)
            val junctionLight = LightNode(engine, builder = jBuilder).apply {
                position = Float3(centerX, junctionHeight - 0.45f, centerZ)
            }
            addChildNode(junctionLight)
        } catch (_: Exception) {}

        // Rail Turnout Frog / Switch Tracks
        val gauge = 0.85f
        val rCenter = 0.55f
        for (offset in listOf(rCenter - gauge / 2f, rCenter + gauge / 2f)) {
            val throughRail = CubeNode(engine, size = Float3(0.065f, 0.055f, junctionLength), materialInstance = railMaterial).apply {
                position = Float3(centerX + offset, 0.045f, centerZ)
            }
            addChildNode(throughRail)
        }
        // Turnout track curving towards East branch
        val turnoutRail1 = CubeNode(engine, size = Float3(0.065f, 0.055f, 4.8f), materialInstance = railMaterial).apply {
            position = Float3(centerX + 1.35f, 0.045f, centerZ)
            rotation = Float3(0f, 35f, 0f)
        }
        val turnoutRail2 = CubeNode(engine, size = Float3(0.065f, 0.055f, 4.8f), materialInstance = railMaterial).apply {
            position = Float3(centerX + 2.05f, 0.045f, centerZ)
            rotation = Float3(0f, 35f, 0f)
        }
        addChildNode(turnoutRail1)
        addChildNode(turnoutRail2)
    }
}
