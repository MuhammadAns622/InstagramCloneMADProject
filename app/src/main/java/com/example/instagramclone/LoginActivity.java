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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText et_password,et_email;
    Button btn_login;
    TextView tvSignUp;
    FirebaseAuth myLoginAuth;
    DatabaseReference reference;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_email=findViewById(R.id.et_email);
        et_password=findViewById(R.id.et_password);
        tvSignUp=findViewById(R.id.tvSignUp);
        btn_login=(Button)findViewById(R.id.btnLogin);
        myLoginAuth = FirebaseAuth.getInstance();


        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(LoginActivity.this);
                pd.setMessage("Please wait...");
                pd.show();

                String str_email= et_email.getText().toString().trim();
                String str_password= et_password.getText().toString().trim();

                if(str_email.isEmpty()||str_password.isEmpty())
                {
                    Toast.makeText(LoginActivity.this, "Email or Password can not be empty", Toast.LENGTH_SHORT).show();
                }
                else if(str_password.length()<6) {
                    Toast.makeText(LoginActivity.this,"Password must have 6 characters",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    myLoginAuth.signInWithEmailAndPassword(str_email,str_password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(myLoginAuth.getCurrentUser().getUid());
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        pd.dismiss();

                                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);

                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                        pd.dismiss();
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this,"Authentication Failed!",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }
}