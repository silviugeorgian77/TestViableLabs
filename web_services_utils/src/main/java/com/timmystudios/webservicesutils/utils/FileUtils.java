package com.timmystudios.webservicesutils.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.system.Os;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FileUtils {

    /**
     * Removes illegal file name characters.
     */
    public static String correctFileName(String fileName) {
        final char[] ILLEGAL_FILE_NAME_CHARACTERS = {
                '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'
        };
        char letter;
        char invalidLetter;
        boolean validLetter;
        String correctFileName = "";
        for (int i = 0; i < fileName.length(); i++) {
            letter = fileName.charAt(i);
            validLetter = true;
            for (int j = 0; j < ILLEGAL_FILE_NAME_CHARACTERS.length; j++) {
                invalidLetter = ILLEGAL_FILE_NAME_CHARACTERS[j];
                if (letter == invalidLetter) {
                    validLetter = false;
                    break;
                }
            }
            if (validLetter) {
                correctFileName += letter;
            }
        }
        return  correctFileName;
    }

    public static long getLastFileAccessTime(String filePath) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return Os.lstat(filePath).st_atime;
            } else {
                Class<?> clazz = Class.forName("libcore.io.Libcore");
                Field field = clazz.getDeclaredField("os");
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object os = field.get(null);

                Method method = os.getClass().getMethod("lstat", String.class);
                Object lstat = method.invoke(os, filePath);

                field = lstat.getClass().getDeclaredField("st_atime");
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.getLong(lstat);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static void copyFile(final String fromPath,
                                final String toPath,
                                final OnFileCopiedListener listener) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                FileInputStream inStream = null;
                FileOutputStream outStream = null;
                boolean success = false;
                try{
                    File fromFile = new File(fromPath);
                    File toFile = new File(toPath);

                    String parentPath = toFile.getParent();
                    if (parentPath != null) {
                        File parentDirectory = new File(parentPath);
                        if (!parentDirectory.exists()) {
                            parentDirectory.mkdirs();
                        }
                    }
                    if (toFile.exists()) {
                        toFile.delete();
                    }
                    toFile.createNewFile();

                    inStream = new FileInputStream(fromFile);
                    outStream = new FileOutputStream(toFile);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, length);
                    }

                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inStream != null) {
                            inStream.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                final boolean finalSuccess = success;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFileCopied(finalSuccess, fromPath, toPath);
                        }
                    }
                });
            }
        };
        thread.start();
    }

    public interface OnFileCopiedListener {
        void onFileCopied(boolean success, String fromPath, String toPath);
    }

}
