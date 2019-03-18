package com.rissy.android.classlog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import io.realm.Realm;

public class ResettingActivity extends AppCompatActivity implements View.OnClickListener {

    public Realm realm;

    private Intent intent;
    private EditText subjectText;
    private EditText teacherText;
    private EditText roomText;

    private Button button;
    private ImageDataList renewAlbum;
    private ImageDataListContainer schedule; // これから登録する科目の所属する時間割

    // 講義名の登録は必須。担当教員名、教室名は任意。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_register);

        subjectText = findViewById(R.id.subject);
        teacherText = findViewById(R.id.teacher);
        roomText = findViewById(R.id.room);
        button = findViewById(R.id.register);
        button.setOnClickListener(this);
        button.setText("上記の変更を保存する");

        intent = getIntent();
        setTitle(intent.getStringExtra("何曜何限"));

        renewAlbum = realm.where(ImageDataList.class)
                .equalTo("timestamp", intent.getStringExtra("list_id"))
                .findFirst();

        subjectText.setText(renewAlbum.getName()); // 変更前の講義情報をセットしておく
        teacherText.setText(renewAlbum.getTeacher());
        roomText.setText(renewAlbum.getRoom());



    }

    @Override
    public void onClick(View v) {


        final String subject = subjectText.getText().toString(); // 入力された科目名の取得

        if(subject.isEmpty()){
            subjectText.setError("講義名を入力してください。");

        } else {
            final String teacher = teacherText.getText().toString();
            final String room = roomText.getText().toString();

            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){
                    renewAlbum.setName(subject);
                    renewAlbum.setTeacher(teacher);
                    renewAlbum.setRoom(room);
                }
            });


            Intent to_gallery = new Intent(this, DetailActivity.class);

            to_gallery.putExtra("list_id", renewAlbum.getListID());
            to_gallery.putExtra("from", "resetting");

            startActivity(to_gallery); // 科目別アルバム(DetailActivity)へ飛ぶ


            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // realmを閉じる
    }


}

