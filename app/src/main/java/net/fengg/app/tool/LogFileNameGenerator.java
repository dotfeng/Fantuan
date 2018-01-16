package net.fengg.app.tool;

import com.elvishew.xlog.printer.file.naming.FileNameGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zhangfeng_2017 on 2017/11/20.
 */

public class LogFileNameGenerator implements FileNameGenerator {
    ThreadLocal<SimpleDateFormat> mLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", Locale.US);
        }
    };

    @Override
    public boolean isFileNameChangeable() {
        return true;
    }

    @Override
    public String generateFileName(int logLevel, long timestamp) {
        SimpleDateFormat sdf = mLocalDateFormat.get();
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
