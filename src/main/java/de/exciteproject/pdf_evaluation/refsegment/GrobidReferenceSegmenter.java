package de.exciteproject.pdf_evaluation.refsegment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Person;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;

import junit.framework.Test;

public class GrobidReferenceSegmenter  {

    public static void main(String[] args) throws Exception {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File grobidHomeDir = new File(args[2]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        GrobidReferenceSegmenter grobidReferenceSegmenter = new GrobidReferenceSegmenter(grobidHomeDir);
        for (File inputFile : inputDir.listFiles()) {
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".bib");

            try {
                List<String> references = grobidReferenceSegmenter.segmentReferenceFromRawLines(inputFile);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                for (String reference : references) {
                    bufferedWriter.write(reference);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } catch (StackOverflowError e) {
                System.err.println("stackoverflow at file: " + inputFile.getAbsolutePath());
            }
        }
    }

    protected File grobidHomeDir;

    public GrobidReferenceSegmenter(File grobidHomeDir) {
        this.grobidHomeDir = grobidHomeDir;
        File grobidPropertiesFile = new File(
                this.grobidHomeDir + File.separator + "config" + File.separator + "grobid.properties");
        try {
            MockContext.setInitialContext(this.grobidHomeDir.getAbsolutePath(), grobidPropertiesFile.getAbsolutePath());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }

    public List<String> segmentReferenceFromRawLines(File refFile) throws Exception {
        List<String> references = new ArrayList<String>();

        List<String> referenceStrings = Files.readAllLines(refFile.toPath(), Charset.defaultCharset());
            Engine engine = GrobidFactory.getInstance().getEngine();

             List<BiblioItem> segmentedStrings=engine.processRawReferences(referenceStrings, false);
             int i=1;
             for(BiblioItem item:segmentedStrings){
                 references.add(item.toBibTeX("ref-"+i));
                 //references.add(item.toTEI(i));
                 i++;
             }
        return references;
    }
}
