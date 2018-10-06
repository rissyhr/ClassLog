package android.lifeistech.com.classlog;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ImageDataList extends RealmObject {

    /* 各科目の"アルバム" */
    private String schedule; // 自分の所属する時間割名 (=ImageDataListContainer.timestamp)
    private String timestamp; // 新規作成日時

    private String name; //　科目名　(画面表示用)
    // ここに、テーマカラーもかく　教員名も教室もメモも保存？
    private int position; // 何曜何限か (R.id.sbjXX と同じ値)
    private RealmList<ImageData> album;


    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public RealmList<ImageData> getAlbum() {
        return album;
    }

    public void setAlbum(RealmList<ImageData> album) {
        this.album = album;
    }
}
