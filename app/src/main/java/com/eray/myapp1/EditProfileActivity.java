package com.eray.myapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    public static final String TAG = "ERAY";
    FirebaseAuth fAuth;
    String userID,userMail;
    FirebaseFirestore fstore;
    EditText nameEditText, surnameEditText, stdIDEditText, licenseEditText, phoneEditText;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.eName);
        surnameEditText = findViewById(R.id.eSurname);
        stdIDEditText = findViewById(R.id.eStudentID);
        licenseEditText = findViewById(R.id.eDepartment);
        phoneEditText = findViewById(R.id.ePhone);
        saveButton = findViewById(R.id.eSaveBtn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = fAuth.getCurrentUser().getUid();
                userMail = fAuth.getCurrentUser().getEmail();
                DocumentReference documentReference = fstore.collection("users").document(userID);
                Map<String,Object> user = new HashMap<>();

                String name = nameEditText.getText().toString();
                String surname = surnameEditText.getText().toString();
                String stdID = stdIDEditText.getText().toString();
                String department = licenseEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                user.put("name",name);
                user.put("surname",surname);
                user.put("stdID",stdID);
                user.put("department",department);
                user.put("phone",phone);
                user.put("email",userMail);

                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"On Success : User Profile is created for" + userID);
                    }
                });
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }
}
