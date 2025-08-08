package com.example.chelonia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chelonia.R;
import com.example.chelonia.information.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Note> noteList;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_EDITABLE = 1;

    public NoteAdapter(List<Note> noteList) {
        this.noteList = noteList;
    }

    @Override
    public int getItemViewType(int position) {
        return noteList.get(position).isEditable() ? TYPE_EDITABLE : TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_EDITABLE) {
            View view = inflater.inflate(R.layout.note_item_edit_mode, parent, false);
            return new EditableNoteViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.note_item, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Note note = noteList.get(position);

        if (holder instanceof NoteViewHolder) {
            NoteViewHolder viewHolder = (NoteViewHolder) holder;
            viewHolder.noteTitle.setText(note.getTitle());
            viewHolder.noteDescription.setText(note.getDescription());

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = timeFormat.format(new Date(note.getStartTimeMillis() != null ? note.getStartTimeMillis() : System.currentTimeMillis()));

            if (note.getEndTimeMillis() != null) {
                time += " - " + timeFormat.format(new Date(note.getEndTimeMillis()));
            }

            viewHolder.timeTitle.setText(time);

            viewHolder.timeTitle.setText(time);
        } else if (holder instanceof EditableNoteViewHolder) {
            EditableNoteViewHolder viewHolder = (EditableNoteViewHolder) holder;
            viewHolder.noteTitle.setText(note.getTitle());
            viewHolder.noteDescription.setText(note.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void addNote(Note note) {
        noteList.add(0, note); // Добавляем в начало списка
        notifyItemInserted(0);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        TextView noteDescription;
        TextView timeTitle;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title);
            noteDescription = itemView.findViewById(R.id.description);
            timeTitle = itemView.findViewById(R.id.note_time);
        }
    }

    public static class EditableNoteViewHolder extends RecyclerView.ViewHolder {
        public EditText noteTitle;
        public EditText noteDescription;
        public EditText noteTime;

        public EditableNoteViewHolder(View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title_edit);
            noteDescription = itemView.findViewById(R.id.description_edit);
            noteTime = itemView.findViewById(R.id.note_time_edit);
        }
    }

}
