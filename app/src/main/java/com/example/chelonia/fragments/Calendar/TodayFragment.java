package com.example.chelonia.fragments.Calendar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chelonia.R;
import com.example.chelonia.adapters.NoteAdapter;
import com.example.chelonia.information.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    public TodayFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Note> noteList = getNoteList();
        NoteAdapter noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);
        updateDateTime(view);
        return view;
    }

    private List<Note> getNoteList(){
        List<Note> notes = new ArrayList<>();

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        notes.add(new Note("first note","","12:00",
                true, 12, 0, 0));

        return notes;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimeUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    private void startTimeUpdates() {
        long currentMillis = System.currentTimeMillis();
        long delay = 60000 - (currentMillis % 60000);

        timeRunnable = new Runnable() {
            @Override
            public void run() {
                View rootView = getView();
                if (rootView != null) {
                    updateDateTime(rootView);
                }
                timeHandler.postDelayed(this, 60000);
            }
        };
        timeHandler.postDelayed(timeRunnable, delay);
    }

    private void stopTimeUpdates() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void updateDateTime(View view) {
        TextView dayOfWeekTextView = view.findViewById(R.id.dayOfWeek);
        TextView dateNumberTextView = view.findViewById(R.id.dateNumber);
        TextView monthTextView = view.findViewById(R.id.month);
        TextView timeTextView = view.findViewById(R.id.time);
        TextView countryTextView = view.findViewById(R.id.country);

        Calendar calendar = Calendar.getInstance();
        Locale locale = Locale.getDefault();

        Locale localeCountry = Locale.getDefault();

        String dayOfWeek = new SimpleDateFormat("EEEE", locale).format(calendar.getTime());
        int dayNum = Integer.parseInt(new SimpleDateFormat("d", locale).format(calendar.getTime()));
        String ordinalDate = getOrdinalFor(dayNum);
        String month = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
        String time = new SimpleDateFormat("HH:mm", locale).format(calendar.getTime());
        String country = localeCountry.getDisplayCountry();

        dayOfWeekTextView.setText(capitalize(dayOfWeek));
        dateNumberTextView.setText(ordinalDate);
        monthTextView.setText(capitalize(month.toLowerCase()));
        timeTextView.setText(time);
        countryTextView.setText(country);
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    private String getOrdinalFor(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return number + "th";
        }
        switch (number % 10) {
            case 1: return number + "st";
            case 2: return number + "nd";
            case 3: return number + "rd";
            default: return number + "th";
        }
    }
}
