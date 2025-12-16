package com.example.miniapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Note(val id: Int, val content: String, val category: String)

class NoteDbHelper(context: Context) : SQLiteOpenHelper(context, "Memo.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, category TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS notes")
        onCreate(db)
    }

    // 插入数据
    fun addNote(content: String, category: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("content", content)
            put("category", category)
        }
        db.insert("notes", null, values)
        db.close()
    }

    // 删除数据
    fun deleteNote(id: Int) {
        val db = writableDatabase
        db.delete("notes", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    // 查询所有数据
    fun getAllNotes(): List<Note> {
        val list = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM notes ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            list.add(Note(id, content, category))
        }
        cursor.close()
        db.close()
        return list
    }

    // 获取所有分类
    fun getAllCategories(): List<String> {
        val categories = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT category FROM notes ORDER BY category", null)

        while (cursor.moveToNext()) {
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            if (!category.isNullOrEmpty()) {
                categories.add(category)
            }
        }
        cursor.close()
        db.close()
        return categories
    }
}