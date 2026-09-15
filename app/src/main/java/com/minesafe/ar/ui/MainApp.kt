package com.minesafe.ar.ui

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult as ArHitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.minesafe.ar.R
import com.minesafe.ar.ar.ChemicalHazardNode
import com.minesafe.ar.ar.DoorwayNode
import com.minesafe.ar.ar.EmergencyStationNode
import com.minesafe.ar.ar.ExtinguisherNode
import com.minesafe.ar.ar.FireNode
import com.minesafe.ar.ar.MineEnvironmentNode
import com.minesafe.ar.ar.PlacementReticleNode
import com.minesafe.ar.audio.AudioManager
import com.minesafe.ar.audio.VoiceInstructionManager
import com.minesafe.ar.training.TrainingModule
import com.minesafe.ar.training.TrainingState
import com.minesafe.ar.training.TrainingViewModel
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.dot
import dev.romainguy.kotlin.math.normalize
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.collision.HitResult
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MainApp(
    viewModel: TrainingViewModel,
    voiceManager: VoiceInstructionManager,
    audioManager: AudioManager,
    onTrainingComplete: () -> Unit
) {
    val selectedModule by viewModel.selectedModule.collectAsState()
    val currentState by viewModel.currentState.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeSeconds by viewModel.timeSeconds.collectAsState()
    val isSoundMuted by viewModel.isSoundMuted.collectAsState()
    val nozzleReminderCount by viewModel.nozzleReminderTrigger.collectAsState()

    var showHelpModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)

    // Calibrated PBR Materials for realistic coal mine rendering (Matching Reference Photograph)
    val rockMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF827161), 0.0f, 0.65f, 0.40f) }
    val floorMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF6E5F52), 0.0f, 0.80f, 0.35f) }
    val woodMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF7E5E44), 0.0f, 0.55f, 0.40f) }
    val darkMetalMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF615B56), 0.70f, 0.40f, 0.60f) }
    val railMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFCFD8DC), 0.92f, 0.18f, 0.88f) }
    val amberLampMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFCA28), 0.0f, 0.10f, 1.0f) }
    val signMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFD54F), 0.40f, 0.30f, 0.80f) }
    val reticleWhiteMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFFFFF), 0.0f, 0.10f, 1.0f) }
    val reticleCyanMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF00E5FF), 0.0f, 0.10f, 1.0f) }
    val hazardYellowMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFD600), 0.1f, 0.40f, 0.4f) }
    val hazardBandMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF263238), 0.1f, 0.50f, 0.3f) }
    val pipeMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF546E7A), 0.75f, 0.35f, 0.5f) }
    val fireCoreMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFF9C4), 0.0f, 0.05f, 1.0f) }
    val fireOuterMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFF3D00), 0.0f, 0.10f, 1.0f) }
    val smokeMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF455A64), 0.0f, 0.85f, 0.10f) }
    val extRedMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFE53935), 0.15f, 0.30f, 0.4f) }
    val brassMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFCA28), 0.85f, 0.25f, 0.7f) }
    val rubberMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF263238), 0.0f, 0.75f, 0.2f) }
    val sprayMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFECEFF1), 0.0f, 0.80f, 0.2f) }
    val greenGaugeMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF00E676), 0.0f, 0.20f, 0.6f) }
    val chemVaporMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFCCFF90), 0.0f, 0.10f, 0.8f) }
    val chemPuddleMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF76FF03), 0.1f, 0.15f, 0.9f) }
    val greenStationMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF2E7D32), 0.1f, 0.40f, 0.4f) }
    val whiteMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFFFFF), 0.0f, 0.30f, 0.5f) }
    val wetWalkwayMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF756555), 0.05f, 0.22f, 0.78f) }
    val ventDuctMat = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFA59B44), 0.05f, 0.50f, 0.45f) }

    // White AR Placement Reticle (Reference Image 2)
    val reticleNode = remember(engine, reticleWhiteMat) {
        PlacementReticleNode(engine, reticleWhiteMat).apply {
            isVisible = false
        }
    }

    var currentFloorHit by remember { mutableStateOf<ArHitResult?>(null) }
    var childNodes by remember { mutableStateOf(listOf<Node>(reticleNode)) }
    var currentFrame by remember { mutableStateOf<Frame?>(null) }
    var doorwayAnchor by remember { mutableStateOf<Anchor?>(null) }
    var doorDistance by remember { mutableFloatStateOf(-1f) }
    var trackingLost by remember { mutableStateOf(false) }

    // Hazard and Equipment Node references
    var fireNode by remember { mutableStateOf<FireNode?>(null) }
    var extinguisherNode by remember { mutableStateOf<ExtinguisherNode?>(null) }
    var chemicalNode by remember { mutableStateOf<ChemicalHazardNode?>(null) }
    var emergencyStationNode by remember { mutableStateOf<EmergencyStationNode?>(null) }

    var isAimingAtFire by remember { mutableStateOf(false) }
    var dangerZoneWarningTriggered by remember { mutableStateOf(false) }

    // Primary Directional Fill Light (180,000 lx angled down the mine tunnel)
    val mainLight = rememberMainLightNode(engine) {
        color = Float4(1.0f, 0.94f, 0.82f, 1.0f)
        intensity = 180000f
        lightDirection = Float3(0.15f, -0.75f, -0.65f)
    }

    // Periodic nozzle reminder reaction
    LaunchedEffect(nozzleReminderCount) {
        if (nozzleReminderCount > 0 && currentState == TrainingState.EXTINGUISHER_REACHED) {
            voiceManager.speak("Please open the extinguisher nozzle.")
        }
    }

    // Voice instruction and sound reaction based on state machine changes
    LaunchedEffect(currentState) {
        when (currentState) {
            TrainingState.START -> {
                voiceManager.speak("Welcome to MineSafe AR. Scan the floor and tap to place the doorway.")
            }
            TrainingState.PLACE_DOORWAY -> {
                voiceManager.speak("Scan the floor and tap to place the doorway.")
            }
            TrainingState.ENTER_MINE -> {
                audioManager.startMineAmbiance()
                voiceManager.speak("Physically walk toward the doorway to enter the mine.")
            }
            // Module 1: Electrical Fire
            TrainingState.FIRE_DETECTED -> {
                audioManager.startFireSound()
                voiceManager.speak("Electrical fire detected. Move toward the fire extinguisher.")
            }
            TrainingState.EXTINGUISHER_REACHED -> {
                audioManager.playSuccessChime()
                voiceManager.speak("Fire extinguisher reached. Open the extinguisher nozzle.")
            }
            TrainingState.OPEN_NOZZLE -> {
                audioManager.playTap()
                voiceManager.speak("Aim the extinguisher toward the base of the fire.")
            }
            TrainingState.AIM_AT_FIRE -> {
                voiceManager.speak("Press to discharge the extinguisher.")
            }
            TrainingState.DISCHARGE_EXTINGUISHER -> {
                audioManager.playExtinguisherDischarge()
            }
            TrainingState.FIRE_EXTINGUISHED -> {
                audioManager.stopHazardSound()
                audioManager.playSuccessChime()
                voiceManager.speak("Good job. The fire has been extinguished.")
                delay(3000)
                onTrainingComplete()
            }
            // Module 2: Chemical Hazard
            TrainingState.CHEMICAL_HAZARD_DETECTED -> {
                audioManager.startChemicalLeakSound()
                voiceManager.speak("Chemical hazard detected. Do not approach the leak.")
            }
            TrainingState.MAINTAIN_SAFE_DISTANCE -> {
                voiceManager.speak("Move away from the hazard zone.")
            }
            TrainingState.LEAVE_HAZARD_ZONE -> {
                audioManager.playSuccessChime()
                voiceManager.speak("Locate the emergency response equipment.")
            }
            TrainingState.LOCATE_EMERGENCY_EQUIPMENT -> {
                voiceManager.speak("Do not touch the leaking container. Follow the emergency procedure.")
            }
            TrainingState.PERFORM_SAFE_RESPONSE -> {
                voiceManager.speak("Activate the emergency isolation valve to seal the line.")
            }
            TrainingState.HAZARD_CONTROLLED -> {
                audioManager.stopHazardSound()
                audioManager.playSuccessChime()
                voiceManager.speak("Hazard controlled. Emergency isolation confirmed.")
                delay(3000)
                onTrainingComplete()
            }
            else -> {}
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cx = if (constraints.maxWidth > 0) constraints.maxWidth.toFloat() / 2f else 540f
        val cy = if (constraints.maxHeight > 0) constraints.maxHeight.toFloat() / 2f else 960f

        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            mainLightNode = mainLight,
            childNodes = childNodes,
            planeRenderer = (doorwayAnchor == null),
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.focusMode = Config.FocusMode.AUTO
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onTouchEvent = { e: MotionEvent, hitResult: HitResult? ->
                if (e.action == MotionEvent.ACTION_UP) {
                    if (doorwayAnchor == null && (currentState == TrainingState.START || currentState == TrainingState.SCAN_FLOOR || currentState == TrainingState.PLACE_DOORWAY)) {
                        val tapHit = currentFrame?.hitTest(e.x, e.y)?.firstOrNull { hit ->
                            val trackable = hit.trackable
                            trackable is Plane &&
                            trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                            trackable.trackingState == TrackingState.TRACKING &&
                            trackable.isPoseInPolygon(hit.hitPose)
                        } ?: currentFrame?.hitTest(e.x, e.y)?.firstOrNull { hit ->
                            val trackable = hit.trackable
                            trackable is Plane &&
                            trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                            trackable.trackingState == TrackingState.TRACKING
                        } ?: currentFloorHit

                        if (tapHit != null) {
                            val anchor = tapHit.createAnchorOrNull()
                            if (anchor != null) {
                                doorwayAnchor = anchor
                                reticleNode.isVisible = false
                                val anchorNode = AnchorNode(engine, anchor)

                                // 1. Realistic 3D Coal Mine Doorway (Matching Reference Image 2)
                                val doorway = DoorwayNode(
                                    engine = engine,
                                    rockMaterial = rockMat,
                                    woodMaterial = woodMat,
                                    metalMaterial = darkMetalMat,
                                    lampEmissiveMaterial = amberLampMat,
                                    signMaterial = signMat
                                )
                                anchorNode.addChildNode(doorway)

                                // 2. Modular Underground Mine Environment (Heatmap topology)
                                val mineEnv = MineEnvironmentNode(
                                    engine = engine,
                                    rockMaterial = rockMat,
                                    floorMaterial = floorMat,
                                    woodMaterial = woodMat,
                                    metalMaterial = darkMetalMat,
                                    lampMaterial = amberLampMat,
                                    hazardYellowMaterial = hazardYellowMat,
                                    pipeMaterial = pipeMat,
                                    railMaterial = railMat,
                                    wetWalkwayMaterial = wetWalkwayMat,
                                    ventDuctMaterial = ventDuctMat
                                )
                                anchorNode.addChildNode(mineEnv)

                                // 3. Spawning Hazard & Equipment by Module
                                if (selectedModule == TrainingModule.ELECTRICAL_FIRE) {
                                    val fNode = FireNode(
                                        engine = engine,
                                        cabinetMaterial = darkMetalMat,
                                        fireCoreMaterial = fireCoreMat,
                                        fireOuterMaterial = fireOuterMat,
                                        smokeMaterial = smokeMat,
                                        warningSignMaterial = hazardYellowMat
                                    ).apply {
                                        position = Float3(0.0f, 0.0f, -10.0f)
                                    }
                                    fireNode = fNode
                                    anchorNode.addChildNode(fNode)

                                    val extNode = ExtinguisherNode(
                                        engine = engine,
                                        redBodyMaterial = extRedMat,
                                        metalMaterial = darkMetalMat,
                                        brassMaterial = brassMat,
                                        rubberMaterial = rubberMat,
                                        sprayMaterial = sprayMat,
                                        gaugeGreenMaterial = greenGaugeMat
                                    ).apply {
                                        position = Float3(0.45f, 0.0f, -9.2f)
                                    }
                                    extinguisherNode = extNode
                                    anchorNode.addChildNode(extNode)
                                } else {
                                    // Module 2: Chemical Hazard
                                    val cNode = ChemicalHazardNode(
                                        engine = engine,
                                        drumYellowMaterial = hazardYellowMat,
                                        hazardBandMaterial = hazardBandMat,
                                        vaporMaterial = chemVaporMat,
                                        puddleMaterial = chemPuddleMat,
                                        pipeMaterial = pipeMat,
                                        perimeterMaterial = hazardYellowMat
                                    ).apply {
                                        position = Float3(2.2f, 0.0f, -14.0f)
                                    }
                                    chemicalNode = cNode
                                    anchorNode.addChildNode(cNode)

                                    val eNode = EmergencyStationNode(
                                        engine = engine,
                                        boardMaterial = greenStationMat,
                                        cabinetMaterial = hazardYellowMat,
                                        valveMaterial = extRedMat,
                                        metalMaterial = darkMetalMat,
                                        whiteCrossMaterial = whiteMat
                                    ).apply {
                                        position = Float3(-1.8f, 0.0f, -13.5f)
                                    }
                                    emergencyStationNode = eNode
                                    anchorNode.addChildNode(eNode)
                                }

                                childNodes = (childNodes - reticleNode) + anchorNode
                                viewModel.updateState(TrainingState.ENTER_MINE)
                                return@ARScene true
                            }
                        }
                    }
                    when (currentState) {
                        TrainingState.EXTINGUISHER_REACHED -> {
                            // Tap on safety pin or nozzle
                            if (hitResult?.node == extinguisherNode?.safetyPin || hitResult?.node == extinguisherNode?.nozzle) {
                                extinguisherNode?.removeSafetyPin()
                                extinguisherNode?.openNozzle()
                                viewModel.updateState(TrainingState.OPEN_NOZZLE)
                                viewModel.updateState(TrainingState.AIM_AT_FIRE)
                            }
                        }
                        TrainingState.PERFORM_SAFE_RESPONSE -> {
                            if (hitResult?.node == emergencyStationNode?.isolationValve || hitResult?.node == emergencyStationNode?.isolationLever) {
                                emergencyStationNode?.activateValve()
                                chemicalNode?.setHazardControlled(true)
                                viewModel.updateState(TrainingState.HAZARD_CONTROLLED)
                            }
                        }
                        else -> {}
                    }
                }
                false
            },
            onSessionUpdated = { _, frame ->
                currentFrame = frame
                val mainHandler = Handler(Looper.getMainLooper())
                val isTrackingLost = frame.camera.trackingState != TrackingState.TRACKING

                mainHandler.post {
                    trackingLost = isTrackingLost
                }

                if (doorwayAnchor == null) {
                    val hits = frame.hitTest(cx, cy)
                    val floorHit = hits.firstOrNull { hit ->
                        val trackable = hit.trackable
                        trackable is Plane &&
                        trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        trackable.trackingState == TrackingState.TRACKING &&
                        trackable.isPoseInPolygon(hit.hitPose)
                    } ?: hits.firstOrNull { hit ->
                        val trackable = hit.trackable
                        trackable is Plane &&
                        trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        trackable.trackingState == TrackingState.TRACKING
                    } ?: frame.hitTest(cx, cy * 1.25f).firstOrNull { hit ->
                        val trackable = hit.trackable
                        trackable is Plane &&
                        trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        trackable.trackingState == TrackingState.TRACKING
                    }

                    if (floorHit != null) {
                        currentFloorHit = floorHit
                        val hitPose = floorHit.hitPose
                        reticleNode.position = Float3(hitPose.tx(), hitPose.ty(), hitPose.tz())
                        val camPose = frame.camera.pose
                        val dx = camPose.tx() - hitPose.tx()
                        val dz = camPose.tz() - hitPose.tz()
                        val yawDeg = Math.toDegrees(Math.atan2(dx.toDouble(), dz.toDouble())).toFloat()
                        reticleNode.rotation = Float3(0f, yawDeg, 0f)
                        reticleNode.isVisible = true
                    } else if (currentFloorHit == null) {
                        reticleNode.isVisible = false
                    }
                }

                val cameraPose = frame.camera.pose
                val dAnchor = doorwayAnchor

                if (dAnchor != null) {
                    val dx = cameraPose.tx() - dAnchor.pose.tx()
                    val dy = cameraPose.ty() - dAnchor.pose.ty()
                    val dz = cameraPose.tz() - dAnchor.pose.tz()
                    val distToDoor = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

                    mainHandler.post {
                        doorDistance = distToDoor
                    }

                    // --- ENTERING THE MINE DETECTION ---
                    if (currentState == TrainingState.ENTER_MINE && distToDoor <= 1.35f && distToDoor > 0.0f) {
                        mainHandler.post {
                            if (selectedModule == TrainingModule.ELECTRICAL_FIRE) {
                                viewModel.updateState(TrainingState.FIRE_DETECTED)
                            } else {
                                viewModel.updateState(TrainingState.CHEMICAL_HAZARD_DETECTED)
                            }
                        }
                    }

                    // --- MODULE 1: PROXIMITY & AIMING LOGIC ---
                    if (selectedModule == TrainingModule.ELECTRICAL_FIRE) {
                        val extPos = extinguisherNode?.worldPosition
                        if (extPos != null) {
                            val extDx = cameraPose.tx() - extPos.x
                            val extDz = cameraPose.tz() - extPos.z
                            val distToExt = Math.hypot(extDx.toDouble(), extDz.toDouble()).toFloat()

                            mainHandler.post {
                                viewModel.updateDistanceToObjective(distToExt)
                            }

                            if ((currentState == TrainingState.FIRE_DETECTED || currentState == TrainingState.GO_TO_EXTINGUISHER) && distToExt <= 1.55f) {
                                mainHandler.post {
                                    viewModel.updateState(TrainingState.EXTINGUISHER_REACHED)
                                }
                            }
                        }

                        // Check Aiming vector toward the fire
                        val fPos = fireNode?.worldPosition
                        if (fPos != null && (currentState == TrainingState.AIM_AT_FIRE || currentState == TrainingState.OPEN_NOZZLE)) {
                            val camPos = Float3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())
                            val camForward = Float3(cameraPose.zAxis[0], cameraPose.zAxis[1], cameraPose.zAxis[2]) * -1.0f
                            val toFire = normalize(fPos - camPos)
                            val aimCos = dot(camForward, toFire)

                            mainHandler.post {
                                isAimingAtFire = aimCos > 0.94f // within ~20 degrees
                            }
                        }
                    }

                    // --- MODULE 2: CHEMICAL HAZARD DISTANCE & ISOLATION LOGIC ---
                    if (selectedModule == TrainingModule.CHEMICAL_HAZARD) {
                        val cPos = chemicalNode?.worldPosition
                        if (cPos != null) {
                            val chemDx = cameraPose.tx() - cPos.x
                            val chemDz = cameraPose.tz() - cPos.z
                            val distToChem = Math.hypot(chemDx.toDouble(), chemDz.toDouble()).toFloat()

                            mainHandler.post {
                                viewModel.updateDistanceToObjective(distToChem)
                            }

                            // Safe distance check: Danger zone is 2.0 meters
                            if (distToChem < 2.0f && !dangerZoneWarningTriggered && currentState != TrainingState.HAZARD_CONTROLLED) {
                                dangerZoneWarningTriggered = true
                                mainHandler.post {
                                    audioManager.playMistakeBuzzer()
                                    viewModel.registerMistake(5)
                                    voiceManager.speak("Warning! Too close to chemical hazard. Step back immediately!")
                                }
                            } else if (distToChem >= 2.2f) {
                                dangerZoneWarningTriggered = false
                            }

                            if (currentState == TrainingState.CHEMICAL_HAZARD_DETECTED && distToChem >= 3.2f) {
                                mainHandler.post {
                                    viewModel.updateState(TrainingState.LEAVE_HAZARD_ZONE)
                                }
                            }
                        }

                        val ePos = emergencyStationNode?.worldPosition
                        if (ePos != null) {
                            val eDx = cameraPose.tx() - ePos.x
                            val eDz = cameraPose.tz() - ePos.z
                            val distToStation = Math.hypot(eDx.toDouble(), eDz.toDouble()).toFloat()

                            if ((currentState == TrainingState.LEAVE_HAZARD_ZONE || currentState == TrainingState.LOCATE_EMERGENCY_EQUIPMENT) && distToStation <= 1.8f) {
                                mainHandler.post {
                                    viewModel.updateState(TrainingState.PERFORM_SAFE_RESPONSE)
                                }
                            }
                        }
                    }
                }
            }
        )

        // =========================================================================
        // REFERENCE IMAGE 2: COMPLETE INDUSTRIAL AR HUD OVERLAY
        // =========================================================================
        Box(modifier = Modifier.fillMaxSize()) {

            // --- 1. TOP STATUS BAR (Score / Time & Title) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left Glassmorphic Badge: "Score: 100/100 | Time: 0:00"
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Score: $score/100",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "  |  ",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Time: ${timeSeconds / 60}:${String.format(Locale.US, "%02d", timeSeconds % 60)}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top-Right Industrial Branding: "MINE SAFE AR / COAL MINE TRAINING"
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.hud_title),
                        color = Color(0xFF00E676),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = MaterialTheme.typography.titleMedium.letterSpacing * 1.2f
                    )
                    Text(
                        text = stringResource(R.string.hud_subtitle),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- 2. TOP-LEFT INSTRUCTION CARD (Matching Reference Image 2) ---
            val instructionHeader = when (currentState) {
                TrainingState.START, TrainingState.SCAN_FLOOR, TrainingState.PLACE_DOORWAY -> stringResource(R.string.scan_title)
                TrainingState.ENTER_MINE -> stringResource(R.string.instr_enter_mine)
                TrainingState.FIRE_DETECTED, TrainingState.GO_TO_EXTINGUISHER -> stringResource(R.string.instr_fire_detected)
                TrainingState.EXTINGUISHER_REACHED -> stringResource(R.string.instr_extinguisher_reached)
                TrainingState.OPEN_NOZZLE, TrainingState.AIM_AT_FIRE -> stringResource(R.string.instr_aim_at_fire)
                TrainingState.DISCHARGE_EXTINGUISHER -> stringResource(R.string.instr_discharge)
                TrainingState.FIRE_EXTINGUISHED -> stringResource(R.string.instr_fire_extinguished)
                TrainingState.CHEMICAL_HAZARD_DETECTED -> stringResource(R.string.instr_chem_detected)
                TrainingState.MAINTAIN_SAFE_DISTANCE, TrainingState.LEAVE_HAZARD_ZONE -> stringResource(R.string.instr_chem_move_away)
                TrainingState.LOCATE_EMERGENCY_EQUIPMENT -> stringResource(R.string.instr_chem_locate_equipment)
                TrainingState.PERFORM_SAFE_RESPONSE -> stringResource(R.string.instr_chem_follow_procedure)
                TrainingState.HAZARD_CONTROLLED -> stringResource(R.string.instr_chem_controlled)
                TrainingState.TRAINING_COMPLETE -> stringResource(R.string.training_complete)
            }

            val instructionDesc = when (currentState) {
                TrainingState.START, TrainingState.SCAN_FLOOR, TrainingState.PLACE_DOORWAY -> stringResource(R.string.scan_desc)
                TrainingState.ENTER_MINE -> if (doorDistance > 0) String.format(Locale.US, "Doorway: %.2f m ahead", doorDistance) else "Approach the entrance"
                TrainingState.FIRE_DETECTED -> "Target extinguisher ~10m ahead in drift"
                TrainingState.EXTINGUISHER_REACHED -> "Tap nozzle or pull safety pin"
                TrainingState.OPEN_NOZZLE, TrainingState.AIM_AT_FIRE -> if (isAimingAtFire) "Aimed at base of fire. Ready to discharge." else "Aim camera directly at fire base"
                TrainingState.DISCHARGE_EXTINGUISHER -> "Discharging suppression agent"
                TrainingState.CHEMICAL_HAZARD_DETECTED -> "Stay at least 2.0m away from toxic vapor"
                TrainingState.LEAVE_HAZARD_ZONE -> "Retreat to clear vantage point"
                TrainingState.PERFORM_SAFE_RESPONSE -> "Turn emergency isolation valve on wall"
                else -> ""
            }

            Card(
                modifier = Modifier
                    .padding(top = 76.dp, start = 16.dp)
                    .widthIn(max = 290.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14171A).copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Frame scan viewfinder icon
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⛶", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = instructionHeader,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (instructionDesc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = instructionDesc,
                                color = Color(0xFFB0BEC5),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // --- 3. RIGHT-SIDE VERTICAL ACTION DOCK (Reference Image 2) ---
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Settings Button
                FilledIconButton(
                    onClick = { showSettingsModal = true },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("⚙", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }

                // Help Button
                FilledIconButton(
                    onClick = { showHelpModal = true },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("?", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                // Sound Toggle Button
                FilledIconButton(
                    onClick = {
                        viewModel.toggleSound()
                        val muted = !isSoundMuted
                        voiceManager.isMuted = muted
                        audioManager.isMuted = muted
                        if (muted) audioManager.stopMineAmbiance()
                    },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f),
                        contentColor = if (isSoundMuted) Color(0xFFEF5350) else Color.White
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Text(if (isSoundMuted) "🔇" else "🔊", style = MaterialTheme.typography.titleMedium)
                }
            }

            // --- 4. INTERACTIVE ACTIONS HUD (Nozzle, Aim, Discharge, Valve) ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentState == TrainingState.EXTINGUISHER_REACHED) {
                    Button(
                        onClick = {
                            extinguisherNode?.removeSafetyPin()
                            extinguisherNode?.openNozzle()
                            viewModel.updateState(TrainingState.OPEN_NOZZLE)
                            viewModel.updateState(TrainingState.AIM_AT_FIRE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(stringResource(R.string.btn_open_nozzle), fontWeight = FontWeight.Bold)
                    }
                }

                if (currentState == TrainingState.AIM_AT_FIRE || currentState == TrainingState.OPEN_NOZZLE) {
                    Button(
                        onClick = {
                            if (isAimingAtFire) {
                                viewModel.updateState(TrainingState.DISCHARGE_EXTINGUISHER)
                                extinguisherNode?.setDischarging(true)
                                fireNode?.setFireScale(0.0f)
                                viewModel.updateState(TrainingState.FIRE_EXTINGUISHED)
                            } else {
                                viewModel.registerMistake(5)
                                audioManager.playMistakeBuzzer()
                                voiceManager.speak("Aim directly at the base of the fire before discharging.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAimingAtFire) Color(0xFFFF3D00) else Color(0xFF455A64),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(stringResource(R.string.btn_discharge), fontWeight = FontWeight.Bold)
                    }
                }

                if (currentState == TrainingState.PERFORM_SAFE_RESPONSE) {
                    Button(
                        onClick = {
                            emergencyStationNode?.activateValve()
                            chemicalNode?.setHazardControlled(true)
                            viewModel.updateState(TrainingState.HAZARD_CONTROLLED)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(stringResource(R.string.btn_isolate_valve), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- 5. BOTTOM TOAST PILL (Reference Image 2) ---
            if (doorwayAnchor == null) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👆", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tap_to_place_toast),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Tracking lost warning
            if (trackingLost) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.90f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ar_tracking_lost),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (showHelpModal) {
            HelpModal(onDismiss = { showHelpModal = false })
        }

        if (showSettingsModal) {
            SettingsModal(
                isSoundMuted = isSoundMuted,
                onToggleSound = {
                    viewModel.toggleSound()
                    val muted = !isSoundMuted
                    voiceManager.isMuted = muted
                    audioManager.isMuted = muted
                    if (muted) audioManager.stopMineAmbiance()
                },
                currentLanguage = voiceManager.currentLanguageCode,
                onLanguageChange = { lang -> voiceManager.setLanguage(lang) },
                onDismiss = { showSettingsModal = false }
            )
        }
    }
}
