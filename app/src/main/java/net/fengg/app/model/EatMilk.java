package net.fengg.app.model;

import android.support.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * @author zhangfeng_2017
 * @date 2018/1/4
 */

@Entity
public class EatMilk implements Comparable<EatMilk> {
    @Id
    long id;
    long start;
    long end;
    long duration;
    boolean left;
    int ml;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public int getMl() {
        return ml;
    }

    public void setMl(int ml) {
        this.ml = ml;
    }

    @Override
    public int compareTo(@NonNull EatMilk o) {
        int res = (int) (o.getEnd() - this.getEnd());
        return res;
    }
}
