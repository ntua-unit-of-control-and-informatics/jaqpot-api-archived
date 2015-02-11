/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
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
          

    private UserBuilder(String id) {
        user = new User(id);
        user.setId(id);
    }

    public UserBuilder setName(String name) {
        user.setName(name);
        return this;
    }
    
    public UserBuilder setMail(String mail) throws IllegalArgumentException{
         if (!EmailValidator.validate(mail)) {
            throw new IllegalArgumentException("Bad email address according to RFC 2822 : '" + mail + "'");
        }
        user.setMail(mail);
        return this;
    }
    
    private void initCapabilities(){
        if (user.getCapabilities() == null){
            user.setCapabilities(new HashMap<>());
        }
    }
    
    private void initPublicationRatePerWeek(){
        if (user.getPublicationRatePerWeek()== null){
            user.setPublicationRatePerWeek(new HashMap<>());
        }
    }
    
    public UserBuilder setMaxBibTeX(int maxBibTeX) {
        initCapabilities();
        user.getCapabilities().put("bibtex", maxBibTeX);
        return this;
    }
    
    public UserBuilder setMaxSubstances(int maxSubstances) {
        initCapabilities();
        user.getCapabilities().put("substances", maxSubstances);
        return this;
    }
    
    public UserBuilder setMaxModels(int maxModels) {
        initCapabilities();
        user.getCapabilities().put("models", maxModels);
        return this;
    }
    
    public UserBuilder setMaxFeatures(int maxFeatures) {
        initCapabilities();
        user.getCapabilities().put("features",0);
        return this;
    }
    
    public UserBuilder setParallelTasks(int maxParallelTasks) {
        initCapabilities();
        user.getCapabilities().put("tasks.parallel", maxParallelTasks);
        return this;
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
