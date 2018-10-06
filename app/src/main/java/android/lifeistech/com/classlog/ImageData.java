package android.lifeistech.com.classlog;

import io.realm.RealmObject;

public class ImageData extends RealmObject {

    /* "アルバム"に保存される”写真” */

    private String subject; // // 自分の所属する科目名(=ImageDataList.timestamp)
    private String timestamp; // 撮影日時

    private String uri; // 画像の保存場所
    private String name; // サムネタイトル

    public String getSubject(){
        return subject;
    }
    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getUri(){
        return uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }









}
