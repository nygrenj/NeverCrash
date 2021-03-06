package nnnn.aaaaa.nevercrash;

import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.XposedHelpers;

/**
 * inspired by https://github.com/android-notes/Cockroach
 */
public class CrashHandler {


    public interface ExceptionHandler {

        void handlerException(Throwable throwable);
    }

    private CrashHandler() {
    }

    private static ExceptionHandler sExceptionHandler;
    private static Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;
    private static boolean sInstalled = false;


    public static synchronized void insert(ExceptionHandler exceptionHandler) {
        if (sInstalled) {
            return;
        }
        sInstalled = true;
        sExceptionHandler = exceptionHandler;

        final Looper targetLooper = (Looper) XposedHelpers.callStaticMethod(Looper.class, "getMainLooper");
        new Handler(targetLooper).post(() -> {

            while (true) {
                try {
                    XposedHelpers.callStaticMethod(Looper.class, "loop");
                } catch (Throwable e) {
                   /* if (e instanceof RuntimeException) {
                        return;
                    }*/
                    if (sExceptionHandler != null) {
                        sExceptionHandler.handlerException(e);
                    }
                }
            }
        });

        sUncaughtExceptionHandler = (Thread.UncaughtExceptionHandler) XposedHelpers.callStaticMethod(Thread.class, "getDefaultUncaughtExceptionHandler");
        XposedHelpers.callStaticMethod(Thread.class, "setDefaultUncaughtExceptionHandler", (Thread.UncaughtExceptionHandler) (t, e) -> {
            if (sExceptionHandler != null) {
                sExceptionHandler.handlerException(e);
            }
        });

    }



}
