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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chelonia.Interfaces.FabActionProvider;
import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.R;
import com.example.chelonia.adapters.TabsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment implements FabActionProvider {

    public static final String ACTION_REGISTRATION_COMPLETE = "com.example.chelonia.REGISTRATION_COMPLETE";
    public static final String ACTION_HEADER_TOGGLE = "com.example.chelonia.ACTION_HEADER_TOGGLE";
    public static final String EXTRA_HEADER_HIDE = "extra_header_hide";

    private TabsPagerAdapter adapter;
    private ViewPager2 viewPager;

    private TextView primaryWelcomeMessage;
    private TextView secondaryWelcomeMessage;
    private ImageView avatarImageView;

    // views to animate
    private View headView;
    private View headerLayoutView;
    private int headerPeekPx = 0;
    private View headerContent;

    private boolean headerHidden = false;
    private int headHeight = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    private final BroadcastReceiver registrationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUserData();
        }
    };

    private final BroadcastReceiver headerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_HEADER_TOGGLE.equals(intent.getAction())) return;
            boolean hide = intent.getBooleanExtra(EXTRA_HEADER_HIDE, false);
            // avoid duplicate requests
            if (hide == headerHidden) return;
            animateHeader(hide, true);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        primaryWelcomeMessage = view.findViewById(R.id.welcome_message);
        secondaryWelcomeMessage = view.findViewById(R.id.welcome_message2);
        avatarImageView = view.findViewById(R.id.imageView);

        headView = view.findViewById(R.id.constraintLayoutHead);
        headerLayoutView = view.findViewById(R.id.header_layout);
        headView = view.findViewById(R.id.constraintLayoutHead); // фон
        headerContent = view.findViewById(R.id.headerContent);

        headerPeekPx = dpToPx();
        headView.post(() -> headHeight = headView.getHeight());

        updateUserData();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                registrationReceiver,
                new IntentFilter(ACTION_REGISTRATION_COMPLETE)
        );

        // listen for header toggle requests from children
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                headerReceiver,
                new IntentFilter(ACTION_HEADER_TOGGLE)
        );

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
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
                if (getActivity() instanceof com.example.chelonia.MainActivity) {
                    ((com.example.chelonia.MainActivity) getActivity()).refreshFabAppearance();
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

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (getActivity() instanceof com.example.chelonia.MainActivity) {
                    ((com.example.chelonia.MainActivity) getActivity()).refreshFabAppearance();
                }
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
    public boolean onFabClick() {
        NoteEditable active = getActiveNoteEditable();
        if (active != null) {
            active.addEditableNote();
            return true;
        }
        return false;
    }

    @Override
    public int getFabIconRes() {
        if (viewPager == null || adapter == null) return -1;
        Fragment f = adapter.getFragmentIfExists(viewPager.getCurrentItem());
        if (f instanceof FabActionProvider) {
            return ((FabActionProvider) f).getFabIconRes();
        }
        return -1;
    }

    @Override
    public int getFabTintColorRes() {
        if (viewPager == null || adapter == null) return -1;
        Fragment f = adapter.getFragmentIfExists(viewPager.getCurrentItem());
        if (f instanceof FabActionProvider) {
            return ((FabActionProvider) f).getFabTintColorRes();
        }
        return -1;
    }

    @Override
    public boolean isFabVisible() {
        if (viewPager == null || adapter == null) return true;
        Fragment f = adapter.getFragmentIfExists(viewPager.getCurrentItem());
        if (f instanceof FabActionProvider) {
            return ((FabActionProvider) f).isFabVisible();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // improve keyboard behaviour: allow resize (helps when editing notes)
        try {
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } catch (Exception ignored) { }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(registrationReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(headerReceiver);
        } catch (Exception ignored) {}
    }

    private void updateUserData() {
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

    /**
     * Анимированно скрывает/показывает шапку (constraintLayoutHead + header_layout + viewPager)
     * @param hide true = поднять шапку вверх (скрыть), false = опустить (показать)
     * @param animate true = анимировать
     */

    private void animateHeader(boolean hide, boolean animate) {
        if (headView == null || headerLayoutView == null || viewPager == null) return;

        if (headHeight <= 0) {
            headView.post(() -> {
                headHeight = headView.getHeight();
                animateHeader(hide, animate);
            });
            return;
        }

        int clampPeek = Math.min(headerPeekPx, headHeight);
        float targetY = hide ? -(headHeight - clampPeek) : 0f;
        long duration = animate ? 220L : 0L;

        // фон остаётся всегда, только двигается
        headView.animate().translationY(targetY).setDuration(duration).start();
        headerLayoutView.animate().translationY(targetY).setDuration(duration).start();
        viewPager.animate().translationY(targetY).setDuration(duration).start();

        // исчезают только надписи
        if (headerContent != null) {
            float targetAlpha = hide ? 0f : 1f;
            headerContent.animate().alpha(targetAlpha).setDuration(duration).start();
        }

        headerHidden = hide;
    }

    private int dpToPx() {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(17 * d);
    }

}
