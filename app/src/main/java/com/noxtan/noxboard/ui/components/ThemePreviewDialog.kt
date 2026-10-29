package com.noxtan.noxboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.noxtan.noxboard.ui.models.KeyboardThemePreset

@Composable
fun ThemePreviewDialog(
    theme: KeyboardThemePreset,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onEdit: () -> Unit
) {
    val themeBgColor = Color(android.graphics.Color.parseColor(theme.bgHex))
    val themeKeyColor = Color(android.graphics.Color.parseColor(theme.keyHex))
    val themeTextColor = Color(android.graphics.Color.parseColor(theme.textHex))
    val themeSpecKeyColor = Color(android.graphics.Color.parseColor(theme.specKeyHex))
    val themeSpecTextColor = Color(android.graphics.Color.parseColor(theme.specTextHex))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = theme.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    val bgInt = try { android.graphics.Color.parseColor(theme.bgHex) } catch(e: Exception) { 0 }
                    val darkness = 1 - (0.299 * android.graphics.Color.red(bgInt) + 0.587 * android.graphics.Color.green(bgInt) + 0.114 * android.graphics.Color.blue(bgInt)) / 255
                    val calcSpecBorder = if (theme.bgHex.equals(theme.specKeyHex, ignoreCase = true) && !theme.id.contains("PURE")) {
                        if (darkness < 0.5) Color(0x26000000) else Color(0x26FFFFFF)
                    } else Color.Transparent
                    val finalKeyBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.borderHex)) else Color.Transparent
                    val finalSpecBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.specBorderHex)) else calcSpecBorder

                    val finalSugColor = try { android.graphics.Color.parseColor(if (theme.sugTextHex.isNotEmpty()) theme.sugTextHex else theme.specTextHex) } catch(e: Exception) { android.graphics.Color.WHITE }
                    val finalSugBgColor = try { Color(android.graphics.Color.parseColor(theme.sugBgHex)) } catch(e: Exception) { themeBgColor }

                    MockKeyboard(
                        bg = themeBgColor,
                        keyColor = themeKeyColor,
                        textColor = themeTextColor,
                        specKeyColor = themeSpecKeyColor,
                        specTextColor = themeSpecTextColor,
                        sugBgColor = finalSugBgColor,
                        sugTextColor = Color(finalSugColor),
                        keyBorderColor = finalKeyBorder,
                        specKeyBorderColor = finalSpecBorder
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit", fontWeight = FontWeight.Bold)
                        }

                        val applyBtnBgColor = if (theme.id == "PRESET_LUXURY") themeSpecTextColor else themeTextColor
                        val applyBtnBgInt = try { android.graphics.Color.parseColor(if (theme.id == "PRESET_LUXURY") theme.specTextHex else theme.textHex) } catch(e: Exception) { android.graphics.Color.WHITE }
                        val applyBtnDarkness = 1 - (0.299 * android.graphics.Color.red(applyBtnBgInt) + 0.587 * android.graphics.Color.green(applyBtnBgInt) + 0.114 * android.graphics.Color.blue(applyBtnBgInt)) / 255
                        val applyBtnTextColor = if (applyBtnDarkness < 0.5) Color.Black else Color.White

                        Button(
                            onClick = onApply,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = applyBtnBgColor,
                                contentColor = applyBtnTextColor
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply Theme", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}