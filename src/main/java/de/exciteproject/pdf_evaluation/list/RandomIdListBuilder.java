package de.exciteproject.pdf_evaluation.list;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.util.FileUtils;

public class RandomIdListBuilder {

    public static void main(String[] args) throws IOException {
        File inputDirectory = new File(args[0]);
        File outputFile = new File(args[1]);

        RandomIdListBuilder.writeRandomListFile(inputDirectory, outputFile);
    }

    public static void writeRandomListFile(File inputDirectory, File outputFile) throws IOException {
        List<File> inputFiles = FileUtils.listFilesRecursively(inputDirectory);
        Collections.shuffle(inputFiles);

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (File inputFile : inputFiles) {
            String inputFileId = FilenameUtils.removeExtension(inputFile.getName());
            writer.write(inputFileId);
            writer.newLine();
        }
        writer.close();
    }
}
