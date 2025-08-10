package com.example.chelonia.utils;

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
     * @param current поле, к которому привязан watcher
     * @param next    следующее поле (может быть null)
     * @param isHour  true если поле часов (0-23), false если минуты (0-59)
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

                if (digits.length() == 2) {
                    int val = Integer.parseInt(digits);
                    if (isHour) {
                        if (val > 23) val = 23;
                    } else {
                        if (val > 59) val = 59;
                    }
                    digits = pad2(val);
                }

                if (!digits.equals(s.toString())) {
                    current.setText(digits);
                }
                current.setSelection(digits.length());

                // теперь проверка на авто-переход
                if (autoFocusNext && digits.length() == 2 && next != null) {
                    next.requestFocus();
                    next.setSelection(next.getText().length());
                }

                isEditing = false;
            }
        };
    }


    /**
     * Устанавливаем поведение при потере фокуса: дозаполняем нулями и ограничиваем диапазон.
     */
    public static void attachFocusFormatter(final EditText et, final boolean isHour) {
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String raw = et.getText().toString().replaceAll("[^0-9]", "");
                    if (raw.isEmpty()) {
                        // если часы — 00, минуты — 00
                        et.setText("00");
                        return;
                    }
                    int val = Integer.parseInt(raw);
                    if (isHour) {
                        if (val < 0) val = 0;
                        if (val > 23) val = 23;
                    } else {
                        if (val < 0) val = 0;
                        if (val > 59) val = 59;
                    }
                    et.setText(pad2(val));
                }
            }
        });
    }

    private static String pad2(int v) {
        if (v < 10) return "0" + v;
        return String.valueOf(v);
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
