package com.example.chelonia.database;

import androidx.room.*;
import com.example.chelonia.information.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE dateMillis >= :start AND dateMillis < :end")
    List<Note> getNotesBetween(long start, long end);

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE type = :type")
    List<Note> getNotesByType(String type);

    @Query("DELETE FROM notes")
    void deleteAll();
}
