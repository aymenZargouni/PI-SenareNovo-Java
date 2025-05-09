# analyze_dossier.py
import json
import sys
import re
from datetime import datetime

def analyze_imc(imc):
    """Analyze BMI with complete fields"""
    try:
        imc = float(imc)
    except (ValueError, TypeError):
        return {
            "value": 0,
            "category": "Invalid",
            "details": "Valeur IMC invalide",
            "anomaly": True,
            "messages": ["Invalid BMI value"]
        }

    anomalies = []
    category = "Normal"
    details = "Poids normal"

    if imc < 16:
        anomalies.append("Severe underweight")
        category = "Severe underweight"
        details = "Insuffisance pondérale sévère"
    elif 16 <= imc < 18.5:
        anomalies.append("Underweight")
        category = "Underweight"
        details = "Insuffisance pondérale"
    elif 25 <= imc < 30:
        anomalies.append("Overweight")
        category = "Overweight"
        details = "Surpoids"
    elif imc >= 30:
        anomalies.append("Obese")
        category = "Obese"
        details = "Obésité"
    elif imc < 10 or imc > 60:
        anomalies.append("Extreme BMI value - possible measurement error")
        details = "Valeur IMC extrême - erreur possible de mesure"

    return {
        "value": imc,
        "category": category,
        "details": details,
        "anomaly": len(anomalies) > 0,
        "messages": anomalies
    }

def analyze_prescription(ordonnance):
    """Analyze prescription text for medications"""
    if not isinstance(ordonnance, str):
        return {
            "count": 0,
            "details": "Ordonnance invalide",
            "anomaly": True,
            "messages": ["Format d'ordonnance invalide"],
            "medications": []
        }

    # Nouveaux motifs pour détection des médicaments
    patterns = [
        r'\b([A-Z][a-z]+)\s*(\d+\s*(mg|g|ml|µg))\b',  # Doliprane 500mg
        r'\b([A-Z]{3,})\s*(\d+)\b',  # ASPIRINE 500
        r'\b([A-Z][a-z]+)\b',        # Amoxicilline (sans dosage)
        r'\b(\w+)\s*(comprimé|gelule|sirop)\b'  # Paracetamol comprimé
    ]

    medications = []
    for pattern in patterns:
        matches = re.finditer(pattern, ordonnance, re.IGNORECASE)
        for match in matches:
            med_name = match.group(1).strip()
            dosage = match.group(2).strip() if len(match.groups()) > 1 else "dosage non spécifié"
            medications.append({
                "name": med_name,
                "dosage": dosage,
                "found_as": match.group(0)
            })

    anomalies = []
    details = "Ordonnance normale"

    if not medications:
        anomalies.append("Aucun médicament détecté")
        details = "Aucun médicament identifié"
    elif len(medications) > 5:
        anomalies.append("Nombre élevé de médicaments")
        details = f"{len(medications)} médicaments prescrits"

    return {
        "count": len(medications),
        "details": details,
        "anomaly": bool(anomalies),
        "messages": anomalies,
        "medications": medications
    }

