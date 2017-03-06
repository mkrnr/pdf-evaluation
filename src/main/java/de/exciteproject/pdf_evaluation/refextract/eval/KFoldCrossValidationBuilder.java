package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.common.io.Files;

/**
 * Class for generating datasets for k-fold cross validation.
 */
public class KFoldCrossValidationBuilder {

    public static void main(String[] args) throws IOException {
        int k = Integer.parseInt(args[0]);
        File inputDirectory = new File(args[1]);
        File outputDirectory = new File(args[2]);

        KFoldCrossValidationBuilder kFoldCrossValidationBuilder = new KFoldCrossValidationBuilder();
        kFoldCrossValidationBuilder.buildDatasets(k, inputDirectory, outputDirectory);
    }

    public void buildDatasets(int k, File inputListFile, File outputDirectory) throws IOException {
        List<File> inputFiles = this.readFileListFromFile(inputListFile);

        // initialize testingFilesPerFold
        List<List<File>> testingFilesPerFold = new ArrayList<List<File>>();
        for (int i = 0; i < k; i++) {
            testingFilesPerFold.add(new ArrayList<File>());
        }

        // add files to testFilesPerFold iteratively
        List<File> inputFilesCopy = new ArrayList<File>(inputFiles);
        while (inputFilesCopy.size() > 0) {
            for (int i = 0; i < k; i++) {
                if (inputFilesCopy.size() < 1) {
                    break;
                }
                File currentTestFile = inputFilesCopy.remove(0);
                testingFilesPerFold.get(i).add(currentTestFile);
            }
        }

        // build trainingFilesPerFold from the remaining files which are not in
        // the individual test set
        List<List<File>> trainingFilesPerFold = new ArrayList<List<File>>();
        for (int i = 0; i < k; i++) {
            Set<File> allFilesSet = new HashSet<File>(inputFiles);
            allFilesSet.removeAll(testingFilesPerFold.get(i));
            trainingFilesPerFold.add(new ArrayList<File>(allFilesSet));
        }

        // write files per fold into separate directories
        for (int i = 0; i < k; i++) {
            File foldDirectory = new File(outputDirectory + File.separator + "fold-" + i);
            File trainingDirectory = new File(foldDirectory + File.separator + "train");
            trainingDirectory.mkdirs();
            File testingDirectory = new File(foldDirectory + File.separator + "test");
            testingDirectory.mkdirs();
            for (File trainingFile : trainingFilesPerFold.get(i)) {
                this.copyFile(trainingFile, trainingDirectory);
            }
            for (File testingFile : testingFilesPerFold.get(i)) {
                this.copyFile(testingFile, testingDirectory);
            }
        }
    }

    private void copyFile(File sourceFile, File targetDirectory) throws IOException {
        File targetFile = new File(targetDirectory.getAbsolutePath() + File.separator + sourceFile.getName());
        Files.copy(sourceFile, targetFile);
    }

    private List<File> readFileListFromFile(File listFile) {
        // taken from: http://stackoverflow.com/a/5343727
        Scanner s;
        ArrayList<File> list = new ArrayList<File>();
        try {
            s = new Scanner(listFile);
            while (s.hasNextLine()) {
                list.add(new File(s.nextLine()));
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

}
