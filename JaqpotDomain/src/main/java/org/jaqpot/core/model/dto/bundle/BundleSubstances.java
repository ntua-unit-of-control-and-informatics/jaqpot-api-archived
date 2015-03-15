/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jaqpot.core.model.dto.dataset.Substance;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleSubstances {

    List<Substance> substance;

    public List<Substance> getSubstance() {
        return substance;
    }

    public void setSubstance(List<Substance> substance) {
        this.substance = substance;
    }

}
