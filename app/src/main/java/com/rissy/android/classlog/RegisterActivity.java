package com.rissy.android.classlog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmList;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public Realm realm;

    private Intent intent;
    private Button button;
    private ImageDataList newAlbum;
    private ImageDataListContainer schedule; // これから登録する科目の所属する時間割

    // 講義名の登録は必須。担当教員名、教室名は任意。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_register);

        button = findViewById(R.id.register);
        button.setOnClickListener(this);

        intent = getIntent();
        schedule = realm.where(ImageDataListContainer.class)
                .equalTo("timestamp", intent.getStringExtra("schedule"))
                .findFirst();

        //String sbjId = getApplicationContext().getResources().getResourceEntryName(intent.getIntExtra("position", 0));
        setTitle(intent.getStringExtra("何曜何限"));
    }

    @Override
    public void onClick(View v) {

        final EditText subjectText = findViewById(R.id.subject);
        final EditText teacherText = findViewById(R.id.teacher);
        final EditText roomText = findViewById(R.id.room);

        final String subject = subjectText.getText().toString(); // 入力された科目名の取得

        if(subject.isEmpty()){
            subjectText.setError("講義名を入力してください。");

        } else {
            final String teacher = teacherText.getText().toString();
            final String room = roomText.getText().toString();

            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){

                    newAlbum = new ImageDataList(); // 科目の新規作成

                    newAlbum.setSchedule(schedule.getTimestamp()); // 所属する時間割との紐付け
                    newAlbum.setTimestamp(MainActivity.makeTimestamp());
                    newAlbum.setPosition(intent.getIntExtra("position", 0));

                    newAlbum.setName(subject);
                    newAlbum.setTeacher(teacher);
                    newAlbum.setRoom(room);

                    RealmList<ImageData> firstAlbum = new RealmList<>(); // 空のアルバムを持たせる
                    newAlbum.setAlbum(firstAlbum);
                    realm.copyToRealm(newAlbum);

                    RealmList<ImageDataList> lists = schedule.getClassSchedule();
                    lists.add(newAlbum);
                    realm.copyToRealmOrUpdate(lists);
                }
            });

            /* タップされたx曜日y限がmScheduleに登録済みなら、DetailActivityへ移動 */
            Intent to_gallery = new Intent(this, DetailActivity.class);

            to_gallery.putExtra("list_id", newAlbum.getTimestamp());
/*            to_gallery.putExtra("timestamp", newAlbum.getTimestamp());
            to_gallery.putExtra("schedule", schedule.getTimestamp());
            to_gallery.putExtra("position", newAlbum.getPosition());*/
            to_gallery.putExtra("from", "RegisterActivity");
            to_gallery.putExtra("何曜何限", intent.getStringExtra("何曜何限"));
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