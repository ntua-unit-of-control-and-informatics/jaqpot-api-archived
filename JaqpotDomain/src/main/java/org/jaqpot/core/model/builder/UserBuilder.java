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
package org.jaqpot.core.model.builder;

import java.util.HashMap;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.validator.EmailValidator;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class UserBuilder implements EntityBuilder<User> {

    private final User user;

    public static UserBuilder builder(String id) {
        return new UserBuilder(id);
    }

    public static UserBuilder builder(User other) {
        return new UserBuilder(other);
    }

    private UserBuilder(String id) {
        user = new User(id);
        user.setId(id);
    }

    private UserBuilder(User other) {
        this.user = other;
    }

    public UserBuilder setName(String name) {
        user.setName(name);
        return this;
    }

    public UserBuilder setMail(String mail) throws IllegalArgumentException {
        if (!EmailValidator.validate(mail)) {
            throw new IllegalArgumentException("Bad email address according to RFC 2822 : '" + mail + "'");
        }
        user.setMail(mail);
        return this;
    }

    private void initCapabilities() {
        if (user.getCapabilities() == null) {
            user.setCapabilities(new HashMap<>());
        }
    }

    private void initPublicationRatePerWeek() {
        if (user.getPublicationRatePerWeek() == null) {
            user.setPublicationRatePerWeek(new HashMap<>());
        }
    }

    public UserBuilder setMaxCapability(String capabilityName, int max) {
        initCapabilities();
        user.getCapabilities().put(capabilityName, max);
        return this;
    }

    public UserBuilder setMaxWeeklyCapability(String weeklyCapabilityName, int max) {
        initPublicationRatePerWeek();
        user.getPublicationRatePerWeek().
                put(weeklyCapabilityName, max);
        return this;
    }

    public UserBuilder setMaxWeeklyPublishedBibTeX(int maxBibTeX) {
        return setMaxWeeklyCapability("bibtex", maxBibTeX);
    }

    public UserBuilder setMaxBibTeX(int maxBibTeX) {
        return setMaxCapability("bibtex", maxBibTeX);
    }

    public UserBuilder setMaxAlgorithms(int algorithms) {
        return setMaxCapability("algorithms", algorithms);
    }

    public UserBuilder setMaxWeeklyPublishedSubstances(int maxSubstances) {
        return setMaxWeeklyCapability("substances", maxSubstances);
    }

    public UserBuilder setMaxSubstances(int maxSubstances) {
        return setMaxCapability("substances", maxSubstances);
    }

    public UserBuilder setMaxWeeklyPublishedModels(int maxModels) {
        return setMaxWeeklyCapability("models", maxModels);
    }

    public UserBuilder setMaxModels(int maxModels) {
        return setMaxCapability("models", maxModels);
    }

    public UserBuilder setMaxWeeklyPublishedFeatures(int maxFeatures) {
        return setMaxWeeklyCapability("features", maxFeatures);
    }

    public UserBuilder setMaxWeeklyPublishedAlgorithms(int maxAlgorithms) {
        return setMaxWeeklyCapability("algorithms", maxAlgorithms);
    }

    public UserBuilder setMaxFeatures(int maxFeatures) {
        return setMaxCapability("features", maxFeatures);
    }

    public UserBuilder setMaxParallelTasks(int maxParallelTasks) {
        return setMaxCapability("tasksParallel", maxParallelTasks);
    }

    public UserBuilder setMaxDatasets(int maxDatasets) {
        return setMaxCapability("datasets", maxDatasets);
    }
    
    public UserBuilder setMaxReports(int maxReports) {
        return setMaxCapability("reports", maxReports);
    }

    public UserBuilder setHashedPassword(String hashedPassword) {
        user.setHashedPass(hashedPassword);
        return this;
    }

    @Override
    public User build() {
        return user;
    }

}
