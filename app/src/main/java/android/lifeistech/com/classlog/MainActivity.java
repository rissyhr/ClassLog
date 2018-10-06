package android.lifeistech.com.classlog;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Realm realm;

    static final int REQUEST_PERMISSION = 0;
    static final int REQUEST_CODE_CAMERA = 1;

    Button button; // サンプルボタン「数理統計学」   配列でどうにかする
    Button buttons[][];
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        setButtons(); //
        buttons[0][0].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "長押し", Toast.LENGTH_SHORT).show();
                return true;    // 戻り値をtrueにするとOnClickイベントは発生しない(falseだと最後にonClickイベント発生)
            }
        });
    }

    public void setButtons() {
        int[][] ids = new int[][]{{R.id.sbj00, R.id.sbj01, R.id.sbj02, R.id.sbj03, R.id.sbj04, R.id.sbj05},
                {R.id.sbj10, R.id.sbj11, R.id.sbj12, R.id.sbj13, R.id.sbj14, R.id.sbj15},
                {R.id.sbj20, R.id.sbj21, R.id.sbj22, R.id.sbj23, R.id.sbj24, R.id.sbj25},
                {R.id.sbj30, R.id.sbj31, R.id.sbj32, R.id.sbj33, R.id.sbj34, R.id.sbj35},
                {R.id.sbj40, R.id.sbj41, R.id.sbj42, R.id.sbj43, R.id.sbj44, R.id.sbj45},
                {R.id.sbj50, R.id.sbj51, R.id.sbj52, R.id.sbj53, R.id.sbj54, R.id.sbj55},
                {R.id.sbj60, R.id.sbj61, R.id.sbj62, R.id.sbj63, R.id.sbj64, R.id.sbj65}};

        // データ型[][] 配列名 = new データ型名[Ｙ方向の長さ][Ｘ方向の長さ];
        buttons = new Button[7][6]; // 月〜土、1限〜7限
        for (int i = 0; i < ids.length; i++) {
            for (int j = 0; j < ids[0].length; j++) {
                buttons[i][j] = findViewById(ids[i][j]);
                buttons[i][j].setOnClickListener(this);
            }
        }
    }

    public void onClick(View v) {
        final View view = v;

        // WRITE_EXTERNAL_IMAGE が未許可の場合
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION); //WRITE_EXTERNAL_STORAGEの許可を求めるダイアログを表示
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); // 日時データを元に文字列を形成
        final String nowStr = dateFormat.format(new Date(System.currentTimeMillis()));
        String fileName = "ClassLog_" + nowStr + ".jpg"; // 保存する画像のファイル名を生成

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName); // 画像ファイル名を設定
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // 画像ファイルの種類を設定

        ContentResolver resolver = getContentResolver();
        uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // URI作成

        Log.d("view.getID", String.valueOf(view.getId()));

        //switch (view.getId()) {


        // カメラ起動　ー＞　撮影　ー＞　保存（ ー＞連続撮影 ）　ー＞　科目別アルバム(DetailActivity)へ

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                /* イメージの作成 (撮影前に、すべての情報を保存)   -> キャンセルされたときの処理も追加　or DetailActivityでこの処理を行う    */
                ImageData image = realm.createObject(ImageData.class);
                image.setTimestamp(nowStr);
                image.setUri(uri.toString());
                image.setSubject(view.getId() + "");
                Log.d("subject", image.getSubject());

                image.setName(((Button) view).getText() + "_" + nowStr);
                realm.copyToRealm(image);
            }
        });


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 再度カメラアプリを起動
            onClick(button);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                //Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                //imageView.setImageBitmap(bmp);

                Toast.makeText(MainActivity.this, "画像を保存しました", Toast.LENGTH_LONG).show();

                Intent intent_main = new Intent(this, DetailActivity.class);
                intent_main.putExtra("imageUri", uri.toString());
                // 科目情報もIntentに持たせる
                startActivity(intent_main); // 科目別アルバム(DetailActivity)へ飛ぶ
            }
        } else if (resultCode == RESULT_CANCELED) {
            // キャンセルされたらToastを表示
            Toast.makeText(MainActivity.this, "CANCELED", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //realmを閉じる
        realm.close();
    }
}

