import sys
import joblib
import pandas as pd

# Vérification des arguments
if len(sys.argv) != 2:
    print("❌ Format attendu : symptôme|âge|sexe")
    sys.exit(1)

try:
    symptome, age, sexe = sys.argv[1].split("|")
except ValueError:
    print("❌ Entrée invalide. Format attendu : symptôme|âge|sexe")
    sys.exit(1)

# Chargement du pipeline
pipeline = joblib.load("src/main/python/model_knn.pkl")
df = pd.read_csv("src/main/python/infos_complementaires.csv", encoding="utf-8")

# Préparation de l'entrée
input_text = f"{symptome.strip()} âge:{age.strip()} sexe:{sexe.strip()}"
prediction = pipeline.predict([input_text])[0]

# Recherche exacte
filtre = df[
    (df["service_recommande"] == prediction) &
    (df["etat_patient"].str.lower() == symptome.strip().lower()) &
    (df["age"] == int(age.strip())) &
    (df["sexe"].str.upper() == sexe.strip().upper())
    ]

if not filtre.empty:
    ligne = filtre.sample(1).iloc[0]
else:
    # Fallback par service uniquement
    filtre_service = df[df["service_recommande"] == prediction]
    if not filtre_service.empty:
        ligne = filtre_service.sample(1).iloc[0]
    else:
        # Dernier recours
        print(f"{prediction}|Consultez un professionnel de santé.|Non spécifiées")
        sys.exit(0)

# Récupération des données
conseil = ligne["conseil"]
maladies = ligne["maladies_possibles"]

# Affichage au format attendu par JavaFX
print(f"{prediction}|{conseil}|{maladies}", flush=True)
