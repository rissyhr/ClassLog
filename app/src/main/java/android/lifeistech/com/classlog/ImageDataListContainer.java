package android.lifeistech.com.classlog;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ImageDataListContainer extends RealmObject {

    /* 複数のアルバム(ImageDataList)を所持する時間割 */

    private String timestamp; // 新規作成日時

    private RealmList<ImageDataList> classSchedule;
    private boolean isSelected; // trueのとき、アプリ起動時にそのインスタンスのclassScheduleが表示される

    private boolean hasSaturday;
    private boolean hasSunday; // 日曜日は現在未実装
    private boolean hasT1, hasT2, hasT3, hasT4, hasT5, hasT6, hasT7, hasT8, hasT9, hasT10;


    public RealmList<ImageDataList> getClassSchedule() {
        return classSchedule;
    }

    public void setClassSchedule(RealmList<ImageDataList> classSchedule) {
        this.classSchedule = classSchedule;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean hasSaturday() {
        return hasSaturday;
    }

    public void setHasSaturday(boolean hasSaturday) {
        this.hasSaturday = hasSaturday;
    }

    public boolean hasSunday() {
        return hasSunday;
    }

    public void setHasSunday(boolean hasSunday) {
        this.hasSunday = hasSunday;
    }

    public boolean hasT1() {
        return hasT1;
    }

    public void setHasT1(boolean hasT1) {
        this.hasT1 = hasT1;
    }

    public boolean hasT2() {
        return hasT2;
    }

    public void setHasT2(boolean hasT2) {
        this.hasT2 = hasT2;
    }

    public boolean hasT3() {
        return hasT3;
    }

    public void setHasT3(boolean hasT3) {
        this.hasT3 = hasT3;
    }

    public boolean hasT4() {
        return hasT4;
    }

    public void setHasT4(boolean hasT4) {
        this.hasT4 = hasT4;
    }

    public boolean hasT5() {
        return hasT5;
    }

    public void setHasT5(boolean hasT5) {
        this.hasT5 = hasT5;
    }

    public boolean hasT6() {
        return hasT6;
    }

    public void setHasT6(boolean hasT6) {
        this.hasT6 = hasT6;
    }

    public boolean hasT7() {
        return hasT7;
    }

    public void setHasT7(boolean hasT7) {
        this.hasT7 = hasT7;
    }

    public boolean hasT8() {
        return hasT8;
    }

    public void setHasT8(boolean hasT8) {
        this.hasT8 = hasT8;
    }

    public boolean hasT9() {
        return hasT9;
    }

    public void setHasT9(boolean hasT9) {
        this.hasT9 = hasT9;
    }

    public boolean hasT10() {
        return hasT10;
    }

    public void setHasT10(boolean hasT10) {
        this.hasT10 = hasT10;
    }



}
