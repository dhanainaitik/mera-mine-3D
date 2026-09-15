package com.minesafe.ar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minesafe.ar.R

@Composable
fun ResultsScreen(
    score: Int,
    timeSeconds: Int,
    mistakes: Int,
    stepsCompleted: Int,
    totalSteps: Int,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = Color.Yellow,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(id = R.string.training_complete),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(id = R.string.safety_score), color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("$score / 100", color = if (score >= 80) Color.Green else Color.Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(id = R.string.response_time), color = Color.LightGray)
                    Text(timeString, color = Color.White)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(id = R.string.mistakes), color = Color.LightGray)
                    Text("$mistakes", color = Color.White)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(id = R.string.steps_completed), color = Color.LightGray)
                    Text("$stepsCompleted / $totalSteps", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(id = R.string.fire_extinguished),
                    color = Color.Green,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text(stringResource(id = R.string.restart_training), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text(stringResource(id = R.string.back_to_home), color = Color.White)
        }
    }
}
