package com.example.lb7_test;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText inputNote;
    private Button btnSave;
    private ListView noteList;
    private ArrayList<String> data;
    private ArrayAdapter<String> adapter;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "notes_prefs";
    private static final String KEY_DRAFT = "draftNote";
    private static final String KEY_LIST = "noteList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputNote = findViewById(R.id.inputNote);
        btnSave = findViewById(R.id.btnSave);
        noteList = findViewById(R.id.noteList);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (savedInstanceState != null) {
            data = savedInstanceState.getStringArrayList("noteList");
            if (data == null) data = new ArrayList<>();
            inputNote.setText(savedInstanceState.getString("draftNote", ""));
        } else {
            data = new ArrayList<>(preferences.getStringSet(KEY_LIST, new java.util.HashSet<>()));
            inputNote.setText(preferences.getString(KEY_DRAFT, ""));
            if (data.isEmpty()) {
                data.add("Заметка №1");
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        noteList.setAdapter(adapter);

        // Добавление новой заметки
        btnSave.setOnClickListener(v -> {
            String note = inputNote.getText().toString().trim();
            if (!note.isEmpty()) {
                data.add(0, note);
                adapter.notifyDataSetChanged();
                inputNote.setText("");
            }
        });

        // Удалить / Редактировать
        noteList.setOnItemLongClickListener((parent, view, position, id) -> {
            String selected = data.get(position);

            String[] options = {"Удалить", "Редактировать"};
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Выберите действие")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Удаление заметки
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.confirm_delete_title)
                                    .setMessage(getString(R.string.confirm_delete_message) + "\n\n\"" + selected + "\"")
                                    .setPositiveButton(R.string.yes, (d, w) -> {
                                        data.remove(position);
                                        adapter.notifyDataSetChanged();
                                    })
                                    .setNegativeButton(R.string.no, null)
                                    .show();
                        } else if (which == 1) {
                            // Редактирование заметки
                            EditText editText = new EditText(MainActivity.this);
                            editText.setText(selected);

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Редактировать заметку")
                                    .setView(editText)
                                    .setPositiveButton("Сохранить", (d, w) -> {
                                        String newText = editText.getText().toString().trim();
                                        if (!newText.isEmpty()) {
                                            data.set(position, newText);
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                        }
                    })
                    .show();

            return true;
        });
    }

    // Сохраняем данные при смене ориентации
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("draftNote", inputNote.getText().toString());
        outState.putStringArrayList("noteList", data);
    }

    // Сохраняем черновик и список заметок при уходе из Activity
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DRAFT, inputNote.getText().toString());
        editor.putStringSet(KEY_LIST, new java.util.HashSet<>(data));
        editor.apply();
    }

    // Восстанавливаем черновик при возврате в Activity
    @Override
    protected void onResume() {
        super.onResume();
        inputNote.setText(preferences.getString(KEY_DRAFT, ""));
        java.util.Set<String> savedSet = preferences.getStringSet(KEY_LIST, new java.util.HashSet<>());
        if (!savedSet.isEmpty()) {
            data.clear();
            data.addAll(savedSet);
            adapter.notifyDataSetChanged();
        }
    }
}
