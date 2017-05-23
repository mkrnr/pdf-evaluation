package de.exciteproject.pdf_evaluation.util;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    /**
     * Deletes the content of targetDirectory and then copies into it the
     * content of sourceDirectory
     * 
     * @throws IOException
     */
    public static void copyAndReplaceInDir(File sourceDirectory, File targetDirectory) throws IOException {
        org.apache.commons.io.FileUtils.cleanDirectory(targetDirectory);
        org.apache.commons.io.FileUtils.copyDirectory(sourceDirectory, targetDirectory);
    }
}
