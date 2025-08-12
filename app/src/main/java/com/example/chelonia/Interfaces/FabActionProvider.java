package com.example.chelonia.Interfaces;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public interface FabActionProvider {
    /**
     * Вызывается, когда пользователь нажал FAB.
     * @return true если фрагмент обработал действие (тогда MainActivity ничего не делает),
     *         false если не обработал (MainActivity может применить запасной сценарий).
     */
    boolean onFabClick();

    // опционально — как должна выглядеть кнопка (MainActivity может использовать или нет)
    @DrawableRes
    default int getFabIconRes() { return -1; } // -1 — использовать дефолт

    @ColorRes
    default int getFabTintColorRes() { return -1; } // -1 — дефолт

    default boolean isFabVisible() { return true; } // видимость FAB
}
