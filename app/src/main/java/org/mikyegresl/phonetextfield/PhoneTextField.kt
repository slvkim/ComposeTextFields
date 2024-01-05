package org.mikyegresl.phonetextfield

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Phone input text field. */
@Composable
fun PhoneTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    maxLength: Int,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    GenericTextField(
        value = value,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        onValueChange = { onValueChange(it) },
        maxLength = maxLength,
        placeholder = { Text(text = placeholder) },
        visualTransformation = PhoneVisualTransformationV2()
    )
}

@Composable
fun GenericTextField(
    modifier: Modifier = Modifier,
    value: String,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    maxLength: Int? = null,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit = {},
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(
        modifier = Modifier.animateContentSize(
            spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            )
        )
    ) {
        OutlinedTextField(
            modifier = modifier,
            value = maxLength?.let(value::coerceMaxLength) ?: value,
            textStyle = textStyle,
            enabled = enabled,
            visualTransformation = visualTransformation,
            singleLine = true,
            placeholder = placeholder,
            onValueChange = { value ->
                onValueChange(value)
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/** Returns a string if its length does not exceed maxLength limit. */
fun String.coerceMaxLength(maxLength: Int): String =
    (length - maxLength).takeIf { it > 0 }?.let { dropLast(it) } ?: this
