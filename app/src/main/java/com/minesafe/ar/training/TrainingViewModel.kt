package com.minesafe.ar.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingViewModel : ViewModel() {
    private val _currentState = MutableStateFlow(TrainingState.START)
    val currentState: StateFlow<TrainingState> = _currentState.asStateFlow()

    private val _score = MutableStateFlow(100)
    val score: StateFlow<Int> = _score.asStateFlow()
    
    private val _timeSeconds = MutableStateFlow(0)
    val timeSeconds: StateFlow<Int> = _timeSeconds.asStateFlow()

    private val _mistakes = MutableStateFlow(0)
    val mistakes: StateFlow<Int> = _mistakes.asStateFlow()

    private val _stepsCompleted = MutableStateFlow(0)
    val stepsCompleted: StateFlow<Int> = _stepsCompleted.asStateFlow()

    private var timerJob: Job? = null
    val totalSteps = 6 // Fire detected, Reach extinguisher, Open pin, Aim, Discharge, Extinguish

    fun updateState(newState: TrainingState) {
        if (_currentState.value != newState) {
            _currentState.value = newState
            
            // Advance steps based on state
            when (newState) {
                TrainingState.ENTER_MINE -> startTimer()
                TrainingState.GO_TO_EXTINGUISHER -> _stepsCompleted.value = 1
                TrainingState.EXTINGUISHER_REACHED -> _stepsCompleted.value = 2
                TrainingState.OPEN_SAFETY_MECHANISM -> _stepsCompleted.value = 3
                TrainingState.AIM_AT_FIRE -> _stepsCompleted.value = 4
                TrainingState.DISCHARGE_EXTINGUISHER -> _stepsCompleted.value = 5
                TrainingState.FIRE_EXTINGUISHED -> {
                    _stepsCompleted.value = 6
                    stopTimer()
                }
                TrainingState.START -> resetTraining()
                else -> {}
            }
        }
    }

    fun registerMistake() {
        _mistakes.value += 1
        deductScore(5)
    }

    private fun deductScore(points: Int) {
        if (_score.value - points >= 0) {
            _score.value -= points
        } else {
            _score.value = 0
        }
    }

    private fun startTimer() {
        if (timerJob == null) {
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _timeSeconds.value += 1
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTraining() {
        stopTimer()
        _currentState.value = TrainingState.START
        _score.value = 100
        _timeSeconds.value = 0
        _mistakes.value = 0
        _stepsCompleted.value = 0
    }
}
