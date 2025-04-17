package ed.sanarenovo.interfaces;

import ed.sanarenovo.entities.Patient;

import java.util.List;

public interface IPatientService {
    void register(Patient patient);
    List<Patient> getAll();
    Patient getById(int id);
}
