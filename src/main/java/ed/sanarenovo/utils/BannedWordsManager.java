package ed.sanarenovo.utils;

import java.util.ArrayList;
import java.util.List;

public class BannedWordsManager {
    
    private static final List<String> BANNED_WORDS = new ArrayList<>();
    
    static {
        // Initialisation de la liste des mots interdits
        BANNED_WORDS.add("bête");
        BANNED_WORDS.add("idiot");
        BANNED_WORDS.add("stupide");
        BANNED_WORDS.add("nul");
        BANNED_WORDS.add("con");
        BANNED_WORDS.add("merde");
        BANNED_WORDS.add("putain");
        BANNED_WORDS.add("connard");
        BANNED_WORDS.add("connasse");
        BANNED_WORDS.add("salope");
        BANNED_WORDS.add("batard");
        BANNED_WORDS.add("bâtard");
        BANNED_WORDS.add("merdique");
        BANNED_WORDS.add("9atel");
        BANNED_WORDS.add("i8tisab");
        BANNED_WORDS.add("i8tisab");
        BANNED_WORDS.add("israel");
        BANNED_WORDS.add("Israël");
    }

    //Vérifie si un texte contient un mot de la liste.
    public static boolean containsBannedWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase(); // Convertit le texte en minuscules pour que la vérification soit insensible à la casse.
        //Parcourt la liste BANNED_WORDS, et retourne true si au moins un mot est contenu dans le texte.
        return BANNED_WORDS.stream()
                .anyMatch(word -> lowerText.contains(word.toLowerCase()));
    }

    //Méthode pour récupérer la liste complète des mots interdits (copie de sécurité, pour éviter de modifier l’originale).
    public static List<String> getBannedWords() {
        return new ArrayList<>(BANNED_WORDS);
    }

    //Méthode pour ajouter et supprimer un mot interdit dynamiquement.
    public static void addBannedWord(String word) {
        if (word != null && !word.isEmpty() && !BANNED_WORDS.contains(word.toLowerCase())) {
            BANNED_WORDS.add(word.toLowerCase());
        }
    }
    public static void removeBannedWord(String word) {
        if (word != null && !word.isEmpty()) {
            BANNED_WORDS.remove(word.toLowerCase());
        }
    }
} 