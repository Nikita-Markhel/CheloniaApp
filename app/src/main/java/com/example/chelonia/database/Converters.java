package com.example.chelonia.database;

import androidx.room.TypeConverter;
import com.example.chelonia.information.Note.NoteType;

public class Converters {

    @TypeConverter
    public static String fromNoteType(NoteType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static NoteType toNoteType(String name) {
        return name == null ? null : NoteType.valueOf(name);
    }
}
