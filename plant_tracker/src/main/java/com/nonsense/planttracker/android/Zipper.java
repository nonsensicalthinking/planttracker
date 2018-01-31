package com.nonsense.planttracker.android;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Derek Brooks on 1/30/2018.
 */

public class Zipper {
    private static final int BUFFER_SIZE = 2048;


    public static boolean importTrackerDataArchive(String basePath, String zipFile)  {

        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                    new FileInputStream(zipFile)));

            ZipEntry ze = null;
            while((ze=zis.getNextEntry()) != null)   {
                File f = new File(ze.getName());
                if (ze.isDirectory())   {
                    f.mkdirs();
                    continue;
                }

                FileOutputStream fos = new FileOutputStream(basePath + "/" + ze.getName());

                int bytesRead = 0;
                byte buf[] = new byte[BUFFER_SIZE];
                while((bytesRead=zis.read(buf, 0, BUFFER_SIZE)) != -1)   {
                    fos.write(buf, 0, bytesRead);
                }

                fos.close();
                zis.closeEntry();
            }

            zis.close();
            return true;
        }
        catch(Exception e)  {
            e.printStackTrace();
        }

        return false;
    }

//    private static void unZipFiles(ZipInputStream zis, String path)    {
//        File f = new File(path);
//        if (f.isDirectory())    {
//
//        }
//        else    {
//
//        }
//
//
//    }

    public static boolean compressTrackerData(ArrayList<String> files, String zipFile)    {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

            for(String file : files) {
                if ((new File(file)).isDirectory())   {
                    ZipEntry ze = new ZipEntry(file);
                    zos.putNextEntry(ze);
                    String archivePath = file.substring(file.lastIndexOf('/') + 1);
                    zipFolders(zos, archivePath, file);
                }
                else    {
                    zipFile(zos, "", file);
                }
            }

            zos.close();

            return true;
        }
        catch(Exception e)  {
            e.printStackTrace();
        }

        return false;
    }

    private static void zipFolders(ZipOutputStream zos, String archivePath, String path) throws
            IOException    {

        File folder = new File(path);

        for(File f : folder.listFiles()) {
            if (f.isDirectory())    {
                zipFolders(zos, "/" + archivePath + "/" + f.getName(), f.getPath());
            }
            else    {
                zipFile(zos, archivePath, f.getPath());
            }
        }
    }

    private static void zipFile(ZipOutputStream zos, String archivePath, String file) throws
            IOException {

        BufferedInputStream bis = null;
        FileInputStream fis = new FileInputStream(file);

        bis = new BufferedInputStream(fis);

        String fileName = file.substring(file.lastIndexOf("/") + 1);
        ZipEntry entry = new ZipEntry(archivePath + "/" + fileName);
        zos.putNextEntry(entry);

        int readBytes = 0;
        byte buf[] = new byte[BUFFER_SIZE];
        while((readBytes = bis.read(buf, 0, BUFFER_SIZE)) != -1) {
            zos.write(buf, 0, readBytes);
        }

        bis.close();
    }


    //TODO unzip!

//
//    public void zip() {
//        try  {
//            BufferedInputStream origin = null;
//            FileOutputStream dest = new FileOutputStream(_zipFile);
//
//            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
//
//            byte data[] = new byte[BUFFER];
//
//            for(int i=0; i < _files.length; i++) {
//                Log.v("Compress", "Adding: " + _files[i]);
//                FileInputStream fi = new FileInputStream(_files[i]);
//                origin = new BufferedInputStream(fi, BUFFER);
//                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
//                out.putNextEntry(entry);
//                int count;
//                while ((count = origin.read(data, 0, BUFFER)) != -1) {
//                    out.write(data, 0, count);
//                }
//                origin.close();
//            }
//
//            out.close();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}
