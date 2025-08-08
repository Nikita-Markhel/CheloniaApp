package com.example.chelonia.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class SmartTimeTextWatcher implements TextWatcher {

    private final EditText editText;
    private boolean isUpdating = false;

    public SmartTimeTextWatcher(EditText editText) {
        this.editText = editText;
    }

    private String previousText = "";

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!isUpdating) {
            previousText = s.toString();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Не нужно
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isUpdating) return;

        isUpdating = true;

        int cursorPosition = editText.getSelectionStart();

        String raw = cleanInput(s.toString());
        String[] parts = raw.split("-", 2);

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            formatted.append(formatPart(parts[i]));
            if (i == 0 && parts.length == 2) {
                formatted.append(" - ");
            }
        }

        String finalText = formatted.toString();

        editText.removeTextChangedListener(this);
        editText.setText(finalText);

        // Устанавливаем курсор как можно ближе к прежнему месту
        int newCursorPos = Math.min(finalText.length(), cursorPosition + (finalText.length() - previousText.length()));
        editText.setSelection(Math.max(0, newCursorPos));

        editText.addTextChangedListener(this);
        isUpdating = false;
    }

    private String cleanInput(String input) {
        StringBuilder result = new StringBuilder();
        boolean dashAdded = false;
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                result.append(c);
            } else if (c == '-' && !dashAdded) {
                result.append('-');
                dashAdded = true;
            }
        }
        return result.toString();
    }

    private String formatPart(String part) {
        if (part.isEmpty()) return "";

        String hours = "00";
        String minutes = "00";

        int len = part.length();

        if (len == 1) {
            hours = "0" + part;
        } else if (len == 2) {
            hours = part;
        } else if (len == 3) {
            hours = "0" + part.charAt(0);
            minutes = part.substring(1);
        } else if (len >= 4) {
            hours = part.substring(0, 2);
            minutes = part.substring(2, Math.min(4, len));
        }

        return hours + ":" + minutes;
    }
}
