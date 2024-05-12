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

public class Register extends AppCompatActivity {
    public static final String TAG = "ERAY";
    EditText mStudentID,mEmail,mPassword,mName,mSurname;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;

    String userID;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mSurname = findViewById(R.id.surname);
        mEmail = findViewById(R.id.eMail);
        mPassword = findViewById(R.id.password);
        mStudentID = findViewById(R.id.studentID);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();



        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mName.getText().toString();
                String surname = mSurname.getText().toString();
                String stdID = mStudentID.getText().toString();


                if (TextUtils.isEmpty(email)){
                    mEmail.setError("E mail is required");
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required");
                }
                String domain = email.substring(email.indexOf("@") + 1);


                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser fuser  = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Register.this,"Verification Mail has been sent",Toast.LENGTH_SHORT).show();
                                        userID = fAuth.getCurrentUser().getUid();
                                        if(domain.equals("std.yildiz.edu.tr")){
                                            DocumentReference documentReference = fstore.collection("students").document(userID);
                                            Map<String,Object> user = new HashMap<>();
                                            user.put("name",name);
                                            user.put("surname",surname);
                                            user.put("stdID",stdID);
                                            user.put("email",email);
                                            user.put("password",password);
                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG,"On Success : User Profile is created for" + userID);
                                                }
                                            });
                                        }
                                        else if (domain.equals("gmail.com")){
                                            DocumentReference documentReference = fstore.collection("teachers").document(userID);
                                            Map<String,Object> user = new HashMap<>();
                                            user.put("name",name);
                                            user.put("surname",surname);
                                            user.put("email",email);
                                            user.put("password",password);
                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG,"On Success : User Profile is created for" + userID);
                                                }
                                            });
                                        }

                                        startActivity(new Intent(getApplicationContext(),Login.class));
                                    }else{
                                        Toast.makeText(Register.this,"Error ! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(Register.this,"Error ! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }
}