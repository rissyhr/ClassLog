package android.lifeistech.com.classlog;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ImageDataListContainer extends RealmObject {

    /* 複数のアルバム(ImageDataList)を所持する時間割 */

    private RealmList<ImageDataList> classSchedule;

    public RealmList<ImageDataList> getClassSchedule() {
        return classSchedule;
    }

    public void setClassSchedule(RealmList<ImageDataList> classSchedule) {
        this.classSchedule = classSchedule;
    }
}
