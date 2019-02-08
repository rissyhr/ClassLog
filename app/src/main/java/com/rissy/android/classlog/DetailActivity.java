package com.rissy.android.classlog;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

import static com.rissy.android.classlog.MainActivity.REQUEST_CODE_CAMERA;
import static com.rissy.android.classlog.MainActivity.REQUEST_PERMISSION;
import static com.rissy.android.classlog.MainActivity.makeTimestamp;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener{

    public Realm realm;
    private ArrayList<ImageData> images;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;

    private Intent intent;
    private Toolbar toolbar;
    private RealmList<ImageData> album;
    private ImageDataList data;
    private ImageData image;
    private FloatingActionButton fab;
    private View fabView;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // realmを開く
        realm = Realm.getDefaultInstance();

        /* 画面部品の諸設定 */
        recyclerView = findViewById(R.id.recycler_view);
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getApplicationContext(), images);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        // 画像をクリックしたらスライドショーを開始する
        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        intent = getIntent();

        /* この科目のアルバムを取得 */
        data = realm.where(ImageDataList.class)
                .equalTo("timestamp", intent.getStringExtra("list_id"))
                .findFirst(); // 科目名に対応したアルバムを指定


        /* ツールバーに科目名を表示 */
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(data.getName());


        /* 講義情報の変更 */
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.option);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.m1) { // 画像をインポート
                    Toast.makeText(DetailActivity.this, "この機能は現在利用できません", Toast.LENGTH_SHORT).show();
                    return true;
                } else if(id == R.id.m2){ // 講義情報を変更する
                    Intent to_resetting = new Intent(getApplicationContext(), ResettingActivity.class);
                    to_resetting.putExtra("list_id", intent.getStringExtra("list_id"));
                    to_resetting.putExtra("from", "DetailActivity");
                    to_resetting.putExtra("何曜何限", intent.getStringExtra("何曜何限"));
                    startActivity(to_resetting); // 科目登録画面へ
                    finish();
                    return true;
                }
                return false;
            }
        });



        // 見るだけの時と、撮影後のときで処理を分ける
        intent = getIntent();
        String from = intent.getStringExtra("from");
        if(from.equals("onLongClick")){
            saveNewImage();
        }
        fetchImages();

    }


    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.fab:

                    fabView = v;

                    // WRITE_EXTERNAL_IMAGE が未許可の場合
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION); //WRITE_EXTERNAL_STORAGEの許可を求めるダイアログを表示

                        return;
                    }

                    final String nowStr = makeTimestamp(); // カメラ起動時の日時取得
                    String fileName = data.getName() + nowStr + ".jpg"; // 保存する画像のファイル名を生成

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName); // 画像ファイル名を設定
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // 画像ファイルの種類を設定

                    ContentResolver resolver = getContentResolver();
                    final Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // URI作成

                    // カメラ起動　ー＞　撮影　ー＞　保存（ ー＞連続撮影 ）　ー＞　科目別アルバム(DetailActivity)へ

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            /* イメージの作成 (撮影前に、すべての情報を保存)   -> キャンセルされたときの処理も追加　or DetailActivityでこの処理を行う    */
                            image = realm.createObject(ImageData.class, nowStr); // subject, timestamp, uriが最低限登録できればOK

                            image.setSubject(data.getTimestamp());
                            //image.setTimestamp(nowStr);
                            image.setUri(uri.toString());

                            Log.d("subject", image.getSubject());

                            image.setName(data.getName() + "_" + nowStr);
                            realm.copyToRealm(image);
                        }
                    });

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 再度カメラアプリを起動
            onClick(fabView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                //Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                //imageView.setImageBitmap(bmp);

                Toast.makeText(DetailActivity.this, "Saved.", Toast.LENGTH_SHORT).show();

                Intent to_detail = new Intent(this, DetailActivity.class);
                to_detail.putExtra("list_id", image.getSubject());
                to_detail.putExtra("image_id", image.getTimestamp());
                /*to_detail.putExtra("subject", image.getSubject());
                to_detail.putExtra("timestamp", image.getTimestamp());*/
                to_detail.putExtra("from", "onLongClick"); // ボタンの長押し->撮影->ギャラリー
                startActivity(to_detail); // 科目別アルバム(DetailActivity)へ飛ぶ
                finish();
            }
        } else if (resultCode == RESULT_CANCELED) {
            // キャンセルされたらToastを表示
            Toast.makeText(DetailActivity.this, "NOT Saved.", Toast.LENGTH_SHORT).show();
        }
    }

    // 撮影した写真を、その科目の画像一覧に追加
    private void saveNewImage() {
        realm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm realm){
                Intent intent = getIntent();

                /* 撮影した画像データを取得 */
                ImageData new_image = realm.where(ImageData.class)
                        .equalTo("timestamp", intent.getStringExtra("image_id"))
                        .findFirst();

                /* アルバムに写真を追加 */
                album = data.getAlbum();
                album.add(new_image);
                data.setAlbum(album);

                realm.copyToRealm(data);
            }
        });
    }

    // 表示する画像一覧を取得
    private void fetchImages(){

        images.clear();

        album = data.getAlbum();

        if(album.isEmpty()){
            Toast.makeText(DetailActivity.this, "写真はまだ登録されていません", Toast.LENGTH_SHORT).show();
        }

        for(ImageData image : album){
            images.add(image); // albumの中身を全てコピー
        }
        Log.d("images.size", String.valueOf(images.size()));
        mAdapter.notifyDataSetChanged(); // 画面に変更を反映
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteDirectoryTree(getApplicationContext().getCacheDir()); // Glideが保存したキャッシュを削除
        realm.close(); // realmを閉じる
    }

    /**
     * Deletes a directory tree recursively.
     */
    public static void deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }
        fileOrDirectory.delete();
    }



}
