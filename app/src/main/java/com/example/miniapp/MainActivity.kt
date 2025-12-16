package com.example.miniapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: NoteDbHelper
    private lateinit var adapter: NoteAdapter
    private val noteList = mutableListOf<Note>()
    private lateinit var spinnerCategory: Spinner
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = NoteDbHelper(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter(noteList)
        recyclerView.adapter = adapter

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = if (position == 0) null else parent.getItemAtPosition(position).toString()
                refreshList()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
                refreshList()
            }
        }

        refreshCategories()
        refreshList()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val contentInput = dialogView.findViewById<EditText>(R.id.editTextContent)
        val categoryInput = dialogView.findViewById<EditText>(R.id.editTextCategory)

        AlertDialog.Builder(this)
            .setTitle("写点什么...")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val content = contentInput.text.toString()
                val category = categoryInput.text.toString().ifEmpty { "未分类" }
                if (content.isNotEmpty()) {
                    dbHelper.addNote(content, category)
                    refreshCategories()
                    refreshList()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun refreshCategories() {
        val categories = listOf("所有分类") + dbHelper.getAllCategories()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
    }

    private fun refreshList() {
        noteList.clear()
        val allNotes = dbHelper.getAllNotes()
        val filteredNotes = if (selectedCategory != null) {
            allNotes.filter { it.category == selectedCategory }
        } else {
            allNotes
        }
        noteList.addAll(filteredNotes)
        adapter.notifyDataSetChanged()
    }

    inner class NoteAdapter(private val data: List<Note>) :
        RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_note, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val note = data[position]
            holder.textViewNote.text = "[${note.category}] ${note.content}"
            holder.imageViewDelete.setOnClickListener {
                dbHelper.deleteNote(note.id)
                refreshCategories()
                refreshList()
            }
        }

        override fun getItemCount() = data.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewNote: TextView = itemView.findViewById(R.id.textViewNote)
            val imageViewDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
        }
    }
}