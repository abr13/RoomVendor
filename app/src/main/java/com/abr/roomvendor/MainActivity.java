package com.abr.roomvendor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int PICK_IMAGE_REQUEST = 1;
    ImageView imageView;
    Button uploadDetailsButton, browseButton, uploadImgButton;
    EditText cityText, rentText;
    RadioGroup rg;
    long maxid = 0;
    String roomType = "Girls";
    String ImgURL;
    private Uri filePath;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        uploadDetailsButton = findViewById(R.id.uploadDetailsButton);
        browseButton = findViewById(R.id.browseButton);
        uploadImgButton = findViewById(R.id.uploadImgButton);
        cityText = findViewById(R.id.cityText);
        rentText = findViewById(R.id.rentText);
        rg = findViewById(R.id.rg);


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.girls:
                        roomType = "Girls";
                        break;
                    case R.id.boys:
                        roomType = "Boys";
                        break;
                    case R.id.individual:
                        roomType = "Individuals";
                        break;
                    case R.id.family:
                        roomType = "Family";
                        break;
                }
            }
        });


        uploadImgButton.setOnClickListener(this);
        uploadDetailsButton.setOnClickListener(this);
        browseButton.setOnClickListener(this);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a room image"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Upaloading Image");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        String path = filePath.getLastPathSegment();
        final StorageReference riversRef = mStorageRef.child("Rooms/" + path);

        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Get a URL to the uploaded content
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                ImgURL = downloadUrl.toString();
                                String TAG = null;
                                Log.d(TAG, "onSuccess: " + ImgURL);

                                Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                uploadDetailsButton.setEnabled(true);
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage(((int) progress) + "% Uploaded...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                uploadImgButton.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == uploadDetailsButton) {
            if (!cityText.getText().toString().equals("") && !rentText.getText().toString().equals("")) {
                switch (roomType) {
                    case "Girls":
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(roomType);
                        break;
                    case "Boys":
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(roomType);
                        break;
                    case "Individuals":
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(roomType);
                        break;
                    case "Family":
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(roomType);
                        break;
                }

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            maxid = (dataSnapshot.getChildrenCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String City = cityText.getText().toString().trim();
                String Rent = rentText.getText().toString().trim();
                Data data = new Data(City, ImgURL, Rent);

                databaseReference.child(String.valueOf(maxid + 1)).setValue(data);
                Toast.makeText(this, "Details Uploaded", Toast.LENGTH_SHORT).show();
                cityText.setText("");
                rentText.setText("");
                imageView.setImageResource(0);
                uploadImgButton.setEnabled(false);
                uploadDetailsButton.setEnabled(false);
            } else {
                Toast.makeText(this, "Please fill the details!", Toast.LENGTH_SHORT).show();
            }

        } else if (v == browseButton) {
            showFileChooser();
        } else if (v == uploadImgButton) {
            uploadImage();
        }

    }
}
