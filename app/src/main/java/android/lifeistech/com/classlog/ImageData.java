package android.lifeistech.com.classlog;

import io.realm.RealmObject;

public class ImageData extends RealmObject {
    public String name;
    public String small, medium, large;
    public String timestamp;

    public ImageData() {
    }

}
