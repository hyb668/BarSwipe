package com.barswipe.draweePhotoView.lib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * soli edit
 */
public class FileUtil {

    private static boolean isExternalMemoryAvailable() {
        return Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取目录
     *
     * @param context
     * @return
     */
    public static File getRootDir(Context context, boolean isInAndroidDataFile) {

        File targetDir = null;

        try {
            if (isExternalMemoryAvailable()) {
                if (isInAndroidDataFile)
                    targetDir = new File(context.getExternalCacheDir().getPath() + "/Vae+");
//                targetDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + context.getApplicationInfo().packageName + "/cache/Vae+");
                else
                    targetDir = new File(Environment.getExternalStorageDirectory(), "Vae+");
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
            }

        } catch (Exception e) {
        }

        if (targetDir == null || !targetDir.exists()) {
            targetDir = context.getCacheDir();
        }

        return targetDir;
    }

    /**
     * 获取目录
     *
     * @param context
     * @param name
     * @return
     */
    public static File getDir(Context context, String name) {
        File file = new File(getRootDir(context, true), name);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    /**
     * @param context
     * @param name
     * @param isInAndroidDataFile
     * @return
     */
    public static File getDir(Context context, String name, boolean isInAndroidDataFile) {
        File file = new File(getRootDir(context, isInAndroidDataFile), name);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    /**
     * 获取文件
     *
     * @param context
     * @param dir
     * @param fileName
     * @return
     */
    public static File getFile(Context context, String dir, String fileName) {
        return new File(getDir(context, dir), fileName);
    }

    /**
     * 获取文件
     *
     * @param context
     * @param dir
     * @param fileName
     * @return
     */
    public static File getFile(Context context, String dir, String fileName, boolean isInData) {
        return new File(getDir(context, dir, isInData), fileName);
    }

    /**
     * @param sourceStr
     * @return
     */
    public static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return result;
    }

    /**
     * 获取网络下载图片地址保存目录
     *
     * @param context
     * @param imageUrl
     * @return
     */
    public static File getDownLoadImageFile(Context context, String imageUrl) {
        return getFile(context, "download", MD5(imageUrl) + getsuffer(imageUrl), false);
    }

    /**
     * @param url
     * @return
     */
    private static String getsuffer(String url) {
        int index = url.lastIndexOf(".");
        if (index != -1)
            return url.substring(index, url.length());

        return ".jpeg";
    }

    /**
     * 获取网络下载音频保存目录
     *
     * @param context
     * @param path
     * @return
     */
    public static File getDownLoadAudioFile(Context context, String path) {
        return getFile(context, "audio", MD5(path) + getsuffer(path), true);
    }

    /**
     * 照片文件默认放到Vae+/VaePic下面
     *
     * @param context
     * @param head    文件名，后面加时间参数
     * @return
     */
    public static String preparePicturePath(Context context, String head) {
        return getFile(context, "capture", getPictureName(head), false).getAbsolutePath();
    }

    /**
     * 上传图片临时存放文件的位置
     *
     * @param context
     * @return
     */
    public static String getPicUploadTempPath(Context context, String path) {
        return getFile(context, "upload", "fans_pic_upload" + getsuffer(path), true).getAbsolutePath();
    }

    /**
     *
     * @param path
     * @return
     */
    public static String getPicUploadTempName(String path)
    {
        return "fans_pic_upload" + getsuffer(path);
    }

    /**
     * @param context
     * @param fileName
     * @return
     */
    public static String getTusdkEditFilePath(Context context, String fileName) {
        return getFile(context, "tusdk", fileName, true).getAbsolutePath();
    }
//
//    /**
//     * 指定文件目录和文件名
//     *
//     * @param context
//     * @param dir
//     * @param head
//     * @return
//     */
//    public static String preparePicturePath(Context context, String dir, String head) {
//        return getFile(context, dir, getPictureName(head), false).getAbsolutePath();
//    }


    /**
     * 根据时间来设置照片的名字
     *
     * @return
     */
    public static String getPictureName(String head) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss");
        Date date = new Date(System.currentTimeMillis());

        return head + format.format(date) + ".jpeg";
    }


    /**
     * 把assets里的文件copy到指定文件目录下，同名拷贝
     *
     * @param context
     * @param dir
     * @param assetFileName assets文件的名称
     * @return
     */
    public static File CopyAssetsToXXX(Context context, String dir,
                                       String assetFileName) {
        File file = getFile(context, dir, assetFileName);
        try {
            OutputStream myOutput = new FileOutputStream(file);
            InputStream myInput = context.getAssets().open(assetFileName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }

            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (Exception e) {
            file = null;
        }
        return file;
    }

    /**
     * 读取文本数据
     *
     * @param context  程序上下文
     * @param fileName 文件名
     * @return String, 读取到的文本内容，失败返回null
     */
    public static String readAssets(Context context, String fileName) {
        InputStream is = null;
        String content = null;
        try {
            is = context.getAssets().open(fileName);
            if (is != null) {

                byte[] buffer = new byte[1024];
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                while (true) {
                    int readLength = is.read(buffer);
                    if (readLength == -1)
                        break;
                    arrayOutputStream.write(buffer, 0, readLength);
                }
                is.close();
                arrayOutputStream.close();
                content = new String(arrayOutputStream.toByteArray());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            content = null;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return content;
    }

    /**
     * @param b
     * @param ret
     * @return
     */
    public static File getFileFromBytes(byte[] b, File ret) {

        BufferedOutputStream stream = null;
        try {

            FileOutputStream fstream = new FileOutputStream(ret);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * @param bitmap
     * @param file
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static boolean writeBitmapToFile(Bitmap bitmap, File file) throws IOException, FileNotFoundException {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * copy
     *
     * @param oldfile
     * @param newPath
     * @param showUpdateMei
     */
    public static void copyFile(Context ctx, File oldfile, File newPath, boolean showUpdateMei) {
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
        } finally {
            /**更新媒体库*/
            if (showUpdateMei) {
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(newPath));
                ctx.sendBroadcast(scanIntent);
            }
        }
    }

}
