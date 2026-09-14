package com.minesafe.ar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minesafe.ar.audio.AudioManager
import com.minesafe.ar.audio.VoiceInstructionManager
import com.minesafe.ar.training.TrainingViewModel
import com.minesafe.ar.ui.AppScreen
import com.minesafe.ar.ui.MainApp
import com.minesafe.ar.ui.ResultsScreen
import com.minesafe.ar.ui.StartScreen
import com.minesafe.ar.ui.theme.MineSafeARTheme

class MainActivity : ComponentActivity() {
    private var voiceManager: VoiceInstructionManager? = null
    private var audioManager: AudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vManager = VoiceInstructionManager(this)
        val aManager = AudioManager(this)
        voiceManager = vManager
        audioManager = aManager

        setContent {
            MineSafeARTheme {
                val viewModel: TrainingViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(AppScreen.START) }

                val selectedModule by viewModel.selectedModule.collectAsState()
                val score by viewModel.score.collectAsState()
                val timeSeconds by viewModel.timeSeconds.collectAsState()
                val mistakes by viewModel.mistakes.collectAsState()
                val stepsCompleted by viewModel.stepsCompleted.collectAsState()
                val isSoundMuted by viewModel.isSoundMuted.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            AppScreen.START -> {
                                StartScreen(
                                    selectedModule = selectedModule,
                                    onSelectModule = { module -> viewModel.selectModule(module) },
                                    onStartTraining = {
                                        viewModel.resetTraining()
                                        currentScreen = AppScreen.TRAINING
                                    },
                                    isSoundMuted = isSoundMuted,
                                    onToggleSound = {
                                        viewModel.toggleSound()
                                        val muted = !isSoundMuted
                                        vManager.isMuted = muted
                                        aManager.isMuted = muted
                                        if (muted) aManager.stopMineAmbiance()
                                    },
                                    currentLanguage = vManager.currentLanguageCode,
                                    onLanguageSelected = { lang ->
                                        vManager.setLanguage(lang)
                                    }
                                )
                            }
                            AppScreen.TRAINING -> {
                                MainApp(
                                    viewModel = viewModel,
                                    voiceManager = vManager,
                                    audioManager = aManager,
                                    onTrainingComplete = { currentScreen = AppScreen.RESULTS }
                                )
                            }
                            AppScreen.RESULTS -> {
                                ResultsScreen(
                                    module = selectedModule,
                                    score = score,
                                    timeSeconds = timeSeconds,
                                    mistakes = mistakes,
                                    stepsCompleted = stepsCompleted,
                                    totalSteps = viewModel.totalSteps,
                                    onRestart = {
                                        viewModel.resetTraining()
                                        currentScreen = AppScreen.TRAINING
                                    },
                                    onHome = {
                                        currentScreen = AppScreen.START
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager?.shutdown()
        audioManager?.shutdown()
    }
}