package net.fengg.app.tool;

import android.os.Environment;
import android.util.Log;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;

import java.io.File;
import java.util.Arrays;

/**
 * log封装，可选择使用XLog或android.util.Log
 *
 * 用法：UtilLog.getInstance().d("message");
 *
 * @author zhangfeng_2017
 * @date 2017/11/17.
 */
public class UtilLog {
    private static String TAG = "UtilLog";
    private static UtilLog instance;
    /**
     * 使用android.util.Log
     */
    private boolean defaultLog = true;

    public static UtilLog getInstance() {
        if(null == instance) {
            instance = new UtilLog();
        }
        //默认使用XLog
        instance.useDefaultLog(false);
        return instance;
    }

    public boolean isDefaultLog() {
        return defaultLog;
    }

    /**
     * 设置使用android.util.Log
     * @param defaultLog
     * @return
     */
    public UtilLog useDefaultLog(boolean defaultLog) {
        this.defaultLog = defaultLog;
        return this;
    }

    UtilLog() {
        initXLog();
    }

    /**
     * Initialize XLog.
     */
    private void initXLog() {
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(TAG)
//                .st(0)
                .build();
        // Printer that print the log using android.util.Log
        Printer androidPrinter = new AndroidPrinter();
        // Printer that print the log to the file system
        Printer filePrinter = new FilePrinter
                // Specify the path to save log file
                .Builder(new File(Environment.getExternalStorageDirectory(), TAG).getPath())
                // Default: ChangelessFileNameGenerator("log")
                .fileNameGenerator(new LogFileNameGenerator())
                // Default: FileSizeBackupStrategy(1024 * 1024)
                .backupStrategy(new FileSizeBackupStrategy(1024 * 1024))
                // Default: DefaultFlattener
                .logFlattener(new ClassicFlattener())
                .build();

        XLog.init(
                config,
                androidPrinter);
                //, filePrinter);

    }

    public void v(String message) {
        v(null, message);
    }

    public void v(String tag, String message) {
        v(tag, message, null);
    }

    public void v(String tag, Object object) {
        String message;
        if(null == object) {
            message = "null";
        } else {
            message = object.toString();
        }
        v(tag, message);
    }

    public void v(String tag, Object[] array) {
        String message;
        if(null == array) {
            message = "null";
        } else {
            message = Arrays.deepToString(array);
        }
        v(tag, message);
    }

