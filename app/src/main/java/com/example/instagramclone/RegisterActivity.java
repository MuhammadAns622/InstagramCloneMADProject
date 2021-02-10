package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity<Final, email> extends AppCompatActivity {

    EditText etUsername ,etFullName,etPassword,etEmail;
    Button btnRegister;
    TextView tvLogin;
    FirebaseAuth myAuth;
    DatabaseReference reference;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUsername=findViewById(R.id.etUsername);
        etFullName=findViewById(R.id.etFullName);
        etPassword=findViewById(R.id.etPassword);
        etEmail=findViewById(R.id.etEmail);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        myAuth = FirebaseAuth.getInstance();
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please wait...");
                pd.show();
                String Username=etUsername.getText().toString().trim();
                String fullName=etFullName.getText().toString().trim();
                String email=etEmail.getText().toString().trim();
                String password=etPassword.getText().toString().trim();
                if(Username.isEmpty()||password.isEmpty()||fullName.isEmpty()||email.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this, "You Need to fill all fields", Toast.LENGTH_SHORT).show();
                }
                else if(password.length()<5)
                {
                    Toast.makeText(RegisterActivity.this, "Password can not be less than 5 Characters", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Register(email,password,fullName,Username);
                }

            }
        });





    }

    private void Register( final String email, final String password, final String  fullName, final String username) {

        myAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser firebaseUser= myAuth.getCurrentUser();
                            String userId= myAuth.getUid();
                            reference= FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                            HashMap<String, Object>data=new HashMap<>();
                            data.put("id",userId);
                            data.put("username",username);
                            data.put("fullname",fullName);
                            data.put("bio"," ");
                            data.put("imageurl","https://firebasestorage.googleapis.com/v0/b/instagramclone-90aca.appspot.com/o/placeholder.png?alt=media&token=b930b1d5-9c53-46c5-8388-7ec959bcb99c");

                            reference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        pd.dismiss();
                                        Intent intent=new Intent(RegisterActivity.this,StartActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                        else
                        {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this,"You can not register with this email and password",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}