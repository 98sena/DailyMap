package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class Diary extends AppCompatActivity {
    ImageView img;
    TextView loc;
    ImageView feel;
    TextView date;
    TextView content;

    int []feelThumbs ={R.drawable.good,R.drawable.mid,R.drawable.bad};
    //FS
    private FirebaseFirestore db;
    //ST
    private FirebaseStorage storage;
    private StorageReference storageRef;
    //DiaryGroup 정보 유지
    String curDG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        //DiaryGroup 정보 유지
        curDG=getIntent().getStringExtra("curDG");

        //CST
        storage= FirebaseStorage.getInstance("gs://daily-map-d47b1.appspot.com");
        storageRef = storage.getReference();

        // view
        img=(ImageView) findViewById(R.id.diaryImg);
        loc=(TextView)findViewById(R.id.diaryLoc);
        feel=(ImageView)findViewById(R.id.diaryFeel);
        date=(TextView)findViewById(R.id.diaryDate);
        content=(TextView)findViewById(R.id.diaryContent);

        //Intent
        Intent intent = getIntent();
        String locX = intent.getExtras().getString("locationX");
        String locY = intent.getExtras().getString("locationY");
        String feels = intent.getExtras().getString("feel");
        //String location = intent.getStringExtra("mLocation");
        loc.setText(locX+", "+locY);
        switch (feels){
            case "0":
                feel.setImageResource(feelThumbs[0]); break;
            case "1":
                feel.setImageResource(feelThumbs[1]); break;
            case "2":
                feel.setImageResource(feelThumbs[2]); break;
        }
        String tmpDate =intent.getExtras().getString("date"); //date 나중에
        date.setText(tmpDate.substring(0,4)+"/"+tmpDate.substring(4,6)+"/"+tmpDate.substring(6,8));
        content.setText(intent.getExtras().getString("content"));

        db = FirebaseFirestore.getInstance(); //Init Firestore
        //이미지 올리기
        storageRef.child(curDG+"/"+intent.getExtras().getString("img"))
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            ImageView tmpIV=img;
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Diary.this)
                        .load(uri)
                        .into(tmpIV);
                Toast.makeText(Diary.this,"iv updated @@@@",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
                Toast.makeText(Diary.this,"iv failed !@@@",Toast.LENGTH_LONG).show();
            }
        });
        Toast.makeText(Diary.this,"현재 : "+curDG,Toast.LENGTH_LONG).show();
    }
}