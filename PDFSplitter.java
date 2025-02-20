import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

public class PDFSplitter {

    public static void splitPDF(String sourceFile, String destinationDir, int pagesPerSplit) throws IOException {
        try (PDDocument document = PDDocument.load(new File(sourceFile))) {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(pagesPerSplit); // Set number of pages per split
            List<PDDocument> pages = splitter.split(document);

            Iterator<PDDocument> iterator = pages.listIterator();
            int fileNumber = 1;
            while (iterator.hasNext()) {
                try (PDDocument pd = iterator.next()) {
                    String newFileName = destinationDir + File.separator + "split_" + fileNumber + ".pdf";
                    pd.save(newFileName);
                }
                fileNumber++;
            }
        }
    }

    public static void main(String[] args) {
        String sourceFile = "matthew.pdf"; // Replace with your PDF file path
        String destinationDir = "C:\\Users\\MAURICIO\\Documents\\commentary"; // Replace with your desired output directory
        int pagesPerSplit = 5; // Number of pages in each split PDF

        try {
            splitPDF(sourceFile, destinationDir, pagesPerSplit);
            System.out.println("PDF split successfully!");
        } catch (IOException e) {
            System.err.println("Error splitting PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}