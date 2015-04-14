package com.connorlinfoot.backupmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPManager {
    List<String> fileList = new ArrayList<String>();
    private static String OUTPUT_ZIP_FILE = null;
    private static String SOURCE_FOLDER = null;

    public ZIPManager(String output, String source) {
        OUTPUT_ZIP_FILE = output;
        SOURCE_FOLDER = source;
    }

    public void doZip() {
        this.generateFileList(new File(SOURCE_FOLDER));
        this.zipIt(OUTPUT_ZIP_FILE);
    }

    private void zipIt(String zipFile) {

        byte[] buffer = new byte[1024];

        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            if (BackupManager.advancedLogs) System.out.println("Output to Zip : " + zipFile);

            for (String file : this.fileList) {
                if (BackupManager.advancedLogs) System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            zos.close();

            if (BackupManager.advancedLogs) System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void generateFileList(File node) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }

}
