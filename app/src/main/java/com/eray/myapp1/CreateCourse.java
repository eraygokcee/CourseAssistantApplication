package com.eray.myapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateCourse extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private EditText courseIdEditText, courseNameEditText, dateEditText, groupNumbersEditText;
    private LinearLayout groupContainer;
    private Button createCourseButton;

    private Calendar myCalendar;

    private List<List<String>> selectedTeachers; // Her grup için seçilen öğretmenlerin listesi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        selectedTeachers = new ArrayList<>();

        courseIdEditText = findViewById(R.id.courseIdEditText);
        courseNameEditText = findViewById(R.id.courseNameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        myCalendar = Calendar.getInstance();
        groupNumbersEditText = findViewById(R.id.groupNumbersEditText);
        groupContainer = findViewById(R.id.groupContainer);
        createCourseButton = findViewById(R.id.createCourseButton);

        groupNumbersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int totalGroups = Integer.parseInt(s.toString());
                addGroupViews(totalGroups);
            }
        });
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateCourse.this, dateSetListener,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        createCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCourse();
            }
        });

    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateLabel();
        }
    };

    // Seçilen tarihi EditText alanına yerleştirme
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; // Tarih formatı
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void addGroupViews(int totalGroups) {
        groupContainer.removeAllViews();
        selectedTeachers.clear(); // Her seferinde önceki seçimleri temizle
        int i;
        for ( i = 1; i <= totalGroups; i++) {
            View groupView = getLayoutInflater().inflate(R.layout.group_item, null);
            TextView groupNameTextView = groupView.findViewById(R.id.groupNameTextView);
            groupNameTextView.setText("Group " + i);

            Spinner instructorSpinner = groupView.findViewById(R.id.instructorSpinner);

            // Firestore'dan öğretmenleri al ve instructorSpinner'a ekle
            int finalI = i;
            fStore.collection("teachers")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<String> teachers = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Her belgeden öğretmenin adını ve soyadını al
                                    String teacherName = document.getString("name");
                                    String teacherSurname = document.getString("surname");
                                    // Ad ve soyadı birleştirerek öğretmenin tam adını oluştur
                                    String fullName = teacherName + " " + teacherSurname;
                                    // Öğretmen adını Spinner'a ekle
                                    teachers.add(fullName);
                                }
                                // Öğretmen adlarını ArrayAdapter kullanarak Spinner'a ekle
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateCourse.this, android.R.layout.simple_spinner_item, teachers);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                instructorSpinner.setAdapter(adapter);


                                // Öğretmen seçimi değiştiğinde seçilen öğretmeni listeye ekle
                                instructorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        String selectedTeacher = parent.getItemAtPosition(position).toString();
                                        List<String> selectedTeachersForGroup = selectedTeachers.get(finalI - 1); // Grup indeksi sıfırdan başladığı için i-1
                                        selectedTeachersForGroup.clear(); // Önceki seçimleri temizle
                                        selectedTeachersForGroup.add(selectedTeacher); // Yeni seçimi ekle
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                            } else {
                                // Hata durumunda kullanıcıya bilgi ver
                                Toast.makeText(CreateCourse.this, "Error getting teachers: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            groupContainer.addView(groupView);
            selectedTeachers.add(new ArrayList<>()); // Her grup için yeni bir öğretmen listesi oluştur
        }
    }

    private void createCourse() {
        // Kullanıcıdan gerekli bilgileri al
        String courseId = courseIdEditText.getText().toString();
        String courseName = courseNameEditText.getText().toString();
        String date = dateEditText.getText().toString();
        int totalGroups = Integer.parseInt(groupNumbersEditText.getText().toString());

        // Yeni bir kurs oluşturulacak koleksiyon referansı
        CollectionReference courseRef = fStore.collection("courses");

        // Her bir grup için bir alt koleksiyon oluştur


        // Kurs bilgilerini bir Map'e ekle
        Map<String, Object> course = new HashMap<>();
        course.put("courseId", courseId);
        course.put("courseName", courseName);
        course.put("date", date);

        // Kurs koleksiyonuna kurs bilgilerini ekle
        courseRef.add(course)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        for (int i = 1; i <= totalGroups; i++) {
                            // Grup koleksiyonu için referans oluştur
                            CollectionReference groupRef = documentReference.collection("groups");

                            // Grup bilgilerini bir Map'e ekle
                            Map<String, Object> group = new HashMap<>();
                            group.put("groupName", "Group " + i); // Örnek: Group 1, Group 2, ...
                            group.put("numberOfStudents", 0); // Başlangıçta hiç öğrenci yok
                            group.put("instructor", selectedTeachers.get(i - 1).get(0)); // Seçilen öğretmeni ekle

                            // Grup koleksiyonuna grup bilgilerini ekle
                            groupRef.add(group)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // Grup başarıyla eklendiğinde buraya işlemler eklenebilir
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Hata durumunda kullanıcıya bilgi ver
                                            Toast.makeText(CreateCourse.this, "Error adding group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        // Başarılı olduğunda kurs ID'sini al ve kullanıcıya bilgi ver
                        Toast.makeText(CreateCourse.this, "Course and groups created successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hata durumunda kullanıcıya bilgi ver
                        Toast.makeText(CreateCourse.this, "Error creating course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
