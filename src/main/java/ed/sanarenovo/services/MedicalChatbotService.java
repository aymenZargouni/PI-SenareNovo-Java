package ed.sanarenovo.services;
import java.util.HashMap;
import java.util.Map;

public class MedicalChatbotService {
    private static final Map<String, String> faqMap = new HashMap<>();

    static {
        faqMap.put("Quelle température est considérée comme indicatrice d’une fièvre? Quand consulter?",
                "Une fièvre est souvent définie par une température supérieure à 38°C. Consultez si elle persiste plus de 48h ou dépasse 39°C.");
        faqMap.put("Combien de temps dure un rhume? Une grippe?",
                "Un rhume dure en moyenne 5 à 7 jours. Une grippe peut durer de 7 à 10 jours.");
        faqMap.put("Comment soigner une gastro-entérite?",
                "Reposez-vous, hydratez-vous beaucoup et suivez une alimentation légère. Consultez en cas de symptômes graves.");
        faqMap.put("Le zona est-il contagieux?",
                "Le zona lui-même n'est pas contagieux, mais le virus peut transmettre la varicelle à une personne non immunisée.");
        faqMap.put("Qu’est-ce qui cause les pierres aux reins?",
                "Les calculs rénaux se forment à partir de sels minéraux et de déchets chimiques concentrés dans l'urine.");
        faqMap.put("Comment soigner une sinusite?",
                "Utilisez des décongestionnants, buvez beaucoup d'eau, reposez-vous. Consultez si la douleur persiste.");
        faqMap.put("Comment guérir d’une infection urinaire?",
                "Buvez beaucoup d'eau et consultez un médecin pour obtenir un traitement antibiotique.");
        faqMap.put("Qu’est-ce que le lupus?",
                "Le lupus est une maladie auto-immune chronique où le système immunitaire attaque ses propres tissus.");
        faqMap.put("Que faire en cas de migraine?",
                "Reposez-vous dans un endroit sombre et calme, utilisez des analgésiques et consultez si les migraines sont fréquentes.");
        faqMap.put("Comment arrêter une toux?",
                "Hydratez-vous, utilisez des pastilles pour la gorge, et si la toux persiste plus de 10 jours, consultez un médecin.");
        faqMap.put("Est-ce que la bronchite est contagieuse? Comment la soigner?",
                "La bronchite virale est contagieuse. Repos, hydratation et, parfois, médicaments prescrits sont nécessaires.");
        faqMap.put("Est-ce que la pneumonie est contagieuse?",
                "Certaines pneumonies peuvent être contagieuses. Consultez rapidement pour un traitement adapté.");
        faqMap.put("Qu’est-ce que la maladie pulmonaire obstructive?",
                "C'est une maladie qui rend la respiration difficile, souvent liée au tabagisme. Elle nécessite une prise en charge médicale.");
        faqMap.put("Quand doit-on consulter pour un mal de gorge? Que faire?",
                "Si le mal dure plus de 48h, est très intense, ou accompagné de fièvre, consultez un médecin.");
        faqMap.put("Qu’est-ce qui cause la haute pression artérielle? Comment l’abaisser?",
                "Elle est souvent causée par le stress, l'alimentation, ou des facteurs héréditaires. Bougez plus, mangez sainement et consultez un médecin.");
        faqMap.put("Comment faire baisser son taux de cholestérol?",
                "Mangez plus de fibres, moins de gras saturés, faites de l'exercice et, si nécessaire, prenez des médicaments sur prescription.");
    }

    public static String getAnswer(String question) {
        return faqMap.getOrDefault(question, "Désolé, je n'ai pas la réponse à cette question.");
    }

    public static Iterable<String> getQuestions() {
        return faqMap.keySet();
    }
}
