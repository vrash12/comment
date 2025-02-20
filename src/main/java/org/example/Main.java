package org.example;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void extractSectionsToTextFiles(String sourcePdfPath, String destinationDir) {
        // Map sections to their start pages
        // Make sure this is in the order they appear in the PDF
        Map<String, Integer> sections = new LinkedHashMap<>();
        sections.put("Title Page", 1);
        sections.put("Preface_Joshua_to_Esther", 2);
        sections.put("Joshua", 8);
        sections.put("Introduction", 8);  // Same start as "Joshua" - handle carefully or reorder if needed
        sections.put("Chapter_I", 11);
        sections.put("Chapter_II", 21);
        sections.put("Chapter_III", 30);
        sections.put("Chapter_IV", 39);
        sections.put("Chapter_V", 47);
        sections.put("Chapter_VI", 58);
        sections.put("Chapter_VII", 68);
        sections.put("Chapter_VIII", 83);
        sections.put("Chapter_IX", 94);
        sections.put("Chapter_X", 105);
        sections.put("Chapter_XI", 119);
        sections.put("Chapter_XII", 126);
        sections.put("Chapter_XIII", 130);
        sections.put("Chapter_XIV", 137);
        sections.put("Chapter_XV", 144);
        sections.put("Chapter_XVI", 152);
        sections.put("Chapter_XVII", 153);
        sections.put("Chapter_XVIII", 159);
        sections.put("Chapter_XIX", 165);
        sections.put("Chapter_XX", 171);
        sections.put("Chapter_XXI", 174);
        sections.put("Chapter_XXII", 181);
        sections.put("Chapter_XXIII", 195);
        sections.put("Chapter_XXIV", 203);

        try {
            // Ensure the output directory exists
            File dir = new File(destinationDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Failed to create destination directory: " + destinationDir);
                    return;
                }
            }

            // Open the source PDF
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourcePdfPath));
            int totalPages = pdfDoc.getNumberOfPages();
            System.out.println("Total pages in PDF: " + totalPages);

            String[] sectionNames = sections.keySet().toArray(new String[0]);
            Integer[] sectionStarts = sections.values().toArray(new Integer[0]);

            for (int i = 0; i < sectionNames.length; i++) {
                int startPage = sectionStarts[i];
                int endPage;
                if (i < sectionNames.length - 1) {
                    // End page is one less than the next section's start page
                    endPage = sectionStarts[i + 1] - 1;
                } else {
                    // Last section goes till the end of the PDF
                    endPage = totalPages;
                }

                // Adjust if endPage < startPage in case of duplicate start pages
                if (endPage < startPage) {
                    // If two sections have the same start, one might need manual adjustments.
                    // For now, we will not extract if the range is invalid.
                    System.err.println("Skipping " + sectionNames[i] + " due to invalid page range.");
                    continue;
                }

                if (startPage > totalPages) {
                    System.out.println(sectionNames[i] + " starts beyond the total page count. Skipping.");
                    continue;
                }

                if (endPage > totalPages) {
                    endPage = totalPages; // Ensure we do not exceed the total page count
                }

                // Extract text from the given page range
                StringBuilder textBuilder = new StringBuilder();
                for (int page = startPage; page <= endPage; page++) {
                    String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page));
                    textBuilder.append(pageText).append(System.lineSeparator());
                }

                // Write the extracted text to a .txt file
                File outFile = new File(destinationDir, sectionNames[i] + ".txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                    writer.write(textBuilder.toString());
                    System.out.println("Extracted " + sectionNames[i] + " to " + outFile.getAbsolutePath() +
                            " (Pages " + startPage + "-" + endPage + ")");
                } catch (IOException e) {
                    System.err.println("Error writing file " + outFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            pdfDoc.close();
            System.out.println("Extraction completed!");

        } catch (IOException e) {
            System.err.println("Error processing PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String sourcePdfPath = "C:\\Users\\MAURICIO\\Documents\\SplitPDF\\ma.pdf";
        String destinationDir = "C:\\Users\\MAURICIO\\Documents\\commentary\\joshua";

        extractSectionsToTextFiles(sourcePdfPath, destinationDir);
    }
}
