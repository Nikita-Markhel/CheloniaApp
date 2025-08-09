package com.example.chelonia.fragments.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.R;
import com.example.chelonia.adapters.TabsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TabsPagerAdapter adapter;
    private ViewPager2 viewPager;
    private static final String ACTION_REGISTRATION_COMPLETE = "com.example.chelonia.REGISTRATION_COMPLETE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    private TextView primaryWelcomeMessage;
    private TextView secondaryWelcomeMessage;
    private ImageView avatarImageView;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        primaryWelcomeMessage = view.findViewById(R.id.welcome_message);
        secondaryWelcomeMessage = view.findViewById(R.id.welcome_message2);
        avatarImageView = view.findViewById(R.id.imageView);

        updateUserData();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                registrationReceiver,
                new IntentFilter(ACTION_REGISTRATION_COMPLETE)
        );

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        // Используем поля класса, убираем тип (var)
        viewPager = view.findViewById(R.id.viewPager);

        adapter = new TabsPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View customTabView = null;
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            switch (position) {
                case 0:
                    customTabView = inflater.inflate(R.layout.layout_tab_today, null);
                    break;
                case 1:
                    customTabView = inflater.inflate(R.layout.layout_tab_tomorrow, null);
                    break;
                case 2:
                    customTabView = inflater.inflate(R.layout.layout_tab_month, null);
                    break;
            }
            tab.setCustomView(customTabView);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabText = customView.findViewById(R.id.tabText);
                    if (tabText != null) {
                        tabText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                    }
                    customView.setSelected(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabText = customView.findViewById(R.id.tabText);
                    if (tabText != null) {
                        tabText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    }
                    customView.setSelected(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }

    @Nullable
    public NoteEditable getActiveNoteEditable() {
        if (viewPager == null || adapter == null) return null;
        int pos = viewPager.getCurrentItem();
        Log.d("CalendarFragment", "Current ViewPager position: " + pos);
        Fragment f = adapter.getFragmentIfExists(pos);
        Log.d("CalendarFragment", "Fragment at pos: " + (f != null ? f.getClass().getSimpleName() : "null"));
        if (f instanceof NoteEditable) {
            return (NoteEditable) f;
        }
        return null;
    }


    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(registrationReceiver);
        super.onDestroyView();
    }

    private final BroadcastReceiver registrationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUserData();
        }
    };

    public void updateUserData() {

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String userFirstName = prefs.getString("firstName", getString(R.string.unknown_first_name));
        String avatarUriString = prefs.getString("avatarUri", "");

        int currentHour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));

        String greetingPrimary, greetingSecondary;
        int iconRes;

        if (currentHour >= 4 && currentHour <= 12) {
            greetingPrimary = getString(R.string.good_morning_msg);
            greetingSecondary = getString(R.string.good_morning_msg2);
            iconRes = R.drawable.ic_sunrise;
        } else if (currentHour > 12 && currentHour < 18) {
            greetingPrimary = getString(R.string.good_day_msg);
            greetingSecondary = getString(R.string.good_day_msg2);
            iconRes = R.drawable.ic_sun;
        } else if (currentHour >= 18 && currentHour <= 23) {
            greetingPrimary = getString(R.string.good_evening_msg);
            greetingSecondary = getString(R.string.good_evening_msg2);
            iconRes = R.drawable.ic_sunset;
        } else {
            greetingPrimary = getString(R.string.good_night_msg);
            greetingSecondary = getString(R.string.good_night_msg2);
            iconRes = R.drawable.ic_moon;
        }

        String greetingText = greetingPrimary + " " + userFirstName;
        primaryWelcomeMessage.setText(greetingText);
        secondaryWelcomeMessage.setText(greetingSecondary);

        Drawable icon = ContextCompat.getDrawable(requireContext(), iconRes);
        if (icon != null) {
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            primaryWelcomeMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        }

        if (!avatarUriString.isEmpty()) {
            avatarImageView.setImageURI(Uri.parse(avatarUriString));
        } else {
            avatarImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
