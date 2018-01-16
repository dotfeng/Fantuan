package net.fengg.app.model;

import android.support.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * @author zhangfeng_2017
 * @date 2018/1/5
 */
@Entity
public class Shit implements Comparable<Shit> {
    @Id
    long id;
    long time;
    String status;
    long duration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(@NonNull Shit o) {
        int res = (int) (o.getTime() - this.getTime());
        return res;
    }
}
