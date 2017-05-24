package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.common.io.Files;

/**
 * Class for generating datasets for k-fold cross validation.
 */
public class KFoldDataset {

    public static void main(String[] args) throws IOException {
        int k = Integer.parseInt(args[0]);
        File idFile = new File(args[1]);
        File inputDirectory = new File(args[2]);
        File outputDirectory = new File(args[3]);

        KFoldDataset kFoldDataset = new KFoldDataset(k);
        kFoldDataset.build(idFile, inputDirectory);
        kFoldDataset.writeFolds(outputDirectory);
    }

    private int k;
    private ArrayList<List<File>> testingFolds;
    private ArrayList<List<File>> trainingFolds;

    public KFoldDataset(int k) {
        this.k = k;
    }

    public void build(File idFile, File inputDirectory) throws IOException {
        List<String> ids = this.readFileListFromFile(idFile);

        List<File> inputFiles = Arrays.asList(inputDirectory.listFiles());
        System.out.println("inputFiles: " + inputFiles);

        // initialize testingFilesPerFold
        this.testingFolds = new ArrayList<List<File>>();
        for (int i = 0; i < this.k; i++) {
            this.testingFolds.add(new ArrayList<File>());
        }

        // add files to testFilesPerFold iteratively
        List<String> idsCopy = new ArrayList<String>(ids);
        while (idsCopy.size() > 0) {
            for (int i = 0; i < this.k; i++) {
                if (idsCopy.size() < 1) {
                    break;
                }
                String currentId = idsCopy.remove(0);
                System.out.println(currentId);
                this.testingFolds.get(i).add(this.getFile(currentId, inputFiles));
            }
        }

        // build trainingFilesPerFold from the remaining files which are not in
        // the individual test set
        this.trainingFolds = new ArrayList<List<File>>();
        for (int i = 0; i < this.k; i++) {
            List<File> inputFilesCopy = new ArrayList<File>(inputFiles);
            System.out.println("------");
            System.out.println(this.testingFolds.get(i));
            System.out.println(inputFilesCopy);
            System.out.println(inputFilesCopy.size());
            inputFilesCopy.removeAll(this.testingFolds.get(i));
            System.out.println(inputFilesCopy.size());
            System.out.println("------");
            this.trainingFolds.add(inputFilesCopy);
        }
    }

    public List<File> getTestingFold(int k) {
        return this.testingFolds.get(k);
    }

    public List<File> getTrainingFold(int k) {
        return this.trainingFolds.get(k);
    }

    public void writeFolds(File outputDirectory) throws IOException {

        // write files per fold into separate directories
        for (int i = 0; i < this.k; i++) {

            File foldDirectory = new File(outputDirectory + File.separator + "fold-" + i);

            File trainingDirectory = new File(foldDirectory + File.separator + "train");
            trainingDirectory.mkdirs();
            File testingDirectory = new File(foldDirectory + File.separator + "test");
            testingDirectory.mkdirs();

            for (File trainingFile : this.trainingFolds.get(i)) {
                this.copyFile(trainingFile, trainingDirectory);
            }
            for (File testingFile : this.testingFolds.get(i)) {
                this.copyFile(testingFile, testingDirectory);
            }
        }
    }

    private void copyFile(File sourceFile, File targetDirectory) throws IOException {
        File targetFile = new File(targetDirectory.getAbsolutePath() + File.separator + sourceFile.getName());
        Files.copy(sourceFile, targetFile);
    }

    private File getFile(String id, List<File> files) {
        for (File file : files) {
            if (id.equals((file.getName().split("\\.")[0]))) {
                return file;
            }
        }
        return null;
    }

    private List<String> readFileListFromFile(File listFile) {
        // taken from: http://stackoverflow.com/a/5343727
        Scanner s;
        ArrayList<String> list = new ArrayList<String>();
        try {
            s = new Scanner(listFile);
            while (s.hasNextLine()) {
                list.add(s.nextLine());
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

}
