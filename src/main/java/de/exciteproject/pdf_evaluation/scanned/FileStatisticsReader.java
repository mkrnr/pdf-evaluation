package de.exciteproject.pdf_evaluation.scanned;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileStatisticsReader {

    public static void main(String[] args) throws IOException {

        File inputDir = new File(args[0]);
        File statisticsOutputFile = new File(args[1]);
        Set<File> inputFiles = new HashSet<File>();
        inputFiles.addAll(Arrays.asList(inputDir.listFiles()));
        for (int i = 1; i <= inputFiles.size(); i++) {
            File inputFile = new File(inputDir + File.separator + i + ".pdf");
            // String text = FileUtils.readFile(inputFile);
            // System.out.println(text);
            // System.out.println(text.split("\\n").length);
            double bytes = inputFile.length();
            double kilobytes = (bytes / 1024);
            System.out.println((int) kilobytes);
        }
    }

}
