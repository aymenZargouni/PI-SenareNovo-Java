# train_model.py

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import make_pipeline
import joblib

# 1. Charger le dataset enrichi
df = pd.read_csv("src/main/python/dataset_clinique_500.csv", encoding="utf-8")

# 2. Préparer la colonne de texte combiné
df["full_input"] = df["etat_patient"] + " âge:" + df["age"].astype(str) + " sexe:" + df["sexe"]

X = df["full_input"]
y = df["service_recommande"]

# 3. Pipeline : vectorisation + modèle
pipeline = make_pipeline(TfidfVectorizer(), KNeighborsClassifier(n_neighbors=3))

# 4. Entraînement
pipeline.fit(X, y)

# 5. Sauvegarde du modèle
joblib.dump(pipeline, "src/main/python/model_knn.pkl")

# 6. Sauvegarde du CSV simplifié pour la recommandation
df[["etat_patient", "age", "sexe", "service_recommande", "conseil", "maladies_possibles"]].to_csv(
    "src/main/python/infos_complementaires.csv", index=False, encoding="utf-8"
)

print(" Modèle et données sauvegardés avec succès.")
