package com.example.chelonia.fragments.Calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chelonia.R;
import com.example.chelonia.database.AppDatabase;
import com.example.chelonia.information.Note;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends BaseNoteFragment {

    public static TodayFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        // Recycler + scrollListener инициализируются в BaseNoteFragment
        setupRecycler(view);
        return view;
    }

    @Override
    protected List<Note> loadNotes() {
        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + 86400000L;
        return AppDatabase.getInstance(requireContext()).noteDao().getNotesBetween(todayStart, tomorrowStart);
    }

    @Override
    protected long getDefaultDateMillisForNewNote() {
        return getStartOfDayMillis(System.currentTimeMillis());
    }

    @Override
    protected long getBaseDateMillisForSaving(Note editableNote) {
        return editableNote.getDateMillis() != null
                ? editableNote.getDateMillis()
                : getStartOfDayMillis(System.currentTimeMillis());
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimeUpdates();
        refreshNotes();
        requestFabRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
        if (isEditing) addEditableNote();
    }

    private void refreshNotes() {
        if (notes == null) return;
        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + 86400000L;
        notes.clear();
        notes.addAll(AppDatabase.getInstance(requireContext()).noteDao().getNotesBetween(todayStart, tomorrowStart));
        sortNotesByStartTime(notes);
        if (noteAdapter != null) noteAdapter.notifyDataSetChanged();
    }

    private void startTimeUpdates() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                View rootView = getView();
                if (rootView != null) updateDateTime(rootView);
                refreshNotes();
                timeHandler.postDelayed(this, 60_000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void stopTimeUpdates() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void updateDateTime(View view) {
        TextView dayOfWeekTextView = view.findViewById(R.id.dayOfWeek);
        TextView dateNumberTextView = view.findViewById(R.id.dateNumber);
        TextView monthTextView = view.findViewById(R.id.month);
        TextView countryTextView = view.findViewById(R.id.country);

        Calendar calendar = Calendar.getInstance();
        Locale locale = Locale.getDefault();

        String dayOfWeek = new SimpleDateFormat("EEEE", locale).format(calendar.getTime());
        int dayNum = Integer.parseInt(new SimpleDateFormat("d", locale).format(calendar.getTime()));
        String ordinalDate = getOrdinalFor(dayNum);
        String month = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
        String country = locale.getDisplayCountry();

        dayOfWeekTextView.setText(capitalize(dayOfWeek));
        dateNumberTextView.setText(ordinalDate);
        monthTextView.setText(capitalize(month.toLowerCase()));
        countryTextView.setText(country);
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String getOrdinalFor(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) return number + "th";
        switch (number % 10) {
            case 1: return number + "st";
            case 2: return number + "nd";
            case 3: return number + "rd";
            default: return number + "th";
        }
    }
}
