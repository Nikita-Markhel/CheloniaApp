package com.example.chelonia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chelonia.R;
import com.example.chelonia.information.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    public NoteAdapter(List<Note> noteList){
        this.noteList = noteList;
    }


    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.timeTitle.setText(note.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle;
        TextView timeTitle;

        public NoteViewHolder(View itemView){
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title);
            timeTitle = itemView.findViewById(R.id.note_time);
        }

    }
}
