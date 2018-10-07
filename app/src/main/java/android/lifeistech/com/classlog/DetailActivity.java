package android.lifeistech.com.classlog;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

public class DetailActivity extends AppCompatActivity {

    public Realm realm;
    private ArrayList<ImageData> images;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;

    private Intent intent;
    private RealmList<ImageData> album;
    private ImageDataList data;

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

        // 見るだけの時と、撮影後のときで処理を分ける
        intent = getIntent();
        String from = intent.getStringExtra("from");

        /* この科目のアルバムを取得 */
        data = realm.where(ImageDataList.class)
                .equalTo("timestamp", intent.getStringExtra("list_id"))
                .findFirst(); // 科目名に対応したアルバムを指定

        if(from.equals("onLongClick")){
            saveNewImage();
        }
        fetchImages();
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
