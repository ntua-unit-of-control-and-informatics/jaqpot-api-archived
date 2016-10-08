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

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * User of  Jaqpot.
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends JaqpotEntity {

    /**
     * Name of the user.
     */
    private String name;
    /**
     * Email address of the user.
     */
    private String mail;
    /**
     * Hashed password.
     */
    private String hashedPass;
    /**
     * Storage capabilities of the user.
     * E.g., number of models, algorithms, etc, that the user
     * can create.
     */
    private Map<String, Integer> capabilities;   
    /**
     * Weekly publication capabilities of the user.
     * How many resources the user can publish per week.
     * 
     * @see UserFacade
     */
    private Map<String, Integer> publicationRatePerWeek;

    public User() {
    }

    public User(String id) {
        super(id);
    }
    public User(User other){
        super(other);
        this.capabilities = other.capabilities;
        this.hashedPass = other.hashedPass;
        this.mail = other.mail;
        this.name = other.name;
        this.publicationRatePerWeek = other.publicationRatePerWeek;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getHashedPass() {
        return hashedPass;
    }

    public void setHashedPass(String hashedPass) {
        this.hashedPass = hashedPass;
    }

    public Map<String, Integer> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Integer> capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, Integer> getPublicationRatePerWeek() {
        return publicationRatePerWeek;
    }

    public void setPublicationRatePerWeek(Map<String, Integer> publicationRatePerWeek) {
        this.publicationRatePerWeek = publicationRatePerWeek;
    }        

}
