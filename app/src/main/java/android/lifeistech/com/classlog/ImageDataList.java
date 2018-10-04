package android.lifeistech.com.classlog;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ImageDataList extends RealmObject {

    // cf. https://qiita.com/boohbah/items/b515a16aa0034f622a85

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
