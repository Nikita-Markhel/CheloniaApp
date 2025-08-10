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

public class TodayFragment extends Fragment implements NoteEditable {

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;
    private boolean isEditing = false;

    private List<Note> todayNotes;
    private NoteAdapter noteAdapter;

    public static TodayFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        todayNotes = getTodayNotes();
        noteAdapter = new NoteAdapter(todayNotes);
        recyclerView.setAdapter(noteAdapter);

        return view;
    }

    @Override
    public void addEditableNote() {
        if (isEditing) {
            Note editableNote = todayNotes.get(0);
            if (!editableNote.isEditable()) return;

            RecyclerView recyclerView = requireView().findViewById(R.id.notesRecyclerView);
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);

            if (holder instanceof NoteAdapter.EditableNoteViewHolder) {
                NoteAdapter.EditableNoteViewHolder editableHolder = (NoteAdapter.EditableNoteViewHolder) holder;

                String title = editableHolder.noteTitle.getText().toString().trim();
                String description = editableHolder.noteDescription.getText().toString().trim();
                if (title.isEmpty()) title = "Без названия";

                editableNote.setTitle(title);
                editableNote.setDescription(description);

// собираем start и end
                String startTimeStr = TimeInputHelper.buildTimeFromFields(editableHolder.startHour, editableHolder.startMin);
                String endTimeStr = TimeInputHelper.buildTimeFromFields(editableHolder.endHour, editableHolder.endMin);

                try {
                    Calendar base = Calendar.getInstance();
                    // если dateMillis задан для заметки — используем его как день
                    long baseDate = editableNote.getDateMillis() != null ? editableNote.getDateMillis() : getStartOfDayMillis(System.currentTimeMillis());
                    base.setTimeInMillis(baseDate);
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
                        // если нет ввода времени — ставим текущее время (поведение как раньше)
                        editableNote.setStartTimeMillis(System.currentTimeMillis());
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
                    editableNote.setStartTimeMillis(System.currentTimeMillis());
                    editableNote.setEndTimeMillis(null);
                }

                editableNote.setEditable(false);

                AppDatabase db = AppDatabase.getInstance(requireContext());
                db.noteDao().insert(editableNote);

                sortNotesByStartTime(todayNotes);
                noteAdapter.notifyDataSetChanged();

                isEditing = false;
                updateFabStyle(false);
            }

        } else {
            // Создаём новую редактируемую запись
            Note editableNote = new Note("");
            editableNote.setEditable(true);
            editableNote.setDateMillis(getStartOfDayMillis(System.currentTimeMillis()));

            todayNotes.add(0, editableNote);
            sortNotesByStartTime(todayNotes);
            noteAdapter.notifyDataSetChanged();
            noteAdapter.notifyItemInserted(0);

            isEditing = true;
            updateFabStyle(true);
        }
    }

    private List<Note> getTodayNotes() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        NoteDao noteDao = db.noteDao();

        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + 86400000L;

        return noteDao.getNotesBetween(todayStart, tomorrowStart);
    }

    private void updateTodayNotes() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        NoteDao noteDao = db.noteDao();

        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + 86400000L;

        todayNotes.clear();
        todayNotes.addAll(noteDao.getNotesBetween(todayStart, tomorrowStart));
        sortNotesByStartTime(todayNotes);
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

    @Override
    public void onResume() {
        super.onResume();
        startTimeUpdates();
        updateTodayNotes();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();

        // Если пользователь ушёл с экрана в процессе редактирования — автоматически сохраняем запись
        if (isEditing) {
            addEditableNote();
        }
    }

    private void startTimeUpdates() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                View rootView = getView();
                if (rootView != null) {
                    updateDateTime(rootView);
                }

                updateTodayNotes();

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
        TextView timeTextView = view.findViewById(R.id.time);
        TextView countryTextView = view.findViewById(R.id.country);

        Calendar calendar = Calendar.getInstance();
        Locale locale = Locale.getDefault();

        String dayOfWeek = new SimpleDateFormat("EEEE", locale).format(calendar.getTime());
        int dayNum = Integer.parseInt(new SimpleDateFormat("d", locale).format(calendar.getTime()));
        String ordinalDate = getOrdinalFor(dayNum);
        String month = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
        String time = new SimpleDateFormat("HH:mm", locale).format(calendar.getTime());
        String country = locale.getDisplayCountry();

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

    private void updateFabStyle(boolean editing) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateFabStyle(editing);
        }
    }
}
