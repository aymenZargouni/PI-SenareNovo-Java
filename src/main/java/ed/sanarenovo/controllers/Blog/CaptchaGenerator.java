// Gère la mise en cache des données pour améliorer les performances.
package ed.sanarenovo.controllers.Blog;

import java.util.Random;

public class CaptchaGenerator {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final Random random = new Random();

    public static String generateCaptcha(int length) {
// Crée un StringBuilder avec la capacité initiale égale à la longueur demandée (optimisation mémoire)
        StringBuilder sb = new StringBuilder(length);

        // Boucle pour générer chaque caractère du CAPTCHA
        for (int i = 0; i < length; i++) {
            // Génère un index aléatoire dans la plage des caractères disponibles
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());

            // Récupère le caractère correspondant à l'index généré aléatoirement
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            // Ajoute le caractère aléatoire au StringBuilder
            sb.append(rndChar);
        }

        // Retourne la chaîne de caractères générée (le CAPTCHA)
        return sb.toString();
    }
}