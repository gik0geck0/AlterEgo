package edu.mines.alterego;

import android.database.Cursor;

interface ModelInitializer<T> {
    public T initialize(Cursor c);
}
