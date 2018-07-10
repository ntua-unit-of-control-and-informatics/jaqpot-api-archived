/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jaqpot.core.model.dto.dataset.EntryId;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleSubstances {

    List<EntryId> substance;

    public List<EntryId> getSubstance() {
        return substance;
    }

    public void setSubstance(List<EntryId> substance) {
        this.substance = substance;
    }

}
