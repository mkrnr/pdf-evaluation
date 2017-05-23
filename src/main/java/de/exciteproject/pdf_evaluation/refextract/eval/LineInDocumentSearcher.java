package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class LineInDocumentSearcher {

    public static void main(String[] args) throws IOException {

        File xmlAnnotatedDir = new File(args[0]);

        File layoutDirectory = new File(args[1]);

        List<String> stringsToRemove = new ArrayList<String>();
        stringsToRemove.add("<ref>");
        stringsToRemove.add("</ref>");
        stringsToRemove.add("<oth>");
        stringsToRemove.add("</oth>");
        for (File xmlAnnotatedFile : xmlAnnotatedDir.listFiles()) {
            File layoutFile = new File(layoutDirectory.getAbsolutePath() + File.separator
                    + FilenameUtils.removeExtension(xmlAnnotatedFile.getName()) + ".csv");
            LineInDocumentSearcher.search(xmlAnnotatedFile, layoutFile, stringsToRemove);
        }
    }

    public static void search(File xmlAnnotatedFile, File layoutFile, List<String> stringsToRemove) throws IOException {
        System.out.println("========" + xmlAnnotatedFile.getAbsolutePath() + "=========");

        Charset charset = Charset.defaultCharset();
        List<String> linesToSearch = Files.readAllLines(xmlAnnotatedFile.toPath(), charset);
        List<String> referenceLines = new ArrayList<String>();
        boolean refOpen = false;
        for (String lineToSearch : linesToSearch) {
            if (lineToSearch.startsWith("<ref>")) {
                refOpen = true;
            }
            if (refOpen) {
                String currentLine = lineToSearch;
                // System.out.println(currentLine);
                for (String stringToRemove : stringsToRemove) {
                    currentLine = currentLine.replaceAll(stringToRemove, "");
                }

                // System.out.println(currentLine);
                referenceLines.add(currentLine);
            }
            if (lineToSearch.endsWith("</ref>")) {
                refOpen = false;
            }
        }

        List<String> layoutLines = Files.readAllLines(layoutFile.toPath(), charset);
        Set<String> allLines = new HashSet<String>();
        // remove layout information
        for (String layoutLine : layoutLines) {
            allLines.add(layoutLine.split("\\t")[0]);
        }

        // search
        for (String referenceLine : referenceLines) {
            if (!allLines.contains(referenceLine)) {
                System.out.println(referenceLine);
            }
        }
    }

}
