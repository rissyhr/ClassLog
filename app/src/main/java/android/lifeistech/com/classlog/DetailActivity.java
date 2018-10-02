package android.lifeistech.com.classlog;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.realm.Realm;

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

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

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

    // 一枚の画像を取得
    private void fetchImages() {
        images.clear();

        // 撮影した写真のURIを取得
        Intent intent = getIntent();
        final String myUri = intent.getStringExtra("imageUri");

        // 撮影した写真を保存するRealmObjectの作成 & 保存
        realm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm bgRealm){

                // この時点では、MainActivityで扱ったRealmオブジェクトと不一致！　URIで連結させる必要あり。


                ImageData image = realm.where(ImageData.class).equalTo("medium",
                        getIntent().getStringExtra("imageUri")).findFirst();
                //memo.updateDate = updateDate;
                //image.setSmall(url.getString("small"));

                //image.setTimestamp(object.getString("timestamp"));
                images.add(image);
                // adapterの中でうまくURIをURLにかえる　new URL(uri.toString())


            }
        });

/*                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = response.getJSONObject(i);
                                Image image = new Image();
                                image.setName(object.getString("name"));

                                JSONObject url = object.getJSONObject("url");
                                image.setSmall(url.getString("small"));
                                image.setMedium(url.getString("medium"));
                                image.setLarge(url.getString("large"));
                                image.setTimestamp(object.getString("timestamp"));

                                images.add(image);

                            } catch (JSONException e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                            }
                        }*/
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //realmを閉じる
        realm.close();
    }
}


