package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.exciteproject.refext.util.FileUtils;

public class GrobidXmlReferenceLineAnnotator {

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        // load configuration file specifying the retrained SVM models for zone
        // classification

        GrobidXmlReferenceLineAnnotator grobidXmlReferenceLineAnnotator = new GrobidXmlReferenceLineAnnotator();
        for (File inputFile : inputDir.listFiles()) {
            if (!inputFile.getName().endsWith("tei.xml")) {
                continue;
            }
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".csv");
            List<String> references = grobidXmlReferenceLineAnnotator.annotate(inputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            for (String reference : references) {
                bufferedWriter.write(reference);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
    }

    public List<String> annotate(File xmlFile) throws IOException {

        List<String> references = new ArrayList<String>();

        String fileContent = FileUtils.readFile(xmlFile);
        Pattern referenceStringPattern = Pattern.compile("<bibl>((\\R||.)*)</bibl>");

        Matcher matcher = referenceStringPattern.matcher(fileContent);

        while (matcher.find()) {

            String reference = matcher.group(1);
            System.out.println("----");
            System.out.println(reference);
            reference = reference.replaceAll("<lb/>", System.lineSeparator());

            // remove empty lines
            reference = reference.replaceAll("\\R\\s*\\R", System.lineSeparator());

            // remove last line break
            reference = reference.replaceFirst("\\n\\s*$", "");

            // remove leading spaces
            reference = reference.replaceAll("^\\s*", "");
            reference = reference.replaceAll("\\n\\s*", "\n");

            reference = StringEscapeUtils.unescapeXml(reference);

            // TODO put in external class
            reference = "B-REF\t" + reference;
            reference = reference.replaceAll("\\n", "\nI-REF\t");

            System.out.println(reference);
            references.add(reference);
        }
        return references;

    }

}
