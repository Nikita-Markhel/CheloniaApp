package com.example.chelonia.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class TimeInputHelper {

    /**
     * Возвращает TextWatcher для поля (часы/минуты).
     * Автопереход в next при достижении 2 символов.
     * При потере фокуса — дополняет нулём и корректирует диапазон.
     *
     * @param current    поле, к которому привязан watcher
     * @param next       следующее поле (может быть null)
     * @param isHour     true если поле часов (0-23), false если минуты (0-59)
     * @param autoFocusNext true, если нужно переключаться на следующее поле при вводе 2 символов
     */
    public static TextWatcher getTwoDigitWatcher(
            final EditText current,
            final EditText next,
            final boolean isHour,
            final boolean autoFocusNext
    ) {
        return new TextWatcher() {
            boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 2) digits = digits.substring(0, 2);

                // Парсим и ограничиваем диапазон, если есть 2 символа
                if (digits.length() == 2) {
                    int val = Integer.parseInt(digits);
                    if (isHour) {
                        if (val > 23) val = 23;
                    } else {
                        if (val > 59) val = 59;
                    }
                    digits = pad2(val);
                }

                // Обновляем поле только если изменился текст
                if (!digits.equals(s.toString())) {
                    current.setText(digits);
                }

                // Устанавливаем курсор в конец
                current.setSelection(digits.length());

                // Авто-переход на следующее поле
                if (autoFocusNext && digits.length() == 2 && next != null) {
                    // Используем post, чтобы избежать конфликтов с текущим событием ввода
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
     * Устанавливаем поведение при потере фокуса: дозаполняем нулями и ограничиваем диапазон.
     */
    public static void attachFocusFormatter(final EditText et, final boolean isHour) {
        et.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String raw = et.getText().toString().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) {
                    // если часы — 00, минуты — 00
                    if (!"00".equals(et.getText().toString())) {
                        et.setText("00");
                    }
                    return;
                }
                int val;
                try {
                    val = Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    val = 0;
                }
                if (isHour) {
                    if (val < 0) val = 0;
                    if (val > 23) val = 23;
                } else {
                    if (val < 0) val = 0;
                    if (val > 59) val = 59;
                }
                String padded = pad2(val);
                if (!padded.equals(et.getText().toString())) {
                    et.setText(padded);
                }
            }
        });
    }

    private static String pad2(int v) {
        return (v < 10 ? "0" : "") + v;
    }

    /**
     * Собирает строку времени "HH:mm" из двух полей (hour,min).
     * Если оба поля пустые — возвращает null.
     */
    public static String buildTimeFromFields(EditText hourEt, EditText minEt) {
        String h = hourEt.getText().toString().replaceAll("[^0-9]", "");
        String m = minEt.getText().toString().replaceAll("[^0-9]", "");
        if (h.isEmpty() && m.isEmpty()) return null;

        int hh = 0, mm = 0;
        try {
            if (!h.isEmpty()) hh = Integer.parseInt(h);
            if (!m.isEmpty()) mm = Integer.parseInt(m);
        } catch (NumberFormatException e) {
            // ignore -> use zeros
        }
        if (hh < 0) hh = 0; if (hh > 23) hh = 23;
        if (mm < 0) mm = 0; if (mm > 59) mm = 59;

        return pad2(hh) + ":" + pad2(mm);
    }
}