    /**
     * 记录带format格式日志
     * @param tag
     * @param format
     * @param args
     */
    public void v_f(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public void v(String tag, String message, Throwable tr, Object... args) {
        if(null == message) {
            message = "null";
        }
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == tr) {
                if(null != args) {
                    Log.v(tag, formatArgs(message, args));
                } else {
                    Log.v(tag, message);
                }
            } else {
                if(null != args) {
                    Log.v(tag, formatArgs(message, args), tr);
                } else {
                    Log.v(tag, message, tr);
                }
            }
        } else {
            if(null == tag) {
                if(null == tr) {
                    if(null != args) {
                        XLog.v(message, args);
                    } else {
                        XLog.v(message);
                    }
                } else {
                    if(null != args) {
                        XLog.v(message, tr, args);
                    } else {
                        XLog.v(message, tr);
                    }
                }
            } else {
                //有tag就是临时性日志
                if(null == tr) {
                    if(null != args) {
                        XLog.tag(tag).v(message, args);
                    } else {
                        XLog.tag(tag).v(message);
                    }
                } else {
                    if(null != args) {
                        XLog.tag(tag).v(message, tr, args);
                    } else {
                        XLog.tag(tag).v(message, tr);
                    }
                }
            }
        }
    }

    public void d(String message) {
        d(null, message);
    }

    public void d(String tag, String message) {
        d(tag, message, null);
    }

    public void d(String tag, Object object) {
        String message;
        if(null == object) {
            message = "null";
        } else {
            message = object.toString();
        }
        d(tag, message);
    }

    public void d(String tag, Object[] array) {
        String message;
        if(null == array) {
            message = "null";
        } else {
            message = Arrays.deepToString(array);
        }
        d(tag, message);
    }

    public void d_f(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public void d(String tag, String message, Throwable tr, Object... args) {
        if(null == message) {
            message = "null";
        }
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == tr) {
                if(null != args) {
                    Log.d(tag, formatArgs(message, args));
                } else {
                    Log.d(tag, message);
                }
            } else {
                if(null != args) {
                    Log.d(tag, formatArgs(message, args), tr);
                } else {
                    Log.d(tag, message, tr);
                }
            }
        } else {
            if(null == tag) {
                if(null == tr) {
                    if(null != args) {
                        XLog.d(message, args);
                    } else {
                        XLog.d(message);
                    }
                } else {
                    if(null != args) {
                        XLog.d(message, tr, args);
                    } else {
                        XLog.d(message, tr);
                    }
                }
            } else {
                if(null == tr) {
                    if(null != args) {
                        XLog.tag(tag).d(message, args);
                    } else {
                        XLog.tag(tag).d(message);
                    }
                } else {
                    if(null != args) {
                        XLog.tag(tag).d(message, tr, args);
                    } else {
                        XLog.tag(tag).d(message, tr);
                    }
                }
            }
        }
    }

    public void i(String message) {
        i(null, message);
    }

    public void i(String tag, String message) {
        i(tag, message, null);
    }

    public void i(String tag, Object object) {
        String message;
        if(null == object) {
            message = "null";
        } else {
            message = object.toString();
        }
        i(tag, message);
    }

    public void i(String tag, Object[] array) {
        String message;
        if(null == array) {
            message = "null";
        } else {
            message = Arrays.deepToString(array);
        }
        i(tag, message);
    }

    public void i_f(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public void i(String tag, String message, Throwable tr, Object... args) {
        if(null == message) {
            message = "null";
        }
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == tr) {
                if(null != args) {
                    Log.i(tag, formatArgs(message, args));
                } else {
                    Log.i(tag, message);
                }
            } else {
                if(null != args) {
                    Log.i(tag, formatArgs(message, args), tr);
                } else {
                    Log.i(tag, message, tr);
                }
            }
        } else {
            if(null == tag) {
                if(null == tr) {
                    if(null != args) {
                        XLog.i(message, args);
                    } else {
                        XLog.i(message);
                    }
                } else {
                    if(null != args) {
                        XLog.i(message, tr, args);
                    } else {
                        XLog.i(message, tr);
                    }
                }
            } else {
                if(null == tr) {
                    if(null != args) {
                        XLog.tag(tag).i(message, args);
                    } else {
                        XLog.tag(tag).i(message);
                    }
                } else {
                    if(null != args) {
                        XLog.tag(tag).i(message, tr, args);
                    } else {
                        XLog.tag(tag).i(message, tr);
                    }
                }
            }
        }
    }

    public void w(String message) {
        w(null, message);
    }

    public void w(String tag, String message) {
        w(tag, message, null);
    }

    public void w(String tag, Object object) {
        String message;
        if(null == object) {
            message = "null";
        } else {
            message = object.toString();
        }
        w(tag, message);
    }

    public void w(String tag, Object[] array) {
        String message;
        if(null == array) {
            message = "null";
        } else {
            message = Arrays.deepToString(array);
        }
        w(tag, message);
    }

    public void w_f(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public void w(String tag, String message, Throwable tr, Object... args) {
        if(null == message) {
            message = "null";
        }
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == tr) {
                if(null != args) {
                    Log.w(tag, formatArgs(message, args));
                } else {
                    Log.w(tag, message);
                }
            } else {
                if(null != args) {
                    Log.w(tag, formatArgs(message, args), tr);
                } else {
                    Log.w(tag, message, tr);
                }
            }
        } else {
            if(null == tag) {
                if(null == tr) {
                    if(null != args) {
                        XLog.w(message, args);
                    } else {
                        XLog.w(message);
                    }
                } else {
                    if(null != args) {
                        XLog.w(message, tr, args);
                    } else {
                        XLog.w(message, tr);
                    }
                }
            } else {
                if(null == tr) {
                    if(null != args) {
                        XLog.tag(tag).w(message, args);
                    } else {
                        XLog.tag(tag).w(message);
                    }
                } else {
                    if(null != args) {
                        XLog.tag(tag).w(message, tr, args);
                    } else {
                        XLog.tag(tag).w(message, tr);
                    }
                }
            }
        }
    }

    public void e(String message) {
        e(null, message);
    }

    public void e(String tag, String message) {
        e(tag, message, null);
    }

    public void e(String tag, Object object) {
        String message;
        if(null == object) {
            message = "null";
        } else {
            message = object.toString();
        }
        e(tag, message);
    }

    public void e(String tag, Object[] array) {
        String message;
        if(null == array) {
            message = "null";
        } else {
            message = Arrays.deepToString(array);
        }
        e(tag, message);
    }

    public void e_f(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public void e(String tag, String message, Throwable tr, Object... args) {
        if(null == message) {
            message = "null";
        }
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == tr) {
                if(null != args) {
                    Log.e(tag, formatArgs(message, args));
                } else {
                    Log.e(tag, message);
                }
            } else {
                if(null != args) {
                    Log.e(tag, formatArgs(message, args), tr);
                } else {
                    Log.e(tag, message, tr);
                }
            }
        } else {
            if(null == tag) {
                if(null == tr) {
                    if(null != args) {
                        XLog.e(message, args);
                    } else {
                        XLog.e(message);
                    }
                } else {
                    if(null != args) {
                        XLog.e(message, tr, args);
                    } else {
                        XLog.e(message, tr);
                    }
                }
            } else {
                if(null == tr) {
                    if(null != args) {
                        XLog.tag(tag).e(message, args);
                    } else {
                        XLog.tag(tag).e(message);
                    }
                } else {
                    if(null != args) {
                        XLog.tag(tag).e(message, tr, args);
                    } else {
                        XLog.tag(tag).e(message, tr);
                    }
                }
            }
        }
    }

    public void exception(Throwable tr) {
        exception(null, tr);
    }

    public void exception(String tag, Throwable tr) {
        d(tag, Log.getStackTraceString(tr));
    }

    public void json(String json) {
        json(null, json);
    }

    public void json(String tag, String json) {
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == json) {
                json = "null";
            }
            Log.d(tag, json);
        } else {
            if(null == tag) {
                XLog.json(json);
            } else {
                XLog.tag(tag).json(json);
            }
        }
    }

    public void xml(String xml) {
        xml(null, xml);
    }

    public void xml(String tag, String xml) {
        if(isDefaultLog()) {
            if(null == tag) {
                tag = TAG;
            }
            if(null == xml) {
                xml = "null";
            }
            Log.d(tag, xml);
        } else {
            if(null == tag) {
                XLog.xml(xml);
            } else {
                XLog.tag(tag).xml(xml);
            }
        }
    }

    /**
     * jni层调用记录Log
     * @param message
     */
    public static void verbose(String message) {
        getInstance().v(message);
    }

    public static void verbose(String format, Object... args) {
        getInstance().v_f(null, format, args);
    }

    public static void debug(String message) {
        getInstance().d(message);
    }

    public static void info(String message) {
        getInstance().i(message);
    }

    public static void warn(String message) {
        getInstance().w(message);
    }

    public static void error(String message) {
        getInstance().e(message);
    }

    /**
     * Format a string with arguments.
     *
     * @param format the format string, null if just to concat the arguments
     * @param args   the arguments
     * @return the formatted string
     */
    private String formatArgs(String format, Object... args) {
        if (format != null) {
            return String.format(format, args);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, N = args.length; i < N; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(args[i]);
            }
            return sb.toString();
        }
    }
}
