package de.exciteproject.pdf_evaluation.list;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.exciteproject.refext.util.FileUtils;

/**
 * Class for generating a file list with random order given a directory of files
 */
public class RandomFileListGenerator {

    public static void main(String[] args) throws IOException {
        File inputDirectory = new File(args[0]);
        File outputFile = new File(args[1]);
        List<File> randomlySortedFiles = RandomFileListGenerator.randomlySelectFiles(inputDirectory, Integer.MAX_VALUE,
                new ArrayList<File>());

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (File file : randomlySortedFiles) {
            bufferedWriter.write(file.getAbsolutePath().replaceAll(inputDirectory.getAbsolutePath() + "/", ""));
            bufferedWriter.newLine();
        }
        bufferedWriter.close();

    }

    public static List<File> randomlySelectFiles(File inputDirectory, int numberOfFiles, List<File> filesToExclude)
            throws IOException {
        List<File> inputFiles = FileUtils.listFilesRecursively(inputDirectory);
        Collections.shuffle(inputFiles);

        List<File> selectedFiles = new ArrayList<File>();
        for (File inputFile : inputFiles) {
            if (selectedFiles.size() == numberOfFiles) {
                continue;
            }
            if (!filesToExclude.contains(inputFile)) {
                selectedFiles.add(inputFile);
            }
        }
        return selectedFiles;
    }

}
