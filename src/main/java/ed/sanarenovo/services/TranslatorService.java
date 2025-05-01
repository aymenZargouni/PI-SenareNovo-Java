package ed.sanarenovo.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class TranslatorService {

    // Cette méthode envoie un texte à un service Flask, récupère la traduction, et la retourne. Elle isole tout le code réseau/API pour simplifier l’usage dans le reste du programme.
    public static String translate(String text, String targetLang) { // targetLang la ligne cible ex:'fr'
        try {
            URL url = new URL("http://localhost:5000/translate"); // Création d’une connexion HTTP POST vers le serveur Flask local sur /translate.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //Configuration de la requête : Type : POST , Contenu : JSON , On enverra des données dans le corps de la requête.
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            //Construction du corps JSON :
            JSONObject json = new JSONObject();
            JSONArray texts = new JSONArray();
            texts.put(text);
            json.put("texts", texts);
            json.put("target_language", targetLang);

            // Envoi des données JSON dans la requête POST.
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            //Lecture de la réponse du serveur avec un flux de caractères.
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            //On lit toute la réponse ligne par ligne, et on construit la chaîne complète.
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // On extrait la première chaîne traduite du tableau "translated_texts" et on la retourne.
            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson.getJSONArray("translated_texts").getString(0);

        } catch (Exception e) {
            e.printStackTrace();
            return text; // retourne le texte original en cas d'erreur
        }
    }
}