package com.example.chelonia.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chelonia.fragments.Calendar.MonthFragment;
import com.example.chelonia.fragments.Calendar.TodayFragment;
import com.example.chelonia.fragments.Calendar.TomorrowFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TodayFragment();
            case 1:
                return new TomorrowFragment();
            default:
                return new MonthFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
