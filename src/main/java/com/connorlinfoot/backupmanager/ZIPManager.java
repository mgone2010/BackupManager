package com.connorlinfoot.backupmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPManager {
    private static String zipFile = null;
    private static String sourceFolder = null;

    public ZIPManager(String output, String source) {
        zipFile = output;
        sourceFolder = source;
    }

    public void doZip() {
        try {
            File dirObj = new File(sourceFolder);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            if (BackupManager.advancedLogs) System.out.println("Creating: " + zipFile);
            addDir(dirObj, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            if (BackupManager.advancedLogs) System.out.println("Adding File: " + files[i].getAbsolutePath());
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }


}
