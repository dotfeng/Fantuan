package net.fengg.app.model;

import android.support.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * @author zhangfeng_2017
 * @date 2018/1/5
 */
@Entity
public class Sleep implements Comparable<Sleep> {
    @Id
    long id;
    boolean sleep;
    long time;
    long duration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(@NonNull Sleep o) {
        int res = (int) (o.getTime() - this.getTime());
        return res;
    }
}
