package ca.carleton.tim.ksat.client;

import java.util.HashSet;
import java.util.Vector;

import org.eclipse.jface.wizard.Wizard;

import ca.carleton.tim.ksat.model.Site;

public class AddSitesWizard extends Wizard {

    protected final HashSet<Site> initialSites;
    private SitesFromDBPage sitesFromDBPage;

    public AddSitesWizard(HashSet<Site> initialSites, Vector<Site> allSitesFromDB) {
        this.initialSites = initialSites;
        sitesFromDBPage = new SitesFromDBPage("Available Sites from the Database",
            initialSites, allSitesFromDB);
        addPage(sitesFromDBPage);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

}