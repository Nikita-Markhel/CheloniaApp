package com.example.chelonia.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class TimeInputHelper {

    /**
     * Watcher без автодополнения нулями.
     * Только ограничение до 2 символов + автофокус на следующее поле.
     */
    public static TextWatcher getSimpleWatcher(
            final EditText current,
            final EditText next,
            final boolean autoFocusNext
    ) {
        return new TextWatcher() {
            boolean isEditing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 2) digits = digits.substring(0, 2);

                if (!digits.equals(s.toString())) {
                    current.setText(digits);
                }

                current.setSelection(digits.length());

                // Переброс фокуса
                if (autoFocusNext && digits.length() == 2 && next != null) {
                    next.post(() -> {
                        next.requestFocus();
                        int len = next.getText() != null ? next.getText().length() : 0;
                        next.setSelection(len);
                    });
                }

                isEditing = false;
            }
        };
    }

    /**
     * Обработка Backspace: если поле пустое → переводим фокус на предыдущее.
     */
    public static void attachBackspaceHandler(final EditText current, final EditText prev) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    current.getText().toString().isEmpty() &&
                    prev != null) {
                prev.requestFocus();
                int len = prev.getText() != null ? prev.getText().length() : 0;
                prev.setSelection(len);
                return true;
            }
            return false;
        });
    }

    /**
     * При первом клике на любое поле времени — переводим фокус на startHour + сразу открываем клавиатуру.
     */

    public static void redirectFirstClick(EditText first, EditText... others) {
        View.OnTouchListener listener = (v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                // Фокус на первый EditText
                first.requestFocus();
                first.setSelection(first.getText().length());

                // Accessibility + убираем warning
                v.performClick();
                first.performClick();

                // Мгновенное открытие клавиатуры
                InputMethodManager imm = (InputMethodManager)
                        v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(first, InputMethodManager.SHOW_IMPLICIT);
                }

                return true;
            }
            return false;
        };
        for (EditText et : others) {
            et.setOnTouchListener(listener);
        }
    }

    /**
     * Построение времени для сохранения.
     * Часы обязательны. Если только минуты → возвращаем null.
     */
    public static String buildTimeForSave(EditText hourEt, EditText minEt) {
        String h = hourEt.getText().toString().replaceAll("[^0-9]", "");
        String m = minEt.getText().toString().replaceAll("[^0-9]", "");

        // оба пустые → нет времени
        if (h.isEmpty() && m.isEmpty()) return null;

        // только минуты без часов → игнорируем
        if (h.isEmpty()) return null;

        int hh, mm = 0;
        try {
            hh = Integer.parseInt(h);
            if (!m.isEmpty()) {
                mm = Integer.parseInt(m);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        if (hh > 23) hh = 23;
        if (mm > 59) mm = 59;

        return pad2(hh) + ":" + pad2(mm);
    }

    private static String pad2(int v) {
        return (v < 10 ? "0" : "") + v;
    }
}
