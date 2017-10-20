/*
 *
 *   Ambit Client
 *
 *   Ambit Client is licensed by GPL v3 as specified hereafter. Additional components may ship
 *   with some other licence as will be specified therein.
 *
 *   Copyright (C) 2016 KinkyDesign
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Source code:
 *   The source code of Ambit Client is available on github at:
 *   https://github.com/KinkyDesign/AmbitClient
 *   All source files of Ambit Client that are stored on github are licensed
 *   with the aforementioned licence.
 *
 */
package org.jaqpot.core.model.dto.bundle;



import org.jaqpot.core.model.dto.dataset.Substance;

import java.util.List;
import java.util.Map;

/**
 * @author Angelos Valsamis
 * @author Charalampos Chomenidis
 */
public class BundleData {

    private String bundleId;
    private String bundleUri;
    private String description;
    private String substanceOwner;
    private List<Substance> substances;
    private Map<String, List<String>> properties;

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleUri() {
        return bundleUri;
    }

    public void setBundleUri(String bundleUri) {
        this.bundleUri = bundleUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubstanceOwner() {
        return substanceOwner;
    }

    public void setSubstanceOwner(String substanceOwner) {
        this.substanceOwner = substanceOwner;
    }

    public List<Substance> getSubstances() {
        return substances;
    }

    public void setSubstances(List<Substance> substances) {
        this.substances = substances;
    }

    public Map<String, List<String>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, List<String>> properties) {
        this.properties = properties;
    }

}
