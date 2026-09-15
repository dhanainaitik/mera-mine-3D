package com.minesafe.ar.ui

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.minesafe.ar.R
import com.minesafe.ar.ar.DoorwayNode
import com.minesafe.ar.ar.ExtinguisherNode
import com.minesafe.ar.ar.FireNode
import com.minesafe.ar.ar.MineEnvironmentNode
import com.minesafe.ar.audio.VoiceInstructionManager
import com.minesafe.ar.training.TrainingState
import com.minesafe.ar.training.TrainingViewModel
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.dot
import dev.romainguy.kotlin.math.normalize
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.collision.HitResult
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import kotlinx.coroutines.delay
import java.util.Locale

import android.os.Handler
import android.os.Looper

@Composable
fun MainApp(
    viewModel: TrainingViewModel,
    voiceManager: VoiceInstructionManager,
    onTrainingComplete: () -> Unit
) {
    val currentState by viewModel.currentState.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeSeconds by viewModel.timeSeconds.collectAsState()

    // Audio side-effects based on state change
    LaunchedEffect(currentState) {
        when (currentState) {
            TrainingState.START -> {
                voiceManager.speak("Welcome to MineSafe AR. Tap anywhere to begin.")
            }
            TrainingState.PLACE_DOORWAY -> {
                voiceManager.speak("Scan the floor and tap to place the doorway.")
            }
            TrainingState.ENTER_MINE -> {
                voiceManager.speak("Walk through the doorway to enter the mine.")
            }
            TrainingState.FIRE_DETECTED -> {
                voiceManager.speak("An electrical fire has been detected. Get the fire extinguisher.")
            }
            TrainingState.EXTINGUISHER_REACHED -> {
                while (true) {
                    voiceManager.speak("Pick up the extinguisher and open the safety mechanism.")
                    delay(4000)
                }
            }
            TrainingState.AIM_AT_FIRE -> {
                while (true) {
                    voiceManager.speak("Aim the nozzle at the base of the fire.")
                    delay(4000)
                }
            }
            TrainingState.DISCHARGE_EXTINGUISHER -> {
                voiceManager.speak("Discharging extinguisher.")
            }
            TrainingState.FIRE_EXTINGUISHED -> {
                voiceManager.speak("Fire extinguished. Good job. Training complete.")
                delay(3000)
                onTrainingComplete()
            }
            else -> {}
        }
    }

    var childNodes by remember { mutableStateOf(listOf<Node>()) }

    var currentFrame by remember { mutableStateOf<Frame?>(null) }
    var doorwayAnchor by remember { mutableStateOf<Anchor?>(null) }
    var fireNode by remember { mutableStateOf<FireNode?>(null) }
    var extinguisherNode by remember { mutableStateOf<ExtinguisherNode?>(null) }
    var doorDistance by remember { mutableFloatStateOf(-1f) }
    var trackingLost by remember { mutableStateOf(false) }
    
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    
    val redMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFD32F2F)) }
    val metalMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFB0BEC5)) }
    val frameMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF263238)) }
    
    val rockMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF3A302A)) }
    val floorMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF241E1A)) }
    val woodMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF2A1610)) }
    val darkMetalMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFF4F4F4F)) }
    val lampMaterial = remember(materialLoader) { materialLoader.createColorInstance(Color(0xFFFFD54F)) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = childNodes,
            planeRenderer = true,
            onTouchEvent = { e: MotionEvent, hitResult: HitResult? ->
                if (e.action == MotionEvent.ACTION_UP && currentState == TrainingState.START) {
                    viewModel.updateState(TrainingState.PLACE_DOORWAY)
                    return@ARScene true
                }
                
                if (e.action == MotionEvent.ACTION_UP && currentState == TrainingState.PLACE_DOORWAY) {
                    currentFrame?.hitTest(e.x, e.y)?.firstOrNull { it.isValid(planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING)) }?.let { arHit ->
                        val anchor = arHit.createAnchorOrNull()
                        if (anchor != null) {
                            doorwayAnchor = anchor
                            val anchorNode = AnchorNode(engine, anchor)
                            
                            val doorwayNode = DoorwayNode(engine, frameMaterial)
                            anchorNode.addChildNode(doorwayNode)

                            val envNode = MineEnvironmentNode(
                                engine = engine,
                                rockMaterial = rockMaterial,
                                floorMaterial = floorMaterial,
                                woodMaterial = woodMaterial,
                                metalMaterial = darkMetalMaterial,
                                lampMaterial = lampMaterial
                            )
                            anchorNode.addChildNode(envNode)
                            
                            // Spawn Fire 10 meters ahead
                            val fNode = FireNode(engine, redMaterial).apply {
                                position = Float3(0.0f, 0.0f, -10.0f)
                            }
                            fireNode = fNode
                            anchorNode.addChildNode(fNode)
                            
                            // Spawn Extinguisher 9 meters ahead, to the right
                            val extNode = ExtinguisherNode(engine, redMaterial, metalMaterial).apply {
                                position = Float3(0.4f, 0.0f, -9.0f)
                            }
                            extinguisherNode = extNode
                            anchorNode.addChildNode(extNode)
                            
                            childNodes = childNodes + anchorNode
                            viewModel.updateState(TrainingState.ENTER_MINE)
                        }
                    }
                }
                
                if (e.action == MotionEvent.ACTION_UP) {
                    when (currentState) {
                        TrainingState.EXTINGUISHER_REACHED -> {
                            if (hitResult?.node == extinguisherNode?.safetyPin) {
                                extinguisherNode?.removeSafetyPin()
                                viewModel.updateState(TrainingState.AIM_AT_FIRE)
                            } else {
                                viewModel.registerMistake()
                                voiceManager.speak("Tap the safety pin to remove it.")
                            }
                        }
                        TrainingState.AIM_AT_FIRE -> {
                            // Check aiming using vector math instead of simple tap
                            val camPose = currentFrame?.camera?.pose
                            val firePos = fireNode?.worldPosition
                            if (camPose != null && firePos != null) {
                                val camPos = Float3(camPose.tx(), camPose.ty(), camPose.tz())
                                val camForward = Float3(camPose.zAxis[0], camPose.zAxis[1], camPose.zAxis[2]) * -1.0f
                                
                                val toFire = normalize(firePos - camPos)
                                val angleCos = dot(camForward, toFire)
                                
                                // Cosine of 15 degrees is ~0.965
                                if (angleCos > 0.95f) {
                                    viewModel.updateState(TrainingState.DISCHARGE_EXTINGUISHER)
                                    fireNode?.setFireScale(0.1f) // Extinguish visually
                                    viewModel.updateState(TrainingState.FIRE_EXTINGUISHED)
                                } else {
                                    viewModel.registerMistake()
                                    voiceManager.speak("Aim directly at the fire before discharging.")
                                }
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
                
                // State Machine Checks based on frame (Camera position)
                val cameraPose = frame.camera.pose
                val dAnchor = doorwayAnchor
                
                if (dAnchor != null) {
                    val dx = cameraPose.tx() - dAnchor.pose.tx()
                    val dy = cameraPose.ty() - dAnchor.pose.ty()
                    val dz = cameraPose.tz() - dAnchor.pose.tz()
                    val dist3D = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
                    
                    mainHandler.post {
                        doorDistance = dist3D
                    }
                    
                    if (currentState == TrainingState.ENTER_MINE && dist3D <= 1.3f && dist3D > 0.0f) {
                        mainHandler.post {
                            viewModel.updateState(TrainingState.FIRE_DETECTED)
                        }
                    }

                    // Check distance to extinguisher
                    val extPos = extinguisherNode?.worldPosition
                    if (extPos != null && (currentState == TrainingState.FIRE_DETECTED || currentState == TrainingState.GO_TO_EXTINGUISHER)) {
                        val extDx = cameraPose.tx() - extPos.x
                        val extDz = cameraPose.tz() - extPos.z
                        val distanceToExt = Math.hypot(extDx.toDouble(), extDz.toDouble()).toFloat()
                        
                        if (distanceToExt < 1.5f) {
                            mainHandler.post {
                                viewModel.updateState(TrainingState.EXTINGUISHER_REACHED)
                            }
                        }
                    }
                }
            }
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Score: $score/100",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Time: ${timeSeconds / 60}:${String.format(Locale.US, "%02d", timeSeconds % 60)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "MINE SAFE AR",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Yellow
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val instructionText = when (currentState) {
                TrainingState.START -> stringResource(R.string.instr_start)
                TrainingState.SCAN_FLOOR -> stringResource(R.string.instr_scan_floor)
                TrainingState.PLACE_DOORWAY -> stringResource(R.string.instr_scan_floor)
                TrainingState.ENTER_MINE -> stringResource(R.string.instr_enter_mine)
                TrainingState.FIRE_DETECTED -> stringResource(R.string.instr_fire_detected)
                TrainingState.GO_TO_EXTINGUISHER -> stringResource(R.string.instr_go_to_extinguisher)
                TrainingState.EXTINGUISHER_REACHED -> stringResource(R.string.instr_extinguisher_reached)
                TrainingState.OPEN_SAFETY_MECHANISM -> stringResource(R.string.instr_open_safety)
                TrainingState.AIM_AT_FIRE -> stringResource(R.string.instr_aim_at_fire)
                TrainingState.DISCHARGE_EXTINGUISHER -> stringResource(R.string.instr_discharge)
                TrainingState.FIRE_EXTINGUISHED -> stringResource(R.string.instr_fire_extinguished)
                TrainingState.TRAINING_COMPLETE -> stringResource(R.string.training_complete)
            }
            
            Text(
                text = instructionText,
                style = MaterialTheme.typography.headlineSmall,
                color = if (currentState == TrainingState.FIRE_DETECTED) Color.Red else Color.White,
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.8f))
                    .padding(16.dp)
            )

            if (doorwayAnchor != null && doorDistance >= 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Door distance: ${String.format(Locale.US, "%.2f", doorDistance)} m",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Yellow,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (trackingLost) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.ar_tracking_lost),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.8f))
                        .padding(16.dp)
                )
            }
        }
    }
}
