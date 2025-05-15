# 🏥 SanareNovo - Application de Gestion de Clinique (JavaFX)

## 📖 Description du Projet

SanareNovo est une application de bureau développée avec JavaFX pour faciliter la gestion des cliniques et établissements médicaux.  
Ce projet a été réalisé dans le cadre du cours **PIDEV** à **Esprit School of Engineering**. L’application vise à désengorger les hôpitaux tunisiens en offrant une plateforme numérique  
de **consultation à distance**, **gestion éficaces des salles, services, blogs, recrutements** et **communication efficace entre patients et médecins**.

### Fonctionnalités principales :

- 🧑‍⚕️ Gestion multi-utilisateurs (Patient, Médecin, Administrateur, RH, Technicien, Coordinateur)
- 📅 Prise de rendez-vous et consultations en ligne
- 📁 Suivi des dossiers médicaux
- 🏥 Gestion des services, salles et équipements
- 📰 Blog médical intégré
- 💼 Gestion des candidatures et recrutements
  
---

## 📑 Table des Matières

- [📖 Description du Projet](#-description-du-projet)
- [🧰 Technologies Utilisées](#-technologies-utilisées)
- [📁 Structure du Répertoire](#-structure-du-répertoire)
- [📦 Installation](#-installation)
- [⚙️ Utilisation](#️-utilisation)
- [🔗 Connexion à la Base de Données](#-connexion-à-la-base-de-données)
- [👥 Contribution](#-contribution)
- [🛡️ Licence](#️-licence)
- [🙏 Remerciements](#-remerciements)
- [🏷️ Topics GitHub](#-topics-github)

---

## 🧰 Technologies Utilisées

- **Langage :** Java 17+
- **Framework UI :** JavaFX
- **Librairie caméra :** OpenCV
- **Email :** Jakarta Mail
- **Gestion de sessions :** `UserSession`
- **Base de données :** MySQL (partagée avec l'application Symfony)
- **Outil de build :** Maven / Gradle
- **IDE :** IntelliJ IDEA

---

## 📁 Structure du Répertoire

src/
├── controllers/
├── models/
├── views/
├── services/
├── utils/
├── Main.java
resources/
├── fxml/
├── images/
├── styles/

---

## 📦 Installation

1. Cloner le projet depuis GitHub :
   ```bash
   git clone https://github.com/aymenZargouni/PI-SenareNovo-Java.git
   cd sanarenovo-javafx
Configurer le projet dans l'IDE (IntelliJ ou Eclipse)

Vérifier les dépendances JavaFX
2.Ajouter les chemins vers JavaFX dans la configuration de votre projet.

3.Configurer la base de données dans le fichier de configuration

4.Lancer le projet

## ⚙️ Utilisation

Se connecter avec un compte patient ou médecin

Réserver une consultation via l’interface

Consulter ou mettre à jour les informations médicales

Recevoir des notifications par mail

Sécurité : prise automatique d’une photo après 3 tentatives de connexion échouées

## 🔗 Connexion à la Base de Données
Cette application partage une base de données relationnelle avec un second projet web développé sous Symfony.
Vérifiez que le serveur de base de données est actif et que la configuration est commune entre les deux projets.

## 👥 Contribution
Membres de l’équipe :
Aymen Zargouni – Gestion des utilisateurs

Hichem Mhadhbi – Gestion des blogs & commentaires

Mahdy Chaouechi – Gestion des services & salles

Youssef Tarhouni – Gestion des consultations

Sabrine Zaddem – Gestion des équipements & réclamations

Takoua Hichri – Gestion des recrutements

Vous êtes les bienvenus pour proposer des suggestions ou signaler des bugs via les issues GitHub.

## 🛡️ Licence
Ce projet est distribué sous la licence MIT.
Consultez le fichier LICENSE pour plus d’informations.

## 🙏 Remerciements
Ce projet a été réalisé sous la supervision de **Karray Gargouri**
à Esprit School of Engineering, dans le cadre du module PIDEV 3A.
Merci à toute l’équipe pédagogique pour leur accompagnement.

## 🏷️ Topics GitHub
javafx gestion-clinique opencv jakarta-mail user-session pidev esprit-school-of-engineering application-bureau desktop-app
