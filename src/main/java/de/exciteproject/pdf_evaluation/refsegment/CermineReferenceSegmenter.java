package de.exciteproject.pdf_evaluation.refsegment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import pl.edu.icm.cermine.bibref.BibReferenceParser;
import pl.edu.icm.cermine.bibref.CRFBibReferenceParser;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;

public class CermineReferenceSegmenter {
    private BibReferenceParser<BibEntry> bibReferenceParser;
    public static void main(String[] args) throws IOException, AnalysisException {
        File inputFile=new File(args[0]);
        File outputFile=new File(args[1]);
        CermineReferenceSegmenter cermineReferenceSegmenter=new CermineReferenceSegmenter();
        List<String> referenceStrings = Files.readAllLines(inputFile.toPath(), Charset.defaultCharset());
        cermineReferenceSegmenter.extractBibEntriesFromReferences(referenceStrings);


    }
    public CermineReferenceSegmenter() throws AnalysisException {
        this.bibReferenceParser=CRFBibReferenceParser.getInstance();;
    }

    public List<BibEntry>  extractBibEntriesFromReferences(List<String> referenceStrings)
        throws IOException, AnalysisException {
           List<BibEntry> bibEntries = new ArrayList<BibEntry>();
    
           for (String referenceString : referenceStrings) {
               BibEntry bibEntry=this.bibReferenceParser.parseBibReference(referenceString);
               System.out.println(bibEntry.toBibTeX());
               System.out.println();
           }
           return bibEntries;
     }

}
