package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    Uri imageUrl;
    String myUrl = "";

    StorageTask uploadTask ;
    StorageReference storageReference;

    ImageView ivClose, ivImageAdded;

    TextView post ;
    EditText description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ivClose = findViewById(R.id.ivClose);
        ivImageAdded = findViewById(R.id.ivImageAdded);
        post = findViewById(R.id.ivPost);
        description = findViewById(R.id.description);
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(PostActivity.this);

    }
    private String getFileExtensions(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();
        if(imageUrl != null) {
            final StorageReference fileReferance = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtensions(imageUrl));
            uploadTask = fileReferance.putFile(imageUrl);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReferance.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();

                        myUrl = Objects.requireNonNull(downloadUrl).toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();
                        HashMap<String, Object> postData = new HashMap<String, Object>();
                        postData.put("postid", postid);
                        postData.put("postimage", myUrl);
                        postData.put("description", description.getText().toString());
                        postData.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                        reference.child(Objects.requireNonNull(postid)).setValue(postData);

                        progressDialog.dismiss();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {

                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {

            Toast.makeText(this, "no Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUrl = Objects.requireNonNull(result).getUri();

            ivImageAdded.setImageURI(imageUrl);

        }
        else
        {
            Toast.makeText(PostActivity.this,"Searching gone wrong!",Toast.LENGTH_SHORT).show();

            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }
}