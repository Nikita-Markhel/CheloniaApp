package com.example.chelonia.adapters;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chelonia.fragments.Calendar.MonthFragment;
import com.example.chelonia.fragments.Calendar.TodayFragment;
import com.example.chelonia.fragments.Calendar.TomorrowFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    private final SparseArray<Fragment> fragmentRefs = new SparseArray<>();
    private final FragmentManager fragmentManager;

    public TabsPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
        this.fragmentManager = fa.getSupportFragmentManager();
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
                f = new MonthFragment();
                break;
        }
        fragmentRefs.put(position, f);
        return f;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Nullable
    public Fragment getFragmentIfExists(int position) {
        Fragment f = fragmentRefs.get(position);
        if (f != null) return f;

        // Попытка найти во FragmentManager: FragmentStateAdapter использует тег вида "f" + itemId
        String tag = "f" + getItemId(position);
        Fragment fmFragment = fragmentManager.findFragmentByTag(tag);
        if (fmFragment != null) {
            // положим в кэш для будущих запросов
            fragmentRefs.put(position, fmFragment);
            return fmFragment;
        }
        return null;
    }

    public void removeFragmentRef(int position) {
        fragmentRefs.remove(position);
    }
}
