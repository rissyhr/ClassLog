package com.rissy.android.classlog;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    static final int REQUEST_PERMISSION = 0;
    static final int REQUEST_CODE_CAMERA = 1;
    static final int REQUEST_CODE_PHOTO = 2;

    public Realm realm;

    private boolean firstTime; // 初回起動時の処理

    private View clicked_button;
    private Button[][] buttons; // 科目別ボタン
    private TextView[] days, times; // 時間割の目盛
    private LinearLayout[] lines; // 時間割の行

    private int[][] ids; // ボタンのレイアウトidを格納
    private int[] d_ids, t_ids; // 目盛のレイアウトidを格納

    private Uri uri; // 撮影する写真の保存先
    private ImageData image; // 撮影する写真に関する情報

    private ImageDataListContainer mSchedule; // アプリ起動時に表示される時間割(を持つコンテナ) 。　-> アルバム/写真の新規作成時に紐づける

    private Intent intent_camera;   // ボタン長押し->カメラ起動のとき使用。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        setButtons(); // 科目別ボタンに2種のリスナーをセット
        setBars();
        setLines();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setClassSchedule(); // 起動時に表示する時間割を指定
        editClassScheduleView(); // Realmに保存された情報を時間割へ反映
        editSubjectView(); // Realmの中の科目情報をViewに反映
    }

    /* 未実装 */
    // 右上の設定タブを押したら新たにImageDataListContainerのインスタンスが作成され、
    // いままで表示されていたmScheduleの isSelectedをfalseにし、
    // 新しいやつをtrueにさせて画面に反映させる

    public void setButtons() {
        ids = new int[][]{{R.id.sbj00, R.id.sbj01, R.id.sbj02, R.id.sbj03, R.id.sbj04, R.id.sbj05, R.id.sbj06},
                {R.id.sbj10, R.id.sbj11, R.id.sbj12, R.id.sbj13, R.id.sbj14, R.id.sbj15, R.id.sbj16},
                {R.id.sbj20, R.id.sbj21, R.id.sbj22, R.id.sbj23, R.id.sbj24, R.id.sbj25, R.id.sbj26},
                {R.id.sbj30, R.id.sbj31, R.id.sbj32, R.id.sbj33, R.id.sbj34, R.id.sbj35, R.id.sbj36},
                {R.id.sbj40, R.id.sbj41, R.id.sbj42, R.id.sbj43, R.id.sbj44, R.id.sbj45, R.id.sbj46},
                {R.id.sbj50, R.id.sbj51, R.id.sbj52, R.id.sbj53, R.id.sbj54, R.id.sbj55, R.id.sbj56},
                {R.id.sbj60, R.id.sbj61, R.id.sbj62, R.id.sbj63, R.id.sbj64, R.id.sbj65, R.id.sbj66},
                {R.id.sbj70, R.id.sbj71, R.id.sbj72, R.id.sbj73, R.id.sbj74, R.id.sbj75, R.id.sbj76},
                {R.id.sbj80, R.id.sbj81, R.id.sbj82, R.id.sbj83, R.id.sbj84, R.id.sbj85, R.id.sbj86},
                {R.id.sbj90, R.id.sbj91, R.id.sbj92, R.id.sbj93, R.id.sbj94, R.id.sbj95, R.id.sbj96},
        };
        // データ型[][] 配列名 = new データ型名[Ｙ方向の長さ][Ｘ方向の長さ];
        buttons = new Button[10][7]; // 月〜日、1限〜10限
        for (int i = 0; i < ids.length; i++) {
            for (int j = 0; j < ids[0].length; j++) {
                buttons[i][j] = findViewById(ids[i][j]);
                buttons[i][j].setOnClickListener(this); // タップを探知
                buttons[i][j].setOnLongClickListener(this); // 長押しを探知
            }
        }
    }

    public void setBars() {
        d_ids = new int[]{R.id.Mon, R.id.Tue, R.id.Wed, R.id.Thu, R.id.Fri, R.id.Sat, R.id.Sun};
        t_ids = new int[]{R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5, R.id.t6, R.id.t7, R.id.t8, R.id.t9, R.id.t10};

        days = new TextView[7];
        times = new TextView[10];
        for (int i = 0; i < d_ids.length; i++) {
            days[i] = findViewById(d_ids[i]);
        }
        for (int i = 0; i < t_ids.length; i++) {
            times[i] = findViewById(t_ids[i]);
        }
    }

    public void setLines() {
        int[] l_ids = new int[]{R.id.L1, R.id.L2, R.id.L3, R.id.L4, R.id.L5, R.id.L6, R.id.L7, R.id.L8, R.id.L9, R.id.L10};
        lines = new LinearLayout[10];
        for (int i = 0; i < l_ids.length; i++) {
            lines[i] = findViewById(l_ids[i]);
        }
    }

    public void setClassSchedule() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                mSchedule = realm.where(ImageDataListContainer.class).equalTo("isSelected",
                        true).findFirst(); // アプリ起動時に表示する時間割を取得

                if (mSchedule == null) { // アプリ初回起動時
                    // 以下の処理を今後メソッド化して、時間割追加作成時にも使用する

                    mSchedule = new ImageDataListContainer();   // 時間割新規作成
                    mSchedule.setTimestamp(makeTimestamp()); // 現時刻を保存。時間割識別に用いる
                    RealmList<ImageDataList> newSchedule = new RealmList<>();
                    mSchedule.setClassSchedule(newSchedule); // 講義情報未登録のリスト

                    RealmList<ImageDataList> lists = mSchedule.getClassSchedule();

/*                    *//* 科目表示を動的に変更できるかチェック *//*
                    ImageDataList sampleAlbum = new ImageDataList();      // schedule, position, nameが最低限登録できればOK

                    sampleAlbum.setSchedule(mSchedule.getTimestamp());
                    sampleAlbum.setTimestamp(makeTimestamp());
                    sampleAlbum.setPosition(R.id.sbj11);
                    sampleAlbum.setName("sample1");


                    RealmList<ImageData> firstAlbum = new RealmList<>();
                    sampleAlbum.setAlbum(firstAlbum);

                    realm.copyToRealm(sampleAlbum);

                    lists.add(sampleAlbum);*/



                    mSchedule.setClassSchedule(lists);


                    mSchedule.setSelected(true); // これを

                    // 月〜金、1限〜5限 を持つ、という情報を保存
                    mSchedule.setHasSaturday(false);
                    mSchedule.setHasSunday(false);
                    mSchedule.setHasT1(true);
                    mSchedule.setHasT2(true);
                    mSchedule.setHasT3(true);
                    mSchedule.setHasT4(true);
                    mSchedule.setHasT5(true);
                    mSchedule.setHasT6(true);
                    mSchedule.setHasT7(false);
                    mSchedule.setHasT8(false);
                    mSchedule.setHasT9(false);
                    mSchedule.setHasT10(false);
                }
                realm.copyToRealm(mSchedule);
                // これから反映させる時間割の情報をRealmに保存
            }
        });
    }

    public void editClassScheduleView() { // 月~金 or 月~土 or 月~日, ~5or6or7限 のみ実装済み
        //　デフォルト(月〜金、1~5限)と異なる場合は、ボタンのvisibilityを変更する
        //　ただし、6限がなくて7限がある、という状況は避ける。

        if (mSchedule.hasSaturday()) { // 1~5限まで土曜日を出現
            days[5].setVisibility(View.VISIBLE);
            for (int i = 0; i < 5; i++) {
                buttons[i][5].setVisibility(View.VISIBLE);
            }
            if (mSchedule.hasSaturday()) { // 1~5限まで日曜日を出現
                days[6].setVisibility(View.VISIBLE);
                for (int i = 0; i < 5; i++) {
                    buttons[i][6].setVisibility(View.VISIBLE);
                }
            }
        }
        if (mSchedule.hasT6()) { // 6限目を出現
            lines[5].setVisibility(View.VISIBLE);
            times[5].setVisibility(View.VISIBLE);
            for (int i = 0; i < 5; i++) {
                buttons[5][i].setVisibility(View.VISIBLE);
            }
            if (mSchedule.hasSaturday()) {
                buttons[5][5].setVisibility((View.VISIBLE)); // 土曜6限を出現
                if (mSchedule.hasSaturday()) {
                    buttons[5][6].setVisibility(View.VISIBLE); // 土曜7限を出現
                }
            }
        }
        if (mSchedule.hasT7()) {
            lines[6].setVisibility(View.VISIBLE);
            times[6].setVisibility(View.VISIBLE);
            for (int i = 0; i < 5; i++) {
                buttons[6][i].setVisibility(View.VISIBLE);
            }
            if (mSchedule.hasSaturday()) {
                buttons[6][5].setVisibility((View.VISIBLE)); // 日曜6限を出現
                if (mSchedule.hasSaturday()) {
                    buttons[6][6].setVisibility(View.VISIBLE); // 日曜7限を出現
                }
            }
        }
    }

    public void editSubjectView() { // 保存された各科目情報を時間割に反映

        RealmList<ImageDataList> lists = mSchedule.getClassSchedule(); // 科目情報リストを取得

        if (!lists.isEmpty()) { // 科目情報が少なくとも1つ登録されているとき
            for (ImageDataList album : lists) {
                int n = album.getPosition();
                Button button = null;

                loop:
                for (int i = 0; i < ids.length; i++) {
                    for (int j = 0; j < ids[0].length; j++) {

                        if (n == ids[i][j]) {
                            button = buttons[i][j];
                            break loop; // 目当ての科目を見つけたら二重for文を抜ける
                        }
                    }
                }
                button.setText(album.getName());
                button.setTextColor(Color.rgb(255, 87, 34));
                //button.setBackgroundColor(Color.rgb(117, 117, 117));
                // 色の変更もここに書く
            }
        }
    }


    // タイムスタンプ作成
    public static final String makeTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); // 日時データを元に文字列を形成
        final String nowStr = dateFormat.format(new Date(System.currentTimeMillis()));

        return nowStr;
    }


    /* タップ： ギャラリー(アルバム)へ移動*/
    @Override
    public void onClick(View view) {

        /* タップされたx曜日y限がmScheduleに未登録なら、RegisterActivityへ移動 */

        String schedule = mSchedule.getTimestamp();
        int position = view.getId();

        ImageDataList data = realm.where(ImageDataList.class)
                .equalTo("schedule", schedule) // いま開いている時間割のなかに
                .equalTo("position", position) // いまタップした科目が登録されているか
                .findFirst();

        if (data == null) {
            Intent to_register = new Intent(this, RegisterActivity.class);
            to_register.putExtra("schedule", schedule);
            to_register.putExtra("position", position);
            to_register.putExtra("何曜何限", (String)view.getTag());
            startActivity(to_register); // 科目登録画面へ
            return;
        }

        /* タップされたx曜日y限がmScheduleに登録済みなら、DetailActivityへ移動 */
        Intent to_gallery = new Intent(this, DetailActivity.class);

        to_gallery.putExtra("list_id", data.getListID());


/*        to_gallery.putExtra("timestamp", data.getTimestamp()); // timestampでImageDataListを特定
        to_gallery.putExtra("schedule", schedule);
        to_gallery.putExtra("position", position);*/
        to_gallery.putExtra("from", "onClick");
        to_gallery.putExtra("何曜何限", (String)view.getTag());
        startActivity(to_gallery); // 科目別アルバム(DetailActivity)へ飛ぶ
    }


    /* 長押し：カメラの起動 */
    @Override
    public  boolean onLongClick(final View view) {

        /* タップされたx曜日y限がmScheduleに未登録なら、RegisterActivityへ移動   (onClickのはじめと同じ処理)*/
        String schedule = mSchedule.getTimestamp();
        int position = view.getId();

        final ImageDataList data = realm.where(ImageDataList.class)
                .equalTo("schedule", schedule) // いま開いている時間割のなかに
                .equalTo("position", position) // いまタップした科目が登録されているか
                .findFirst();

        if (data == null) {
            Intent to_register = new Intent(this, RegisterActivity.class);
            to_register.putExtra("schedule", schedule);
            to_register.putExtra("position", position);
            to_register.putExtra("何曜何限", (String)view.getTag());
            startActivity(to_register); // 科目登録画面へ
            return true;
        }

        // WRITE_EXTERNAL_IMAGE が未許可の場合
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION); //WRITE_EXTERNAL_STORAGEの許可を求めるダイアログを表示

            clicked_button = view; // Clickされた科目に対応するviewをクラスフィールドに保存

            return true;  // 戻り値をtrueにするとOnClickイベントは発生しない(falseだと最後にonClickイベント発生)
        }


        final String nowStr = makeTimestamp(); // カメラ起動時の日時取得
        String fileName = data.getName() + nowStr + ".jpg"; // 保存する画像のファイル名を生成

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName); // 画像ファイル名を設定
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // 画像ファイルの種類を設定

        ContentResolver resolver = getContentResolver();
        uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // URI作成

        // カメラ起動　ー＞　撮影　ー＞　保存（ ー＞連続撮影 ）　ー＞　科目別アルバム(DetailActivity)へ

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                /* イメージの作成 (撮影前に、すべての情報を保存)   -> キャンセルされたときの処理も追加　or DetailActivityでこの処理を行う    */

                image = realm.createObject(ImageData.class, nowStr); // subject, timestamp, uriが最低限登録できればOK

                image.setSubject(data.getListID());
                //image.setTimestamp(nowStr);
                image.setUri(uri.toString());

                Log.d("subject", image.getSubject());

                image.setName(((Button) view).getText() + "_" + nowStr);
                realm.copyToRealm(image);
            }
        });

        intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent_camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent_camera.putExtra("何曜何限", (String)view.getTag());
        startActivityForResult(intent_camera, REQUEST_CODE_CAMERA);

        // カメラの起動はonAcrivityMethodで行う

        return true;    // 戻り値をtrueにするとOnClickイベントは発生しない(falseだと最後にonClickイベント発生)
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 再度カメラアプリを起動
            onLongClick(clicked_button);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                //Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                //imageView.setImageBitmap(bmp);

                Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();

                Intent to_detail = new Intent(this, DetailActivity.class);
                to_detail.putExtra("list_id", image.getSubject());
                to_detail.putExtra("image_id", image.getTimestamp());
                /*to_detail.putExtra("subject", image.getSubject());
                to_detail.putExtra("timestamp", image.getTimestamp());*/
                to_detail.putExtra("from", "onLongClick"); // ボタンの長押し->撮影->ギャラリー
                to_detail.putExtra("何曜何限", intent_camera.getStringExtra("何曜何限"));
                startActivity(to_detail); // 科目別アルバム(DetailActivity)へ飛ぶ
            }
        } else if (resultCode == RESULT_CANCELED) {
            // キャンセルされたらToastを表示
            Toast.makeText(MainActivity.this, "NOT Saved.", Toast.LENGTH_SHORT).show();
        }
    }


/*    *//* メニューに関する設定 *//*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.m1: //画像をインポート


                return true;

            case R.id.m2:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //realmを閉じる
        realm.close();
    }
}

