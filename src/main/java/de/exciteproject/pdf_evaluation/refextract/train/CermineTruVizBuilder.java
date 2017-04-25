package de.exciteproject.pdf_evaluation.refextract.train;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.ITextCharacterExtractor;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;

/**
 * Class for building TruViz annotated files. This class prevents the default
 * page limitation of the CERMINE ContentExtractor. See also:
 * https://github.com/CeON/CERMINE/blob/master/TRAINING.md
 */
public class CermineTruVizBuilder {

    public ContentExtractor getContentExtractor() throws AnalysisException {
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        ITextCharacterExtractor iTextCharacterExtractor = new ITextCharacterExtractor();
        // set page limits to override the default limits
        iTextCharacterExtractor.setPagesLimits(-1, -1);

        componentConfiguration.setCharacterExtractor(iTextCharacterExtractor);

        ContentExtractor contentExtractor = new ContentExtractor();
        contentExtractor.setConf(componentConfiguration);
        return contentExtractor;

    }

    /**
     * Builds the TruViz files in inputDirectory TODO: add more parameters
     * 
     * @param inputDirectory
     * @throws TransformationException
     * @throws IOException
     * @throws AnalysisException
     * @throws ParseException
     */
    public void build(File inputDirectory, String outputFormat)
            throws ParseException, AnalysisException, IOException, TransformationException {

        String path = inputDirectory.getAbsolutePath();

        File file = new File(path);
        Collection<File> files = FileUtils.listFiles(file, new String[] { "pdf" }, true);

        int i = 0;
        for (File pdf : files) {

            long start = System.currentTimeMillis();
            float elapsed;

            System.out.println("File processed: " + pdf.getPath());

            try {

                InputStream in = new FileInputStream(pdf);
                ContentExtractor contentExtractor = this.getContentExtractor();
                contentExtractor.setPDF(in);

                if (outputFormat.equals("trueviz")) {
                    // TODO prettier file name creation...
                    File outputFile = new File(pdf.getParentFile().getAbsolutePath() + File.separator
                            + pdf.getName().replaceAll("\\.pdf$", ".cermstr"));
                    if (outputFile.exists()) {
                        continue;
                    }
                    BxDocument doc = contentExtractor.getBxDocumentWithSpecificLabels();
                    BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();

                    Writer fw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");

                    writer.write(fw, Lists.newArrayList(doc), "UTF-8");
                }

                if (outputFormat.equals("text")) {
                    String text = contentExtractor.getRawFullText();
                    // TODO prettier file name creation...
                    File outputFile = new File(pdf.getParentFile().getAbsolutePath() + File.separator
                            + pdf.getName().replaceAll("\\.pdf$", ".cermstr"));
                    FileUtils.writeStringToFile(outputFile, text, "UTF-8");
                }

            } finally {
                long end = System.currentTimeMillis();
                elapsed = (end - start) / 1000F;
            }

            i++;
            int percentage = i * 100 / files.size();
            System.out.println("Extraction time: " + Math.round(elapsed) + "s");
            System.out.println("Progress: " + percentage + "% done (" + i + " out of " + files.size() + ")");
        }
    }

    public static void main(String[] args)
            throws IOException, AnalysisException, ParseException, TransformationException {
        File inputDir = new File(args[0]);
        String outputFormat = args[1];

        CermineTruVizBuilder cermineTruVizBuilder = new CermineTruVizBuilder();

        cermineTruVizBuilder.build(inputDir, outputFormat);

    }

}
