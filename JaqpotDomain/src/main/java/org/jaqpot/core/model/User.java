/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalambos Chomenides, Pantelis Sopasakis)
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
package org.jaqpot.core.model;

import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jaqpot.util.EmailValidator;

/**
 *
 * @author chung
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User extends JaqpotEntity {
    
    private String uid;
    private String name;
    private String mail;
    private String hashedPass;
    private Integer maxParallelTasks ;
    private Integer maxModels;
    private Integer maxBibTeX;
    private Integer maxConformers;
    public static final User GUEST = new User("guest@opensso.in-silico.ch", 
            "Guest", 
            "anonymous@anonymous.org", 
            "{SSHA}ficDnnD49QMLnwStKABXzDvFIgrd/c4H");

    public Integer getMaxBibTeX() {
        return maxBibTeX;
    }

    public void setMaxBibTeX(Integer maxBibTeX) {
        this.maxBibTeX = maxBibTeX;
    }

    public Integer getMaxModels() {
        return maxModels;
    }

    public void setMaxModels(Integer maxModels) {
        this.maxModels = maxModels;
    }

    public Integer getMaxParallelTasks() {
        return maxParallelTasks;
    }

    public void setMaxParallelTasks(Integer maxParallelTasks) {
        this.maxParallelTasks = maxParallelTasks;
    }

    public User() {
    }

    private User(String uid, String name, String mail, String hashedPass) {
        setId(uid);
        this.name = name;
        this.mail = mail;
        this.hashedPass = hashedPass;
    }

    public String getHashedPass() {
        return hashedPass;
    }

    public void setHashedPass(String hashedPass) {
        this.hashedPass = hashedPass;
    }

    /**
     * Retrieve the email of the user
     * @return
     *      User email
     */
    public String getMail() {
        return mail;
    }

    /**
     * Checks whether the provided e-mail address is RDF-2822 compliant and sets
     * the e-mail address of the user accordingly.
     * @param mail
     *      The e-mail address of the user (Must be RDF-2822 compliant).
     * @return 
     *      The current modifiable instance of User.
     * @throws IllegalArgumentException
     *      In case the provided e-mail address is not compliant to the
     *      specifications of RFC 2822.
     * @see EmailValidator
     */
    public User setMail(String mail) throws IllegalArgumentException {
        if (!EmailValidator.validate(mail)) {
            throw new IllegalArgumentException("Bad email address according to RFC 2822 : '" + mail + "'");
        }
        this.mail = mail;
        return this;

    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public int getMaxConformers() {
        return maxConformers;
    }

    public void setMaxConformers(Integer maxConformers) {
        this.maxConformers = maxConformers;
    }

   
    
}
