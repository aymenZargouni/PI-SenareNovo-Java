package ed.sanarenovo.utils.cvanalysis;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CVAnalyzer {
    private static final String TESSDATA_PATH = "chemin/vers/tessdata"; // À adapter

    public Map<String, Object> analyzeCV(File cvFile) {
        Map<String, Object> analysisResults = new HashMap<>();

        try {
            // 1. Extraction du texte brut
            String textContent = extractTextFromPDF(cvFile);
            analysisResults.put("raw_text", textContent);

            // 2. Analyse avec OCR si nécessaire (pour les CV scannés)
            if (textContent.trim().isEmpty() || isLikelyImagePDF(cvFile)) {
                textContent = extractTextWithOCR(cvFile);
                analysisResults.put("ocr_text", textContent);
            }

            // 3. Analyse des sections clés
            analyzeSections(textContent, analysisResults);

            // 4. Score global
            analysisResults.put("score", calculateCVScore(analysisResults));

        } catch (Exception e) {
            analysisResults.put("error", "Erreur d'analyse: " + e.getMessage());
        }

        return analysisResults;
    }

    private String extractTextFromPDF(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private boolean isLikelyImagePDF(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0);
            // Simple vérification - si très peu de texte sur la première page
            String text = new PDFTextStripper().getText(document);
            return text.trim().length() < 100;
        }
    }

    private String extractTextWithOCR(File pdfFile) throws TesseractException, IOException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage("fra+eng"); // Français et anglais

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder textBuilder = new StringBuilder();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300); // Haute résolution
                String pageText = tesseract.doOCR(image);
                textBuilder.append(pageText).append("\n\n");
            }

            return textBuilder.toString();
        }
    }

    private void analyzeSections(String textContent, Map<String, Object> results) {
        // Détection des sections clés - conversion explicite en boolean
        results.put("has_education", containsSection(textContent, "FORMATION|ÉDUCATION|EDUCATION"));
        results.put("has_experience", containsSection(textContent, "EXPÉRIENCE|EXPERIENCE"));
        results.put("has_skills", containsSection(textContent, "COMPÉTENCES|COMPETENCES|SKILLS"));

        // Détection des mots-clés importants
        int keywordsCount = countKeywords(textContent,
                "Java|Python|Spring|Hibernate|Angular|React|SQL|NoSQL|Git|Agile|Scrum");
        results.put("technical_keywords", keywordsCount);

        // Détection des diplômes
        results.put("degrees", extractDegrees(textContent));

        // Expérience (en années approximatives)
        results.put("experience_years", estimateExperience(textContent));
    }
    private boolean containsSection(String text, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private int countKeywords(String text, String keywordsRegex) {
        Pattern pattern = Pattern.compile(keywordsRegex, Pattern.CASE_INSENSITIVE);
        return (int) pattern.matcher(text).results().count();
    }

    private String extractDegrees(String text) {
        Pattern pattern = Pattern.compile(
                "(Bac\\+\\d|Licence|Master|Doctorat|PhD|Ingénieur|Bachelor|MSc|DEA|DESS)",
                Pattern.CASE_INSENSITIVE);
        return String.join(", ", pattern.matcher(text).results()
                .map(match -> match.group())
                .distinct()
                .toArray(String[]::new));
    }

    private int estimateExperience(String text) {
        // Recherche de durées dans les expériences professionnelles
        Pattern pattern = Pattern.compile(
                "(\\d+)\\s*(an|année|ans|year|years)",
                Pattern.CASE_INSENSITIVE);

        return pattern.matcher(text).results()
                .mapToInt(match -> Integer.parseInt(match.group(1)))
                .sum();
    }

    private int calculateCVScore(Map<String, Object> analysis) {
        int score = 0;

        // Points pour les sections présentes
        if ((boolean) analysis.getOrDefault("has_education", false)) score += 20;
        if ((boolean) analysis.getOrDefault("has_experience", false)) score += 30;
        if ((boolean) analysis.getOrDefault("has_skills", false)) score += 10;

        // Points pour les mots-clés techniques
        score += Math.min(30, (int) analysis.getOrDefault("technical_keywords", 0) * 2);

        // Points pour l'expérience (max 20 points)
        int expYears = (int) analysis.getOrDefault("experience_years", 0);
        score += Math.min(20, expYears * 4);

        return score;
    }
}