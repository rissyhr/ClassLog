package android.lifeistech.com.classlog;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ImageDataList extends RealmObject {

    /* 各科目の"アルバム" */

    private String subject;
    private RealmList<ImageData> album;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public RealmList<ImageData> getAlbum(){
        return album;
    }

    public void setAlbum(RealmList<ImageData> album){
        this.album = album;
    }
}
