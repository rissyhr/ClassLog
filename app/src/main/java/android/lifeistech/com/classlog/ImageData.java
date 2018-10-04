package android.lifeistech.com.classlog;

import io.realm.RealmObject;

public class ImageData extends RealmObject {

    private String uri; // 保存場所
    private String timestamp; // 撮影日時
    private String subject; // 科目(ジャンル)
    private String name; // サムネタイトル

    // コンストラクタを設置するとエラー？

    public String getUri(){
        return uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public String getSubject(){
        return subject;
    }
    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }







}
