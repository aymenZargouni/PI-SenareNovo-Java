package ed.sanarenovo.tests;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.services.PatientService;

import java.util.List;

public class PatientTestService {
    public static void main(String[] args) {
        PatientService patientService = new PatientService();
        List<Patient> patients = patientService.getAll();
        for (Patient patient : patients) {
            System.out.println(patient);
        }
    }
}