def analyze_consultation_frequency(dates):
    """Analyze consultation frequency patterns"""
    if not isinstance(dates, list):
        return {
            "count": 0,
            "avg_gap_days": 0,
            "details": "Format de dates invalide",
            "anomaly": True,
            "messages": ["Invalid dates format"]
        }

    if len(dates) < 2:
        return {
            "count": len(dates),
            "avg_gap_days": 0,
            "details": "Pas assez de consultations pour analyse",
            "anomaly": False,
            "messages": []
        }

    try:
        date_objs = []
        for d in dates:
            try:
                date_objs.append(datetime.strptime(str(d), "%Y-%m-%d"))
            except ValueError:
                continue

        if len(date_objs) < 2:
            return {
                "count": len(date_objs),
                "avg_gap_days": 0,
                "details": "Dates non analysables",
                "anomaly": False,
                "messages": []
            }

        date_objs.sort()
        gaps = [(date_objs[i+1] - date_objs[i]).days for i in range(len(date_objs)-1)]

        avg_gap = sum(gaps) / len(gaps)
        anomalies = []
        details = f"Fréquence normale (moyenne: {avg_gap:.1f} jours)"

        if avg_gap < 7:
            anomalies.append(f"High consultation frequency: average {avg_gap:.1f} days between visits")
            details = f"Fréquence élevée (moyenne: {avg_gap:.1f} jours)"
        elif avg_gap > 90:
            anomalies.append(f"Long gaps between consultations: average {avg_gap:.1f} days")
            details = f"Longs intervalles (moyenne: {avg_gap:.1f} jours)"

        return {
            "count": len(date_objs),
            "avg_gap_days": avg_gap,
            "details": details,
            "anomaly": len(anomalies) > 0,
            "messages": anomalies
        }
    except Exception as e:
        return {
            "count": 0,
            "avg_gap_days": 0,
            "details": f"Erreur d'analyse: {str(e)}",
            "anomaly": True,
            "messages": ["Erreur de traitement des dates"]
        }

def analyze_text(text):
    """Analyze free text observations"""
    if not isinstance(text, str):
        return {
            "length": 0,
            "details": "Texte invalide",
            "anomaly": True,
            "messages": ["Invalid text format"]
        }

    text = text.strip()
    if not text:
        return {
            "length": 0,
            "details": "Aucune observation",
            "anomaly": False,
            "messages": []
        }

    details = "Observations normales"
    anomalies = []

    if len(text) > 500:
        anomalies.append("Unusually long observations text")
        details = "Observations très longues"
    elif len(text) < 10:
        anomalies.append("Very short observations text")
        details = "Observations très courtes"

    return {
        "length": len(text),
        "details": details,
        "anomaly": len(anomalies) > 0,
        "messages": anomalies
    }

def generate_summary(analyses):
    """Generate a comprehensive summary"""
    parts = []

    imc = analyses.get('imc', {})
    parts.append(f"IMC: {imc.get('value', 'N/A')} ({imc.get('category', 'Inconnu')})")

    prescription = analyses.get('prescription', {})
    parts.append(f"Médicaments: {prescription.get('count', 0)}")

    consultations = analyses.get('consultations', {})
    if consultations.get('count', 0) > 1:
        parts.append(f"Fréquence consultations: {consultations.get('avg_gap_days', 0):.1f} jours")

    return ", ".join(parts) if parts else "Aucune information disponible"

def main():
    try:
        # Read input from stdin
        input_data = json.load(sys.stdin)

        # Ensure all expected fields exist in input
        input_data.setdefault('imc', 0)
        input_data.setdefault('ordonnance', '')
        input_data.setdefault('consult_dates', [])
        input_data.setdefault('observations', '')

        # Perform analyses
        analyses = {
            "imc": analyze_imc(input_data['imc']),
            "prescription": analyze_prescription(input_data['ordonnance']),
            "consultations": analyze_consultation_frequency(input_data['consult_dates']),
            "observations": analyze_text(input_data['observations'])
        }

        # Build complete response with all fields
        response = {
            "success": True,
            "analyses": analyses,
            "consult_type": "Général",
            "summary": generate_summary(analyses)
        }

        print(json.dumps(response, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": str(e),
            "analyses": {
                "imc": {"value": 0, "category": "Error", "details": "Erreur", "anomaly": False, "messages": []},
                "prescription": {"count": 0, "details": "Erreur", "anomaly": False, "messages": [], "medications": []},
                "consultations": {"count": 0, "avg_gap_days": 0, "details": "Erreur", "anomaly": False, "messages": []},
                "observations": {"length": 0, "details": "Erreur", "anomaly": False, "messages": []}
            },
            "consult_type": "Erreur",
            "summary": "Erreur d'analyse"
        }))

if __name__ == "__main__":
    main()