package com.eray.myapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyCourses extends AppCompatActivity {

    public static final String TAG = "ERAY";
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private LinearLayout linearCoursesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        linearCoursesContainer = findViewById(R.id.linearCoursesContainer);

        // Kullanıcının adını al
        String teacherUid = fAuth.getCurrentUser().getUid();
        fStore.collection("teachers").document(teacherUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String teacherName = document.getString("name");
                                String teacherSurname = document.getString("surname");
                                // Ad ve soyadı birleştirerek öğretmenin tam adını oluştur
                                String fullName = teacherName + " " + teacherSurname;
                                fStore.collectionGroup("groups")
                                        .whereEqualTo("instructor", fullName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        // Her bir dokümandan kurs ID'sini al
                                                        String courseId = document.getReference().getParent().getParent().getId();
                                                        // Kurs adını almak için kurs ID'siyle ikinci bir sorgu yap
                                                        getCourseName(courseId);
                                                    }
                                                } else {

                                                    // Hata durumunda kullanıcıya bilgi ver
                                                    Toast.makeText(MyCourses.this, "Error getting assigned courses: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // Belirtilen öğretmen kaydı bulunamadı
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                            // Hata durumunda kullanıcıya bilgi ver
                            Toast.makeText(MyCourses.this, "Error getting teacher info: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Kullanıcının adına göre atanmış grupları Firestore'dan al

    }

    // Kurs adını almak için Firestore'dan sorgu yap
    private void getCourseName(String courseId) {
        fStore.collection("courses")
                .document(courseId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String courseName = document.getString("courseName");
                                String coursesId = document.getString("courseId");
                                String courseDate = document.getString("date");
                                // Kurs adını aldıktan sonra kurs bilgilerini layout'a ekle
                                addCourseToLayout(coursesId, courseName, courseDate);
                            }
                        } else {
                            // Hata durumunda kullanıcıya bilgi ver
                            Toast.makeText(MyCourses.this, "Error getting course name: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Kurs bilgilerini layout'a ekleyen metot
    private void addCourseToLayout(String courseId, String courseName, String courseDate) {
        View courseView = LayoutInflater.from(this).inflate(R.layout.item_course_teacher, null);
        TextView textViewCourseName = courseView.findViewById(R.id.textViewCourseName);
        TextView textViewCourseId = courseView.findViewById(R.id.textViewCourseId);
        TextView textViewDate = courseView.findViewById(R.id.textViewDate);
        LinearLayout expandedView = courseView.findViewById(R.id.expandedView);

        // Kurs bilgilerini TextView'lere yerleştir
        textViewCourseName.setText("Course Name: " + courseName);
        textViewCourseId.setText("Course ID: " + courseId);
        textViewDate.setText("Course Date : " + courseDate);

        // Layout'a kurs görünümünü ekle
        linearCoursesContainer.addView(courseView);

        // Kurs öğesine tıklanabilirlik eklemek için onClickListener'ı ayarla
        courseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandedView.getVisibility() == View.VISIBLE) {
                    expandedView.setVisibility(View.GONE);
                } else {
                    expandedView.setVisibility(View.VISIBLE);
                    // Öğrenci ekleme bileşenlerini burada oluşturabilirsiniz
                    addStudentSelectionSpinner(expandedView, courseId);
                }
            }
        });
    }

    // Öğrenci seçimi için spinner bileşenini oluşturan metot
    private void addStudentSelectionSpinner(LinearLayout container, String courseId) {
        if (container.getChildCount() > 1) {
            return;
        }
        SingleSelectionSpinner spinner = new SingleSelectionSpinner(this);
        List<String> students = new ArrayList<>();
        // Firestore'dan öğrencileri al
        fStore.collection("students")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // Her bir dokümandan öğrenci adını al
                                String studentName = document.getString("name");
                                String studentSurname = document.getString("surname");
                                String fullName = studentName + " " + studentSurname;

                                // Öğrenci adını listeye ekle
                                students.add(fullName);
                            }
                            // Öğrenci adlarını Spinner'a ekle
                            spinner.setItems(students);
                            // Spinner'ı layout'a ekle
                            container.addView(spinner);
                        } else {
                            // Hata durumunda kullanıcıya bilgi ver
                            Toast.makeText(MyCourses.this, "Error getting students: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Öğrenci seçimi yapıldığında kaydetme işlemini gerçekleştir
        spinner.setOnItemSelectedListener(new SingleSelectionSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStudent = students.get(position);
                saveSelectedStudent(selectedStudent, courseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Hiçbir öğrenci seçilmediğinde yapılacak işlemler
            }
        });
    }


    // Seçilen öğrenciyi Firestore'a kaydeden metod
    // Seçilen öğrenciyi Firestore'a kaydeden metod
    // Seçilen öğrenciyi Firestore'a kaydeden metod
    // Seçilen öğrenciyi Firestore'a kaydeden metod
    private void saveSelectedStudent(String selectedStudent, String ccourseId) {
        // Kullanıcının adını al
        String teacherUid = fAuth.getCurrentUser().getUid();
        // Kullanıcının adına göre atanmış kursları Firestore'dan al
        fStore.collection("teachers").document(teacherUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String teacherName = document.getString("name");
                                String teacherSurname = document.getString("surname");
                                // Ad ve soyadı birleştirerek öğretmenin tam adını oluştur
                                String fullName = teacherName + " " + teacherSurname;

                                // Seçilen öğrenciyi selectedStudents koleksiyonuna ekleyerek Firestore'a kaydet
                                fStore.collectionGroup("groups")
                                        .whereEqualTo("instructor", fullName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        String courseId = document.getReference().getParent().getParent().getId();
                                                        String groupId = document.getReference().getId();
                                                        fStore.collection("courses")
                                                                .whereEqualTo("courseId", ccourseId)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            for (DocumentSnapshot document : task.getResult()) {
                                                                                DocumentReference selectedStudentsRef = document
                                                                                        .getReference() // Belge referansını al
                                                                                        .collection("groups") // groups koleksiyonuna ulaş
                                                                                        .document(groupId); // Grup belgesine ulaş
                                                                                selectedStudentsRef.collection("students").document().set(new HashMap<String, Object>() {{
                                                                                            put("name", selectedStudent);
                                                                                        }})
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Log.d(TAG, "Selected student saved successfully");
                                                                                                Toast.makeText(MyCourses.this, "Selected student saved successfully", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Log.e(TAG, "Error saving selected student: " + e.getMessage());
                                                                                                Toast.makeText(MyCourses.this, "Error saving selected student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                // Öğrenciyi belgeye ekleme işlemi burada devam eder
                                                                                // ...
                                                                            }
                                                                        } else {
                                                                            // Hata durumunda kullanıcıya bilgi ver
                                                                            Toast.makeText(MyCourses.this, "Error getting document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });


                                                        // Öğrenciyi belgeye ekle

                                                    }
                                                } else {
                                                    // Hata durumunda kullanıcıya bilgi ver
                                                    Toast.makeText(MyCourses.this, "Error getting assigned courses: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Hata durumunda kullanıcıya bilgi ver
                            Toast.makeText(MyCourses.this, "Error getting teacher info: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}

