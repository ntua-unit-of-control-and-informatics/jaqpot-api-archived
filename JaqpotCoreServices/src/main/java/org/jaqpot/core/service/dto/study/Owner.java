package org.jaqpot.core.service.dto.study;

public class Owner {

    private Company company;
    private Substance substance;

    public Company getCompany() {
        return this.company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Substance getSubstance() {
        return this.substance;
    }

    public void setSubstance(Substance substance) {
        this.substance = substance;
    }
}
