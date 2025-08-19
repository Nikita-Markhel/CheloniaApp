package com.example.chelonia.fragments.Calendar;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chelonia.Interfaces.FabActionProvider;
import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.MainActivity;
import com.example.chelonia.adapters.NoteAdapter;
import com.example.chelonia.database.AppDatabase;
import com.example.chelonia.information.Note;
import com.example.chelonia.utils.TimeInputHelper;

import java.util.Calendar;
import java.util.List;
public abstract class BaseNoteFragment extends Fragment implements NoteEditable, FabActionProvider {

    protected List<Note> notes;
    protected NoteAdapter noteAdapter;
    protected RecyclerView recyclerView;
    protected boolean isEditing = false;

    protected final android.os.Handler timeHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    protected Runnable timeRunnable;

    // --- новые поля для скрытия шапки ---
    private int scrollAcc = 0;
    private int scrollThresholdPx;

    protected void setupRecycler(View rootView) {
        recyclerView = rootView.findViewById(com.example.chelonia.R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notes = loadNotes();
        noteAdapter = new NoteAdapter(notes);
        recyclerView.setAdapter(noteAdapter);

        // инициализируем порог и слушатель
        scrollThresholdPx = dpToPx(80);
        attachHeaderHideOnScroll();
    }

    private void attachHeaderHideOnScroll() {
        if (recyclerView == null) return;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy == 0) return;

                if (dy > 0) {
                    // Скролл вниз → накапливаем
                    scrollAcc += dy;
                    if (scrollAcc > scrollThresholdPx) {
                        sendHeaderToggle(true); // скрыть
                        scrollAcc = 0;
                    }
                } else {
                    // Скролл вверх → проверяем, на сколько близко к началу списка
                    LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                    if (lm != null && lm.findFirstCompletelyVisibleItemPosition() == 0) {
                        // Только если вернулись в самый верх → показать
                        sendHeaderToggle(false);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrollAcc = 0;
                }
            }
        });
    }


    private void sendHeaderToggle(boolean hide) {
        Intent intent = new Intent(CalendarFragment.ACTION_HEADER_TOGGLE);
        intent.putExtra(CalendarFragment.EXTRA_HEADER_HIDE, hide);
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }

    /**
     * Загрузить список заметок для текущего состояния фрагмента.
     */
    protected abstract List<Note> loadNotes();

    /**
     * Дата (millis) которая будет ставиться новой заметке при создании (например, начало дня).
     */
    protected abstract long getDefaultDateMillisForNewNote();

    /**
     * Базовая дата для сохранения времени заметки (обычно dateMillis заметки или текущий день).
     */
    protected abstract long getBaseDateMillisForSaving(Note editableNote);

    /**
     * Универсальная реализация добавления/сохранения редактируемой заметки.
     */
    @Override
    public void addEditableNote() {
        if (isEditing) {
            // --- Сохранение редактируемой заметки ---
            if (notes == null || notes.isEmpty()) return;

            // Найдём индекс редактируемой заметки
            int editableIndex = -1;
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).isEditable()) {
                    editableIndex = i;
                    break;
                }
            }
            if (editableIndex == -1) {
                // Нечего сохранять — просто сбрасываем состояние
                isEditing = false;
                requestFabRefresh();
                return;
            }

            Note editableNote = notes.get(editableIndex);

            RecyclerView.ViewHolder holder = null;
            if (recyclerView != null) holder = recyclerView.findViewHolderForAdapterPosition(editableIndex);

            if (holder instanceof NoteAdapter.EditableNoteViewHolder) {
                NoteAdapter.EditableNoteViewHolder editableHolder =
                        (NoteAdapter.EditableNoteViewHolder) holder;

                String title = editableHolder.noteTitle.getText().toString().trim();
                String description = editableHolder.noteDescription.getText().toString().trim();
                if (title.isEmpty()) title = "Без названия";

                editableNote.setTitle(title);
                editableNote.setDescription(description);

                String startTimeStr = TimeInputHelper.buildTimeForSave(editableHolder.startHour, editableHolder.startMin);
                String endTimeStr   = TimeInputHelper.buildTimeForSave(editableHolder.endHour, editableHolder.endMin);


                try {
                    Calendar base = Calendar.getInstance();
                    long baseDate = getBaseDateMillisForSaving(editableNote);
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
                        startCal.set(Calendar.SECOND, 0);
                        startCal.set(Calendar.MILLISECOND, 0);
                        editableNote.setStartTimeMillis(startCal.getTimeInMillis());
                    } else {
                        Calendar now = Calendar.getInstance();
                        Calendar startCal = (Calendar) base.clone();
                        startCal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                        startCal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                        startCal.set(Calendar.SECOND, 0);
                        startCal.set(Calendar.MILLISECOND, 0);
                        editableNote.setStartTimeMillis(startCal.getTimeInMillis());
                    }

                    if (endTimeStr != null) {
                        String[] em = endTimeStr.split(":");
                        int eh = Integer.parseInt(em[0]);
                        int emi = Integer.parseInt(em[1]);

                        Calendar endCal = (Calendar) base.clone();
                        endCal.set(Calendar.HOUR_OF_DAY, eh);
                        endCal.set(Calendar.MINUTE, emi);
                        endCal.set(Calendar.SECOND, 0);
                        endCal.set(Calendar.MILLISECOND, 0);
                        editableNote.setEndTimeMillis(endCal.getTimeInMillis());
                    } else {
                        editableNote.setEndTimeMillis(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Calendar base = Calendar.getInstance();
                    long baseDate = getBaseDateMillisForSaving(editableNote);
                    base.setTimeInMillis(baseDate);
                    base.set(Calendar.SECOND, 0);
                    base.set(Calendar.MILLISECOND, 0);
                    Calendar now = Calendar.getInstance();
                    base.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                    base.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                    base.set(Calendar.SECOND, 0);
                    base.set(Calendar.MILLISECOND, 0);
                    editableNote.setStartTimeMillis(base.getTimeInMillis());
                    editableNote.setEndTimeMillis(null);
                }

                editableNote.setEditable(false);
                AppDatabase.getInstance(requireContext()).noteDao().insert(editableNote);

            } else {
                // Holder недоступен — завершаем редактирование с дефолтами
                if (editableNote.getTitle() == null || editableNote.getTitle().trim().isEmpty()) {
                    editableNote.setTitle("Без названия");
                }
                if (editableNote.getDescription() == null) {
                    editableNote.setDescription("");
                }

                if (editableNote.getStartTimeMillis() == null) {
                    try {
                        Calendar base = Calendar.getInstance();
                        long baseDate = getBaseDateMillisForSaving(editableNote);
                        base.setTimeInMillis(baseDate);
                        base.set(Calendar.SECOND, 0);
                        base.set(Calendar.MILLISECOND, 0);
                        Calendar now = Calendar.getInstance();
                        base.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                        base.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                        base.set(Calendar.SECOND, 0);
                        base.set(Calendar.MILLISECOND, 0);
                        editableNote.setStartTimeMillis(base.getTimeInMillis());
                    } catch (Exception ignored) {}
                }
                editableNote.setEditable(false);
                AppDatabase.getInstance(requireContext()).noteDao().insert(editableNote);
            }

            // После сохранения сортируем и обновляем список
            sortNotesByStartTime(notes);
            if (noteAdapter != null) noteAdapter.notifyDataSetChanged();

            isEditing = false;
            requestFabRefresh();

        } else {
            // --- Создание новой редактируемой заметки ---
            Note editableNote = new Note("");
            editableNote.setEditable(true);
            editableNote.setDateMillis(getDefaultDateMillisForNewNote());

            editableNote.setStartTimeMillis(null);
            editableNote.setEndTimeMillis(null);

            if (notes != null) {
                notes.add(0, editableNote); // Добавляем в начало списка без сортировки
                if (noteAdapter != null) {
                    noteAdapter.notifyItemInserted(0);
                    if (recyclerView != null) {
                        recyclerView.post(() -> recyclerView.scrollToPosition(0));
                    }
                }
            }

            isEditing = true;
            requestFabRefresh();
        }
    }

    protected void sortNotesByStartTime(List<Note> list) {
        // Изменено: заметки без времени (null) трактуем как "в конец" списка — Long.MAX_VALUE
        list.sort((n1, n2) -> {
            Long t1 = n1.getStartTimeMillis() != null ? n1.getStartTimeMillis() : Long.MAX_VALUE;
            Long t2 = n2.getStartTimeMillis() != null ? n2.getStartTimeMillis() : Long.MAX_VALUE;
            return t1.compareTo(t2);
        });
    }

    protected long getStartOfDayMillis(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    protected void requestFabRefresh() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshFabAppearance();
        }
    }

    // --- FabActionProvider defaults ---
    @Override
    public boolean onFabClick() {
        addEditableNote();
        return true;
    }

    @Override
    public int getFabIconRes() {
        return isEditing ? com.example.chelonia.R.drawable.ic_check : com.example.chelonia.R.drawable.ic_plus;
    }

    @Override
    public int getFabTintColorRes() {
        return isEditing ? com.example.chelonia.R.color.slate : com.example.chelonia.R.color.organic_pale;
    }

    @Override
    public boolean isFabVisible() {
        return true;
    }
}
