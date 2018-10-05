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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

public class DetailActivity extends AppCompatActivity {

    public Realm realm;
    private ArrayList<ImageData> images;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // realmを開く
        realm = Realm.getDefaultInstance();

        recyclerView = findViewById(R.id.recycler_view);

        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getApplicationContext(), images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
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

        fetchImages();
    }

    // 撮影した科目の画像一覧を取得 (imagesに,imageDataAlbumの中身を入れる)
    private void fetchImages() {
        realm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm realm){
                Intent intent = getIntent();
                ImageData new_image = realm.where(ImageData.class).equalTo("uri",
                        intent.getStringExtra("imageUri")).findFirst(); // 撮影した画像データを取得
                Log.d("new_image.getSubject()", new_image.getSubject());

                ImageDataList data = realm.where(ImageDataList.class).equalTo("subject",
                        new_image.getSubject()).findFirst(); // 科目名に対応したアルバムを指定

                if(data == null){
                    data = new ImageDataList();   // 該当する科目のアルバムが未作成であれば、新規作成
                    data.setSubject(new_image.getSubject());
                    Log.d("new_image.getSubject()", new_image.getSubject());
                    RealmList<ImageData> firstAlbum = new RealmList<>();
                    data.setAlbum(firstAlbum);
                }

                RealmList<ImageData> lists = data.getAlbum();
//                if(lists == null) lists = new ArrayList<>();
                lists.add(new_image);
                data.setAlbum(lists);
                realm.copyToRealm(data);


/*
                List<ImageData> lists = new ArrayList<>();
                lists.add(new_image);*/

                images.clear();
                for(ImageData image : lists){
                    images.add(image); // 表示される画像一覧(images)に、albumの中身を反映
                }
                Log.d("images.size", String.valueOf(images.size()));
            }
        });
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // realmを閉じる

    }
}
