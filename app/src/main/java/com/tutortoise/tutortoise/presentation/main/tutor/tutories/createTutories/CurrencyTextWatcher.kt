package com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class CurrencyTextWatcher(
    private val editText: EditText,
    private val onValueChanged: ((Long) -> Unit)? = null
) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
            val parsed = cleanString.toLongOrNull() ?: 0

            current = parsed.formatWithThousandsSeparator()
            editText.setText(current)
            editText.setSelection(current.length)
            onValueChanged?.invoke(parsed)

            editText.addTextChangedListener(this)
        }
    }
}