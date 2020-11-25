package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddDiary extends AppCompatActivity {
    DatePickerDialog.OnDateSetListener callbackMethod;
    Calendar cal;
    int year, month,day;

    TextView date;
    ImageView imgContent;
    ImageView submit;
    ImageView[] feels = new ImageView[3];
    EditText content;

    TextView place;
    static final int REQ_ADD_CONTACT = 1 ;
    // 선택한 장소 정보 받아오는 변수
    String location; // 주소
    double latitude, longitude; // 위도, 경도

    //Auth
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //CFS
    private FirebaseFirestore db;
    private DiaryDS newD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);
        //CFS
        db = FirebaseFirestore.getInstance(); //Init Firestore
        user = FirebaseAuth.getInstance().getCurrentUser();
        newD = new DiaryDS(user.getEmail());
        newD.setLocation(37.26,125.27);
        newD.setImg("no idea");

        //date 지정
        date = (TextView) findViewById(R.id.editDate);

        cal=Calendar.getInstance();
        year =cal.get(Calendar.YEAR); month = cal.get(Calendar.MONTH)+1; day = cal.get(Calendar.DATE);
        date.setText(year+"/"+month+"/"+day);
        newD.setDate(year,month,day);
        callbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int tyear, int tmonth, int tday) {
                year = tyear; month=tmonth+1; day=tday;
                date.setText(year+"/"+month+"/"+day);
                newD.setDate(year,month,day);
            }
        };

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(AddDiary.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth
                        ,callbackMethod, year, month-1, day);
                dialog.getDatePicker().setCalendarViewShown(false);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        //View 할당
        imgContent = (ImageView)findViewById(R.id.editImg);
        submit = (ImageView)findViewById(R.id.editSubmit);
        feels[0]= (ImageView)findViewById(R.id.good);
        feels[1]= (ImageView)findViewById(R.id.mid);
        feels[2]= (ImageView)findViewById(R.id.bad);
        content =(EditText)findViewById(R.id.editContent);

        //이미지 로딩
        imgContent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 101);
            }
        });

        // place 지정
        place = findViewById(R.id.editPlace);
        place.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"장소 추가!!", Toast.LENGTH_SHORT).show();
                /*
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 101);
                */
                Intent intent = new Intent(AddDiary.this, SearchLocation.class);
                startActivityForResult(intent,REQ_ADD_CONTACT);
            }
        });

        //Feels
        for(int i=0;i<feels.length;i++){
            final int feelsCnt=i;
            feels[i].setOnClickListener(new View.OnClickListener() {
                boolean clicked = false;
                @Override
                public void onClick(View view) {
                    newD.feel = -1; //-1: 기분 안눌림;
                    for(int i=0;i<feels.length;i++){
                        feels[i].setBackgroundColor(Color.WHITE);
                        if(feelsCnt==i){
                            feels[feelsCnt].setBackgroundColor(getResources().getColor(R.color.mainColor));
                            //clicked=true;
                            newD.feel=feelsCnt;
                        }
                    }
                    Toast.makeText(getApplicationContext(),"feels "+newD.feel, Toast.LENGTH_SHORT).show();
                }
            });
            //Submit
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(newD.feel!=-1){
                        //내용 저장
                        newD.setContent(content.getText().toString());
                        addNewContent(user.getUid());
                        Toast.makeText(getApplicationContext(),"Submit OK", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getApplicationContext(),"기분을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void addNewContent(String uid){
        String dgKey = uid+"000"; // 어떤 DiaryGroup에다가 저장
        //날짜별로 저장? 일단 random key 상태..
        newD.display(AddDiary.this);
        db.collection("DiaryGroupList").document(dgKey)
                .collection("diaryList")
                .add(newD)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddDiary.this,"Diary Add SUCC",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),Main.class);
                        startActivity(intent); //추가와 동시에 이동
                    }
                });
    }

    // 장소 추가 액티비티에서 선택한 장소 정보 받아옴
    // location : 주소, latitude: 위도, longitude 경도
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_ADD_CONTACT) {
            if (resultCode == RESULT_OK) {
                location = intent.getStringExtra("location");
                latitude = intent.getDoubleExtra("latitude", 0);
                longitude = intent.getDoubleExtra("longitude", 0);
                LatLng latLng = new LatLng(latitude,longitude);

                place = findViewById(R.id.editPlace);
                place.setText(location);

                System.out.println(latLng);
            }
        }
    }
}