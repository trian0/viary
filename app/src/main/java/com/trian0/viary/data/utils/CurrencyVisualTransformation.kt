package com.trian0.viary.data.utils

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

class CurrencyOutputTransformation(private val symbol: String) : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        val digits = toString().filter { it.isDigit() }
        val amount = digits.toLongOrNull() ?: 0L
        val formatted = "$symbol ${String.format("%,.2f", amount / 100.0)}"

        replace(0, length, formatted)
    }
}