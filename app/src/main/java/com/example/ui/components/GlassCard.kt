package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    alpha: Float = 0.05f,
    borderAlpha: Float = 0.08f,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = alpha),
            baseColor.copy(alpha = alpha * 0.7f)
        )
    )

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = borderAlpha),
            baseColor.copy(alpha = borderAlpha * 0.4f)
        )
    )

    val baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(bgBrush)
        .border(
            width = 1.dp,
            brush = borderBrush,
            shape = RoundedCornerShape(cornerRadius)
        )

    if (onClick != null) {
        Column(
            modifier = baseModifier.clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = baseColor)
            ).padding(20.dp),
            content = content
        )
    } else {
        Column(
            modifier = baseModifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun FintechButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState by animateFloatAsState(if (isPressed) 0.96f else 1.0f, label = "button_press")

    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White
    val contentColor = if (isLight) Color.White else Color.Black

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = baseColor,
            contentColor = contentColor,
            disabledContainerColor = baseColor.copy(alpha = 0.2f),
            disabledContentColor = baseColor.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .graphicsLayer(
                scaleX = scaleState,
                scaleY = scaleState
            )
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = baseColor.copy(alpha = 0.15f),
                spotColor = baseColor.copy(alpha = 0.2f)
            ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState by animateFloatAsState(if (isPressed) 0.96f else 1.0f, label = "button_press")

    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.12f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = baseColor,
            disabledContentColor = baseColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .graphicsLayer(
                scaleX = scaleState,
                scaleY = scaleState
            )
            .background(baseColor.copy(alpha = 0.04f), RoundedCornerShape(32.dp)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleekTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = if (isLight) LightTextTertiary else TextTertiary,
                fontSize = 15.sp
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (onDone != null) onDone() else focusManager.clearFocus()
            },
            onNext = {
                focusManager.clearFocus()
            }
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = baseColor,
            unfocusedTextColor = baseColor,
            focusedContainerColor = baseColor.copy(alpha = 0.05f),
            unfocusedContainerColor = baseColor.copy(alpha = 0.02f),
            focusedBorderColor = baseColor.copy(alpha = 0.25f),
            unfocusedBorderColor = baseColor.copy(alpha = 0.08f),
            cursorColor = baseColor
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DenominationQuantityInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Number,
    isDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    var textFieldValueState by remember {
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = value,
                selection = androidx.compose.ui.text.TextRange(value.length)
            )
        )
    }

    // Keep local state in sync with external value updates (e.g. Reset, Clear)
    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(
                text = value,
                selection = androidx.compose.ui.text.TextRange(value.length)
            )
        }
    }

    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White

    androidx.compose.foundation.text.BasicTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            val typedText = newValue.text
            // Limit character length to prevent visual and calculation layout issues
            if (typedText.length > 6) return@BasicTextField

            // Filter input based on decimal or integer rules to guarantee clean input values
            val isValid = if (isDecimal) {
                typedText.isEmpty() || typedText.matches(Regex("^\\d*\\.?\\d*$"))
            } else {
                typedText.isEmpty() || typedText.matches(Regex("^\\d*$"))
            }

            if (isValid) {
                textFieldValueState = newValue
                onValueChange(typedText)
            }
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = baseColor
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(baseColor),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(baseColor.copy(alpha = if (value.isNotEmpty() && value != "0" && value != "") 0.08f else 0.03f))
                    .border(
                        width = 1.dp,
                        color = if (value.isNotEmpty() && value != "0" && value != "") baseColor.copy(alpha = 0.15f) else baseColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty() || value == "0") {
                    Text(
                        text = "0",
                        color = if (isLight) LightTextTertiary else TextTertiary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun DenominationRow(
    denomination: Int,
    count: Int,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rowTotal = denomination * count
    val countString = if (count > 0) count.toString() else ""

    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(baseColor.copy(alpha = 0.02f))
            .border(1.dp, baseColor.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Denomination label
        Text(
            text = "₹$denomination",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLight) LightTextSecondary else TextSecondary,
            letterSpacing = 0.2.sp,
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Directly type quantity field
        DenominationQuantityInput(
            value = countString,
            onValueChange = { input ->
                if (input.isEmpty()) {
                    onCountChange(0)
                } else {
                    onCountChange(input.toIntOrNull() ?: 0)
                }
            },
            keyboardType = KeyboardType.Number,
            isDecimal = false,
            modifier = Modifier
                .width(68.dp)
                .height(38.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Total calculated value
        Text(
            text = "₹$rowTotal",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (count > 0) baseColor else (if (isLight) LightTextSecondary else TextSecondary),
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp)
        )
    }
}
