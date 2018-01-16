package net.fengg.app;

import android.app.Application;
import android.os.Environment;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import net.fengg.app.BuildConfig;
import net.fengg.app.model.MyObjectBox;

import java.io.File;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

/**
 * @author zhangfeng_2017
 * @date 2018/1/4
 */

public class AppApplication extends Application {
    private BoxStore boxStore;
    @Override
    public void onCreate() {
        super.onCreate();
//        CrashReport.initCrashReport(getApplicationContext());
        Bugly.init(getApplicationContext(), "0b73ed1b1c", false);
        boxStore = MyObjectBox.builder().androidContext(AppApplication.this).build();
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(this);
        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
