package com.ys.temperaturelib.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * adb shell am start -n com.yishengkj.testtools/MainActivity
 * Created by Administrator on 2017/12/14.
 */

public class FileUtil {

    private static final String TAG = "FileUtil";
    /**
     * 追加文件：使用FileWriter
     *
     * @param fileName
     * @param content
     */
    public static void writeFileAppend(String fileName, String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void writeFile(File file, String content) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFile(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readline = "";
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static String exec(String command) {
        Process process = null;
        DataOutputStream os = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            if (process.waitFor() != 0) {
                System.err.println("exit value = " + process.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stringBuffer.toString();
    }


    public static String getValueFromProp(String key) {
        String value = "";
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            value = (String) getMethod.invoke(classType, new Object[]{key});
        } catch (Exception e) {
        }
        return value;
    }
}
