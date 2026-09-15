package com.minesafe.ar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.minesafe.ar.ui.theme.MineSafeARTheme
import com.minesafe.ar.ui.MainApp

import androidx.compose.runtime.*
import com.minesafe.ar.ui.AppScreen
import com.minesafe.ar.ui.StartScreen
import com.minesafe.ar.ui.ResultsScreen
import com.minesafe.ar.training.TrainingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minesafe.ar.audio.VoiceInstructionManager
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MineSafeARTheme {
                val viewModel: TrainingViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(AppScreen.START) }
                val context = LocalContext.current
                val voiceManager = remember { VoiceInstructionManager(context) }
                
                val score by viewModel.score.collectAsState()
                val timeSeconds by viewModel.timeSeconds.collectAsState()
                val mistakes by viewModel.mistakes.collectAsState()
                val stepsCompleted by viewModel.stepsCompleted.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            AppScreen.START -> {
                                StartScreen(
                                    onStartTraining = { 
                                        viewModel.resetTraining()
                                        currentScreen = AppScreen.TRAINING 
                                    },
                                    onLanguageSelected = { lang ->
                                        voiceManager.setLanguage(lang)
                                    }
                                )
                            }
                            AppScreen.TRAINING -> {
                                MainApp(
                                    viewModel = viewModel, 
                                    voiceManager = voiceManager,
                                    onTrainingComplete = { currentScreen = AppScreen.RESULTS }
                                )
                            }
                            AppScreen.RESULTS -> {
                                ResultsScreen(
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
}