package org.openmrs.module.isanteplusfhir.api.action;

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LaboratoryFormAction implements CustomFormSubmissionAction {
    
    private OrderService orderService;

    private EncounterService encounterService;
    
    private static final String TEST_CLASS = "Test";
    
    private static final Integer DESTINATION_CONCEPT_ID = 160632;
    
    private static final String OPENELIS_DESTINATION = "OpenELIS";
    
    private Log log = LogFactory.getLog(this.getClass());
    
    @Override
    public void applyAction(FormEntrySession formSession) {
        Encounter labFormEncounter = formSession.getEncounter();
        System.out.println("-----------------Post processing----------------------------");
        //add this tag to the Labartory html form to invoke this class on submit
       // <postSubmissionAction class="org.openmrs.module.isanteplusfhir.api.action.LaboratoryFormAction"/> 
        orderService = Context.getOrderService();
        encounterService =  Context.getEncounterService();
        CareSetting careSetting = orderService.getCareSetting(2);
        
        Optional<Obs> destinationObs = labFormEncounter.getObs().stream()
                .filter(obs -> obs.getConcept().getConceptId().equals(DESTINATION_CONCEPT_ID)).findFirst();
        if (destinationObs.isPresent()) {
            if (destinationObs.get().getValueText().equals(OPENELIS_DESTINATION)) {
                Set<Concept> testConcepts = new HashSet<>();
                labFormEncounter.getObs().forEach(obs -> {
                    if (obs.getValueCoded() != null) {
                        if (obs.getValueCoded().getConceptClass().getName().equals(TEST_CLASS)) {
                            testConcepts.add(obs.getValueCoded());
                        }
                    }  
                });
                
                Encounter newEncounter= new Encounter();
                newEncounter.setPatient(labFormEncounter.getPatient());
                newEncounter.setLocation(labFormEncounter.getLocation());
                newEncounter.setEncounterProviders(labFormEncounter.getActiveEncounterProviders());
                newEncounter.setEncounterDatetime(labFormEncounter.getEncounterDatetime());
                newEncounter.setEncounterType(labFormEncounter.getEncounterType());

                Set<Order> orders = testConcepts.stream().map(testConcept -> {
                    TestOrder order = new TestOrder();
                    order.setConcept(testConcept);
                    order.setEncounter(newEncounter);
                    order.setPatient(labFormEncounter.getPatient());
                    order.setOrderer(labFormEncounter.getEncounterProviders().stream().findFirst().get().getProvider());
                    order.setCareSetting(careSetting);
                    return order;
                }).collect(Collectors.toSet());
                newEncounter.setOrders(orders);
                encounterService.saveEncounter(newEncounter);
            }
        } 
    }
}
