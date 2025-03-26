package com.example.calodiary.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class DateInputFormatter implements TextWatcher {
    private final EditText editText;
    private String previousText = "";

    public DateInputFormatter(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        previousText = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        String input = s.toString().replaceAll("[^0-9]", "");
        if (input.length() > 8) {
            input = input.substring(0, 8);
        }
        String formatted = formatDate(input);
        if (!formatted.equals(s.toString())) {
            editText.removeTextChangedListener(this);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    private String formatDate(String input) {
        if (input.length() <= 2) {
            return input;
        } else if (input.length() <= 4) {
            return input.substring(0, 2) + "/" + input.substring(2);
        } else {
            return input.substring(0, 2) + "/" + input.substring(2, 4) + "/" + input.substring(4);
        }
    }
}