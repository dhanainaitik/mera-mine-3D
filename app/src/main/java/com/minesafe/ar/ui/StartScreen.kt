package com.minesafe.ar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minesafe.ar.R

@Composable
fun StartScreen(
    onStartTraining: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("en") }

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
            style = MaterialTheme.typography.displayMedium,
            color = Color.Yellow,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(id = R.string.vocational_safety_training),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(id = R.string.electrical_fire_safety),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = stringResource(id = R.string.language),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    selectedLanguage = "en"
                    onLanguageSelected("en")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "en") Color.Yellow else Color.DarkGray,
                    contentColor = if (selectedLanguage == "en") Color.Black else Color.White
                )
            ) {
                Text(stringResource(id = R.string.english))
            }
            
            Button(
                onClick = {
                    selectedLanguage = "hi"
                    onLanguageSelected("hi")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "hi") Color.Yellow else Color.DarkGray,
                    contentColor = if (selectedLanguage == "hi") Color.Black else Color.White
                )
            ) {
                Text(stringResource(id = R.string.hindi))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartTraining,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text(stringResource(id = R.string.start_training), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
