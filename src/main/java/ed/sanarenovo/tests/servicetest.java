package ed.sanarenovo.tests;

import ed.sanarenovo.entities.Service;
import ed.sanarenovo.services.Serviceserv;

import java.util.List;

public class servicetest {
    public static void main(String[] args) {
        Service serviceASupprimer = new Service(14);

        Serviceserv serviceManager = new Serviceserv();
        serviceManager.deleteService(serviceASupprimer);
}}
