package org.mikyegresl.phonetextfield

import android.telephony.PhoneNumberUtils.PAUSE
import android.telephony.PhoneNumberUtils.WAIT
import android.telephony.PhoneNumberUtils.WILD
import android.text.Selection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

class PhoneVisualTransformationV2 : VisualTransformation {

    private data class Transformation(
        val formatted: String?,
        val originalToTransformed: List<Int>,
        val transformedToOriginal: List<Int>
    )

    private val dynamicTypeFormatter = PhoneNumberUtil
        .getInstance()
        .getAsYouTypeFormatter(
            Locale.getDefault().country
        )

    private fun isNonSeparator(c: Char): Boolean =
        (c in '0'..'9') ||
                c == '*' || c == '#' ||
                c == WILD || c == WAIT || c == PAUSE


    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean): String? =
        if (hasCursor) {
            dynamicTypeFormatter.inputDigitAndRememberPosition(lastNonSeparator)
        } else {
            dynamicTypeFormatter.inputDigit(lastNonSeparator)
        }

    private fun reformat(s: CharSequence, cursor: Int): Transformation {
        dynamicTypeFormatter.clear()

        val currentIndex = cursor - 1
        var formatted: String? = null
        var lastNonSeparator = 0.toChar()
        var hasCursor = false

        val formattedSequence =
            if (s.isNotEmpty() && s.take(0) != "+") "+$s"
            else s

        formattedSequence.forEachIndexed { index, char ->
            if (lastNonSeparator.code != 0) {
                formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                hasCursor = false
            }
            lastNonSeparator = char
            if (index == currentIndex) {
                hasCursor = true
            }
        }

        if (lastNonSeparator.code != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor)
        }
        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()
        var specialCharsCount = 0

        formatted?.forEachIndexed { index, char ->
            if (!isNonSeparator(char)) {
                specialCharsCount++
                transformedToOriginal.add(index - specialCharsCount)
            } else {
                originalToTransformed.add(index)
                transformedToOriginal.add(index - specialCharsCount)
            }
        }
        originalToTransformed.add(originalToTransformed.maxOrNull()?.plus(1) ?: 0)
        transformedToOriginal.add(transformedToOriginal.maxOrNull()?.plus(1) ?: 0)

        return Transformation(formatted, originalToTransformed, transformedToOriginal)
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val transformation = reformat(text, Selection.getSelectionEnd(text))

        return TransformedText(
            AnnotatedString(transformation.formatted.orEmpty()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    transformation.originalToTransformed[offset.coerceIn(
                        transformation.originalToTransformed.indices
                    )]

                override fun transformedToOriginal(offset: Int): Int =
                    transformation.transformedToOriginal[offset.coerceIn(
                        transformation.transformedToOriginal.indices
                    )]
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhoneVisualTransformationV2) return false
        return dynamicTypeFormatter == other.dynamicTypeFormatter
    }

    override fun hashCode(): Int {
        return dynamicTypeFormatter.hashCode()
    }
}