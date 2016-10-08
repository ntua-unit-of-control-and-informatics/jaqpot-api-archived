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
package org.jaqpot.core.model.facades;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaqpot.core.model.User;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class UserFacade {

    private final User user;
    private static final Logger LOG = Logger.getLogger(UserFacade.class.getName());

    public UserFacade(final User user) throws IllegalArgumentException {
        if (user == null) {
            String errorMessage = "Cannot create a facade for a null user";
            LOG.log(Level.WARNING, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public int getMaxCapability(String capability) {
        if (capability == null) {
            throw new NullPointerException("You need to provide the name of a capability!");
        }
        int maxCapability = -1;
        if (user.getCapabilities() != null && user.getCapabilities().containsKey(capability)) {
            Integer maxCapabilityRetrieved = user.getCapabilities().get(capability);
            if (maxCapabilityRetrieved != null) {
                return (int) maxCapabilityRetrieved;
            }
        }
        return maxCapability;
    }

    public int getMaxWeeklyPublicationRate(String capability) {
        if (capability == null) {
            throw new NullPointerException("You need to provide the name of a publication rate capability!");
        }
        int maxPublicationRateCapability = -1;
        if (user.getPublicationRatePerWeek() != null && user.getPublicationRatePerWeek().containsKey(capability)) {
            Integer maxPublicationRateCapabilityRetrieved = user.getPublicationRatePerWeek().get(capability);
            if (maxPublicationRateCapabilityRetrieved != null) {
                return (int) maxPublicationRateCapabilityRetrieved;
            }
        }
        return maxPublicationRateCapability;
    }

    public int getMaxSubstnaces() {
        return getMaxCapability("substances");
    }

    public int getMaxFeatures() {
        return getMaxCapability("features");
    }

    public int getMaxDatasets() {
        return getMaxCapability("datasets");
    }

    public int getMaxBibTeX() {
        return getMaxCapability("bibtex");
    }

    public int getMaxAlgorithms() {
        return getMaxCapability("algorithms");
    }

    public int getMaxModels() {
        return getMaxCapability("models");
    }

    public int getMaxReports() {
        return getMaxCapability("reports");
    }

    public int getMaxPublishedSubstancesPerWeek() {
        return getMaxWeeklyPublicationRate("substances");
    }

    public int getMaxPublishedFeaturesPerWeek() {
        return getMaxWeeklyPublicationRate("features");
    }

    public int getMaxPublishedDatasetsPerWeek() {
        return getMaxWeeklyPublicationRate("datasets");
    }

    public int getMaxPublishedAlgorithmsPerWeek() {
        return getMaxWeeklyPublicationRate("algorithms");
    }

}
