from flask import Flask, request, jsonify #  Flask : création de l’app. , gestion des requêtes/JSON.
from flask_cors import CORS # autorise les connexions depuis d’autres applis (comme JavaFX).
from deep_translator import GoogleTranslator # bibliothèque de traduction via Google Translate.

# Création de l’application Flask et activation du CORS (pour permettre les appels depuis Java ou d'autres applications front-end).
app = Flask(__name__)
CORS(app)


# C’est un petit serveur web qui reçoit une requête contenant un ou plusieurs textes et une langue cible, et renvoie les traductions faites via Google.
SUPPORTED_LANGUAGES = ['en', 'fr', 'es', 'de', 'ar', 'zh'] # Liste des langues supportées.
@app.route('/translate', methods=['POST']) # Création d’une route /translate qui accepte la méthode POST.
def translate(): # Lecture du corps JSON envoyé
    data = request.json
    texts = data.get('texts', [])
    target_language = data.get('target_language', 'fr')

    if not texts:
        return jsonify({'error': 'No texts provided'}), 400

    # Vérifie que la langue cible est supportée.
    if target_language not in SUPPORTED_LANGUAGES:
        return jsonify({'error': f'Unsupported target language: {target_language}'}), 400

    translated_texts = []
    for text in texts:
        if text:
            translated_text = GoogleTranslator(source='auto', target=target_language).translate(text) # détecte automatiquement la langue (source='auto'), traduit
            translated_texts.append(translated_text)
        else:
            translated_texts.append('') # ajoute au tableau translated_texts.

    return jsonify({'translated_texts': translated_texts})  #Retourne un JSON avec la traduction.

#Lance le serveur Flask localement, sur le port 5000 par défaut.
if __name__ == '__main__':
    app.run(debug=True)