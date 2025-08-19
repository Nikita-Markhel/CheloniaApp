package com.example.chelonia.fragments.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.chelonia.R;
import com.example.chelonia.database.AppDatabase;
import com.example.chelonia.information.Note;

import java.util.Calendar;
import java.util.List;

public class MonthFragment extends BaseNoteFragment {

    private long selectedDateMillis;

    public static MonthFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        selectedDateMillis = getStartOfDayMillis(System.currentTimeMillis());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        setupRecycler(view); // scrollListener подключается автоматически

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();
            refreshNotesForSelectedDate();
            requestFabRefresh();
        });

        return view;
    }


    private void sendHeaderToggle(boolean hide) {
        Intent intent = new Intent(CalendarFragment.ACTION_HEADER_TOGGLE);
        intent.putExtra(CalendarFragment.EXTRA_HEADER_HIDE, hide);
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    @Override
    protected List<Note> loadNotes() {
        return getNotesForDate(selectedDateMillis);
    }

    @Override
    protected long getDefaultDateMillisForNewNote() {
        return selectedDateMillis;
    }

    @Override
    protected long getBaseDateMillisForSaving(Note editableNote) {
        return selectedDateMillis;
    }

    private List<Note> getNotesForDate(long dateMillis) {
        long start = getStartOfDayMillis(dateMillis);
        long end = start + 86400000L;
        return AppDatabase.getInstance(requireContext()).noteDao().getNotesBetween(start, end);
    }

    private void refreshNotesForSelectedDate() {
        if (notes == null) return;
        notes.clear();
        notes.addAll(getNotesForDate(selectedDateMillis));
        sortNotesByStartTime(notes);
        if (noteAdapter != null) noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoUpdate();
        refreshNotesForSelectedDate();
        requestFabRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoUpdate();
        if (isEditing) addEditableNote();
    }

    private void startAutoUpdate() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                refreshNotesForSelectedDate();
                timeHandler.postDelayed(this, 60_000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void stopAutoUpdate() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }
}
