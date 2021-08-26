package com.example.android.uploadnotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.android.uploadnotes.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
/// binding object...
    ActivityMainBinding b;
    StorageReference reference;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        instantiatingFirebase();

    }

    private void instantiatingFirebase() {
        reference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("PDF");
    }

    public void goToPhoneStorage(View view) {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select from"),12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null){
            b.editPdf.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/")+1));

            b.uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputExam = b.editExam.getText().toString().trim();
                    String inputType = b.editType.getText().toString().trim();
                    String inputPdf = b.editPdf.getText().toString().trim();

                    if(inputExam.isEmpty()){
                        b.editExam.setError("Please Enter Exam");
                        return;
                    }
                    if(inputType.isEmpty()){
                        b.editType.setError("Please Enter Type");
                        return;
                    }
                    if(inputPdf.isEmpty()){
                        b.editPdf.setError("Pdf Missing");
                        return;
                    }

                    uploadPDF(data.getData());
                }
            });
        }
    }

    private void uploadPDF(Uri data) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is Uploading....");
        progressDialog.show();

        StorageReference storageReference = reference.child("upload"+System.currentTimeMillis()+".pdf");
        storageReference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isComplete());
                        Uri uri = uriTask.getResult();

                        PDFDescription  pdfDescription = new PDFDescription(b.editExam.getText().toString().trim(),
                                b.editType.getText().toString().trim(),b.editPdf.getText().toString().trim(),uri.toString());

                        databaseReference.child(databaseReference.push().getKey()).setValue(pdfDescription);
                        Toast.makeText(MainActivity.this, "File is uploaded ", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File Uploaded "+(int)progress+"%");
            }
        });
    }

}