package org.openmrs.module.isanteplusfhir.api.action;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;

public class LaboratoryFormAction implements CustomFormSubmissionAction {

    @Override
    public void applyAction(FormEntrySession formSession) {
        //add this tag to the html form to invoke this class on submit
        
       // <postSubmissionAction class="org.openmrs.module.isanteplusfhir.api.action.LaboratoryFormAction"/> 
        Patient pat = formSession.getPatient();
        System.out.println("-----------------Post processing----------------------------");
        
    }
    
}
