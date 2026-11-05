package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.sound.SoundManager
import com.example.ui.components.Game3DButton
import com.example.ui.components.Game3DCard
import com.example.ui.components.GameTopBar
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DarkPurpleSurfaceElevated
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    coins: Int,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isDarkMode: Boolean,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (Language) -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = Strings.get("settings", lang),
                coins = coins,
                lang = lang,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepSpaceBackground),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Audio & Haptics Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Audio & Haptics",
                            color = GoldYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Sound Effects Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Strings.get("settings_sound", lang), color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = isSoundEnabled,
                                onCheckedChange = {
                                    soundManager.playClick()
                                    onToggleSound(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E676)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Music Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Strings.get("settings_music", lang), color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = isMusicEnabled,
                                onCheckedChange = {
                                    soundManager.playClick()
                                    onToggleMusic(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E676)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Vibration Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Strings.get("settings_vibration", lang), color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = isVibrationEnabled,
                                onCheckedChange = {
                                    soundManager.playClick()
                                    onToggleVibration(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E676)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language & Display Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = Strings.get("settings_language", lang),
                            color = GoldYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Game3DButton(
                                text = "English 🇺🇸",
                                fontSize = 14,
                                height = 48.dp,
                                gradient = if (lang == Language.EN) Brush.verticalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF512DA8))) else Brush.verticalGradient(listOf(Color(0xFF424242), Color(0xFF212121))),
                                onClick = {
                                    soundManager.playClick()
                                    onSelectLanguage(Language.EN)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "lang_en_btn"
                            )

                            Game3DButton(
                                text = "বাংলা 🇧🇩",
                                fontSize = 14,
                                height = 48.dp,
                                gradient = if (lang == Language.BN) Brush.verticalGradient(listOf(Color(0xFF00E676), Color(0xFF00A152))) else Brush.verticalGradient(listOf(Color(0xFF424242), Color(0xFF212121))),
                                onClick = {
                                    soundManager.playClick()
                                    onSelectLanguage(Language.BN)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "lang_bn_btn"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Strings.get("settings_dark_mode", lang), color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = {
                                    soundManager.playClick()
                                    onToggleDarkMode(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF7C4DFF)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About Game Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = Strings.get("settings_about", lang),
                            color = GoldYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Strings.get("about_text", lang),
                            color = Color(0xFFD1C4E9),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Destructive Action: Reset Progress Button
                Game3DButton(
                    text = "⚠️ ${Strings.get("settings_reset", lang)}",
                    gradient = Brush.verticalGradient(listOf(Color(0xFFFF3D00), Color(0xFFD50000))),
                    shadowColor = Color(0xFFB71C1C),
                    onClick = {
                        soundManager.playWrong()
                        showResetDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_reset_btn"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Reset Confirmation Dialog
            if (showResetDialog) {
                BasicAlertDialog(onDismissRequest = { showResetDialog = false }) {
                    Game3DCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        backgroundColor = DarkPurpleSurfaceElevated,
                        borderColor = Color(0xFFFF1744)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = Strings.get("reset_progress_title", lang),
                                color = Color(0xFFFF5252),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = Strings.get("reset_progress_desc", lang),
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Game3DButton(
                                text = Strings.get("confirm", lang),
                                gradient = Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFFD50000))),
                                shadowColor = Color(0xFFB71C1C),
                                onClick = {
                                    showResetDialog = false
                                    onResetProgress()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Game3DButton(
                                text = Strings.get("cancel", lang),
                                gradient = Brush.verticalGradient(listOf(Color(0xFF616161), Color(0xFF424242))),
                                shadowColor = Color(0xFF212121),
                                onClick = { showResetDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
