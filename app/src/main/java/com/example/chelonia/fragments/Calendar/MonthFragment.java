package com.example.chelonia.fragments.Calendar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.MainActivity;
import com.example.chelonia.R;
import com.example.chelonia.adapters.NoteAdapter;
import com.example.chelonia.database.AppDatabase;
import com.example.chelonia.database.NoteDao;
import com.example.chelonia.information.Note;
import com.example.chelonia.utils.TimeInputHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthFragment extends Fragment implements NoteEditable {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> selectedDayNotes;

    private boolean isEditing = false;
    private long selectedDateMillis;

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    public static MonthFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // По умолчанию выбран сегодняшний день
        selectedDateMillis = getStartOfDayMillis(System.currentTimeMillis());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.notesRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedDayNotes = getNotesForDate(selectedDateMillis);
        noteAdapter = new NoteAdapter(selectedDayNotes);
        recyclerView.setAdapter(noteAdapter);

        // При выборе даты обновляем список
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();

            updateNotesForSelectedDate();
        });

        return view;
    }

    @Override
    public void addEditableNote() {
        if (isEditing) {
            Note editableNote = selectedDayNotes.get(0);
            if (!editableNote.isEditable()) return;

            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
            if (holder instanceof NoteAdapter.EditableNoteViewHolder) {
                NoteAdapter.EditableNoteViewHolder editableHolder = (NoteAdapter.EditableNoteViewHolder) holder;

                String title = editableHolder.noteTitle.getText().toString().trim();
                String description = editableHolder.noteDescription.getText().toString().trim();
                if (title.isEmpty()) title = "Без названия";

                editableNote.setTitle(title);
                editableNote.setDescription(description);

                String startTimeStr = TimeInputHelper.buildTimeFromFields(editableHolder.startHour, editableHolder.startMin);
                String endTimeStr = TimeInputHelper.buildTimeFromFields(editableHolder.endHour, editableHolder.endMin);

                try {
                    Calendar base = Calendar.getInstance();
                    base.setTimeInMillis(selectedDateMillis);
                    base.set(Calendar.SECOND, 0);
                    base.set(Calendar.MILLISECOND, 0);

                    if (startTimeStr != null) {
                        String[] sm = startTimeStr.split(":");
                        int sh = Integer.parseInt(sm[0]);
                        int smi = Integer.parseInt(sm[1]);

                        Calendar startCal = (Calendar) base.clone();
                        startCal.set(Calendar.HOUR_OF_DAY, sh);
                        startCal.set(Calendar.MINUTE, smi);
                        editableNote.setStartTimeMillis(startCal.getTimeInMillis());
                    } else {
                        editableNote.setStartTimeMillis(selectedDateMillis);
                    }

                    if (endTimeStr != null) {
                        String[] em = endTimeStr.split(":");
                        int eh = Integer.parseInt(em[0]);
                        int emi = Integer.parseInt(em[1]);

                        Calendar endCal = (Calendar) base.clone();
                        endCal.set(Calendar.HOUR_OF_DAY, eh);
                        endCal.set(Calendar.MINUTE, emi);
                        editableNote.setEndTimeMillis(endCal.getTimeInMillis());
                    } else {
                        editableNote.setEndTimeMillis(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    editableNote.setStartTimeMillis(selectedDateMillis);
                    editableNote.setEndTimeMillis(null);
                }

                editableNote.setEditable(false);

                AppDatabase.getInstance(requireContext()).noteDao().insert(editableNote);

                sortNotesByStartTime(selectedDayNotes);
                noteAdapter.notifyDataSetChanged();

                isEditing = false;
                updateFabStyle(false);
            }

        } else {
            Note editableNote = new Note("");
            editableNote.setEditable(true);
            editableNote.setDateMillis(selectedDateMillis);

            selectedDayNotes.add(0, editableNote);
            sortNotesByStartTime(selectedDayNotes);
            noteAdapter.notifyItemInserted(0);

            isEditing = true;
            updateFabStyle(true);
        }
    }


    private List<Note> getNotesForDate(long dateMillis) {
        long start = getStartOfDayMillis(dateMillis);
        long end = start + 86400000L;
        NoteDao noteDao = AppDatabase.getInstance(requireContext()).noteDao();
        return noteDao.getNotesBetween(start, end);
    }

    private void updateNotesForSelectedDate() {
        selectedDayNotes.clear();
        selectedDayNotes.addAll(getNotesForDate(selectedDateMillis));
        sortNotesByStartTime(selectedDayNotes);
        noteAdapter.notifyDataSetChanged();
    }

    private void sortNotesByStartTime(List<Note> notes) {
        notes.sort((n1, n2) -> {
            Long t1 = n1.getStartTimeMillis() != null ? n1.getStartTimeMillis() : 0;
            Long t2 = n2.getStartTimeMillis() != null ? n2.getStartTimeMillis() : 0;
            return t1.compareTo(t2);
        });
    }

    private long getStartOfDayMillis(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void updateFabStyle(boolean editing) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateFabStyle(editing);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoUpdate();
        updateNotesForSelectedDate();
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
                updateNotesForSelectedDate();
                timeHandler.postDelayed(this, 60_000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void stopAutoUpdate() {
        timeHandler.removeCallbacks(timeRunnable);
    }
}
