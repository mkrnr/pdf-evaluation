package de.exciteproject.pdf_evaluation.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.io.Files;

import de.exciteproject.refext.util.FileUtils;

public class FileMapper {

    public static void main(String[] args) throws IOException {
        File inputDirectory = new File(args[0]);
        File outputDirectory = new File(args[1]);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        File outputListFile = new File(args[2]);

        int count = 1;
        ArrayList<String> mappings = new ArrayList<String>();
        for (File inputFile : FileUtils.listFilesRecursively(inputDirectory)) {
            String outputFileName = count + ".pdf";
            mappings.add(inputFile.getName().toString() + "\t" + outputFileName);
            Files.copy(inputFile, new File(outputDirectory.getAbsolutePath() + File.separator + outputFileName));
            count++;
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputListFile));
        for (String mapping : mappings) {
            bufferedWriter.write(mapping);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();

    }

}
