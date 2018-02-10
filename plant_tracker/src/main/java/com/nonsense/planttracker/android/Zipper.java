package com.nonsense.planttracker.android;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Derek Brooks on 1/30/2018.
 */

public class Zipper {
    private static final int BUFFER_SIZE = 4096;

    public static String extractJsonFileContents(InputStream archiveStream, String archivePath) {

        try {
            ZipInputStream zis = new ZipInputStream(archiveStream);
            StringBuilder json = new StringBuilder();

            ZipEntry ze = null;
            while((ze=zis.getNextEntry()) != null)  {
                if (ze.getName().equals(archivePath))   {
                    InputStreamReader isr = new InputStreamReader(zis);
                    BufferedReader br = new BufferedReader(isr);

                    String line;
                    while((line=br.readLine()) != null)    {
                        json.append(line);
                    }

                    br.close();
                    zis.close();
                    break;
                }
            }

            //TODO convert to string and return

            return json.toString();
        }
        catch(Exception e)  {
            e.printStackTrace();
        }

        return null;
    }

    //TODO fix forward slash problem we have, archives currently put things in the root /file.json instead of just file.json.
    public static void extractFileToLocation(InputStream is, String file, String destination)  {
        try {
            String outPath = destination + "/" + file;
            Log.d("ExtractToLocation", "Path: " + outPath);

            ZipInputStream zis = new ZipInputStream(is);
            FileOutputStream fos = new FileOutputStream(outPath);
            byte[] buffer = new byte[BUFFER_SIZE];

            ZipEntry ze = null;
            while((ze=zis.getNextEntry()) != null)  {
                if (ze.getName().endsWith(file))  {
                    int bytesRead = 0;
                    byte buf[] = new byte[BUFFER_SIZE];
                    while((bytesRead=zis.read(buf, 0, BUFFER_SIZE)) != -1)   {
                        fos.write(buf, 0, bytesRead);
                    }
                }
            }

            fos.close();
            zis.close();
        }
        catch(Exception e)  {
            e.printStackTrace();
        }
    }

    public static boolean importTrackerDataArchive(String basePath, String zipFile) throws
            IOException {
        return importTrackerDataArchive(basePath, new FileInputStream(zipFile));
    }

    public static boolean importTrackerDataArchive(String basePath, InputStream inputStream)  {
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                    inputStream));

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

    public static boolean compressTrackerData(ArrayList<String> files, String zipFile)    {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

            for(String file : files) {
                String archivePath = file.substring(file.lastIndexOf('/')+1);
                if ((new File(file)).isDirectory())   {
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
        Log.d("ZIP-OUT-FOLDER", archivePath);

        File folder = new File(path);
        for(File f : folder.listFiles()) {
            if (f.isDirectory())    {
                zipFolders(zos, archivePath + f.getName() + "/", f.getPath());
            }
            else    {
                zipFile(zos, archivePath, f.getPath());
            }
        }
    }

    private static void zipFile(ZipOutputStream zos, String archivePath, String file)   {
        try {
            BufferedInputStream bis = null;
            FileInputStream fis = new FileInputStream(file);

            bis = new BufferedInputStream(fis);

            String fileName = file.substring(file.lastIndexOf("/") + 1);

            String entryPath = archivePath + "/" + fileName;
            Log.d("ZIP-OUT-FILE", entryPath);
            ZipEntry entry = new ZipEntry(entryPath);
            zos.putNextEntry(entry);

            int readBytes = 0;
            byte buf[] = new byte[BUFFER_SIZE];
            while((readBytes = bis.read(buf, 0, BUFFER_SIZE)) != -1) {
                zos.write(buf, 0, readBytes);
            }

            zos.closeEntry();
            bis.close();
        }
        catch(Exception e)  {
            e.printStackTrace();
        }
    }
}
