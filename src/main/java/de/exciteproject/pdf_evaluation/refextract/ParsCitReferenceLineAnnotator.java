package de.exciteproject.pdf_evaluation.refextract;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import pl.edu.icm.cermine.exception.AnalysisException;

public class ParsCitReferenceLineAnnotator extends ReferenceLineAnnotator {

    public static void main(String[] args) throws AnalysisException, IOException {
        File citeExtractFile = new File(args[0]);
        File inputTxtFile = new File(args[1]);
        ParsCitReferenceLineAnnotator parsCitReferenceLineAnnotator = new ParsCitReferenceLineAnnotator(
                citeExtractFile);
        parsCitReferenceLineAnnotator.annotateReferenceLinesFromPDF(inputTxtFile);

    }

    protected File citeExtractFile;

    public ParsCitReferenceLineAnnotator(File citeExtractFile) {
        this.citeExtractFile = citeExtractFile;
    }

    /**
     * Actually the input is not a PDF but a text file...
     */
    @Override
    public List<String> annotateReferenceLinesFromPDF(File pdfFile) throws IOException {

        List<String> references = new ArrayList<String>();
        ProcessBuilder builder = new ProcessBuilder(this.citeExtractFile.getAbsolutePath(), "-m", "extract_citations",
                pdfFile.getAbsolutePath());
        builder.directory(new File("/"));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String startTag = "<rawString>";
        String endTag = "</rawString>";
        String line;

        while ((line = r.readLine()) != null) {
            if (line.startsWith(startTag) && line.endsWith(endTag)) {
                String referenceString = line.replaceFirst("^" + startTag, "").replaceFirst(endTag + "$", "");
                referenceString = StringEscapeUtils.unescapeXml(referenceString);
                String[] referenceLines = referenceString.split("\\[LINEBREAK\\]");
                for (int i = 0; i < referenceLines.length; i++) {
                    if (i == 0) {
                        references.add("B-REF\t" + referenceLines[i]);
                    } else {
                        references.add("I-REF\t" + referenceLines[i]);
                    }
                }
            }

        }

        return references;
    }

    @Override
    public void initializeModels(File trainingModelsDirectory) throws IOException {
    }

}
