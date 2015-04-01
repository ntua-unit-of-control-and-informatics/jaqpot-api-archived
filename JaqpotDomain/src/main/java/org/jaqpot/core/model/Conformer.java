/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Conformer extends Substance {

    /**
     * Mapping from representation name to the representation's content.
     */
    private Map<String, String> representations; //e.g., SMILES --> c1ccccc1 

    /**
     * The compound that owns this feature.
     */
    private String fatherCompound;

    /**
     * BibTeX reference as of where one can find more information.
     */
    private BibTeX bibtex;
    
    /**
     * Substructures and fragments of the compound.
     * Used to facilitate look-ups.
     */
    private Set<String> substructures;

    public Conformer() {
    }

    public Conformer(String id) {
        super(id);
    }

    public Conformer(Conformer other) {
        super(other);
        this.bibtex = other.bibtex != null ? new BibTeX(other.bibtex) : null;
        this.fatherCompound = other.fatherCompound;
        this.representations = other.representations != null ? new HashMap<>(other.representations) : null;
        this.substructures = other.substructures != null ? new HashSet<>(other.substructures) : null;
    }

    public Set<String> getSubstructures() {
        return substructures;
    }

    public void setSubstructures(Set<String> substructures) {
        this.substructures = substructures;
    }
    
    

    public Map<String, String> getRepresentations() {
        return representations;
    }

    @JsonIgnore // To make sure that nothing strange happens with the serialization
    public String putRepresentation(String key, String value) {
        if (representations == null) { // initialize representations if null
            setRepresentations(new HashMap<>());
        }
        return representations.put(key, value);
    }

    public void setRepresentations(Map<String, String> representations) {
        this.representations = representations;
    }

    public String getFatherCompound() {
        return fatherCompound;
    }

    public void setFatherCompound(String fatherCompound) {
        this.fatherCompound = fatherCompound;
    }

    public BibTeX getBibtex() {
        return bibtex;
    }

    public void setBibtex(BibTeX bibtex) {
        this.bibtex = bibtex;
    }

}
