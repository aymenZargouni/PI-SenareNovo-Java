import sys
from gtts import gTTS
import pygame
import os

class TextToSpeech:
    def __init__(self, lang="fr"):
        self.lang = lang  # Langue : Français

    def speak(self, text):
        if not text.strip():
            print("❌ Aucun texte à lire !")
            return

        try:
            filename = "speech.mp3"
            tts = gTTS(text=text, lang=self.lang)
            tts.save(filename)

            # Lire l'audio
            pygame.mixer.init()
            pygame.mixer.music.load(filename)
            pygame.mixer.music.play()

            # Attendre la fin de la lecture
            while pygame.mixer.music.get_busy():
                continue

            # Supprimer le fichier audio après la lecture
            os.remove(filename)
        except Exception as e:
            print(f"⚠️ Erreur lors de la lecture : {e}")

# 🔥 Exécution depuis Java (avec argument)
if __name__ == "__main__":
    if len(sys.argv) > 1:
        text = " ".join(sys.argv[1:])  # Récupérer le texte depuis Java
        tts = TextToSpeech(lang="fr")
        tts.speak(text)
    else:
        print("⚠️ Aucun texte fourni !")
