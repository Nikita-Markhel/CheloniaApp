package com.example.chelonia.adapters;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chelonia.fragments.Calendar.MonthFragment;
import com.example.chelonia.fragments.Calendar.TodayFragment;
import com.example.chelonia.fragments.Calendar.TomorrowFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    private final SparseArray<Fragment> fragmentRefs = new SparseArray<>();

    public TabsPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment f;
        switch (position) {
            case 0:
                f = new TodayFragment();
                break;
            case 1:
                f = new TomorrowFragment();
                break;
            case 2:
            default:
                f = new MonthFragment(); // пример
                break;
        }
        // кэшируем фрагмент
        fragmentRefs.put(position, f);
        return f;
    }

    @Override
    public int getItemCount() {
        return 3; // количество вкладок
    }

    @Nullable
    public Fragment getFragmentIfExists(int position) {
        return fragmentRefs.get(position);
    }

    public void removeFragmentRef(int position) {
        fragmentRefs.remove(position);
    }
}

