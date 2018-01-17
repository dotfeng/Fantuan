package net.fengg.app.model;

import android.support.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * @author zhangfeng_2017
 * @date 2018/1/10
 */
@Entity
public class Body implements Comparable<Body> {
    @Id
    long id;
    float weight;
    float height;
    float temperature;
    long time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(@NonNull Body o) {
        int res = (int) (o.getTime() - this.getTime());
        return res;
    }
}
