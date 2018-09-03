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
import java.util.List;

/**
 * User of Jaqpot.
 *
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
     * Storage capabilities of the user. E.g., number of models, algorithms,
     * etc, that the user can create.
     */
    private Map<String, Integer> capabilities;
    /**
     * Weekly publication capabilities of the user. How many resources the user
     * can publish per week.
     *
     * @see UserFacade
     */
    private Map<String, Integer> publicationRatePerWeek;

    private List<String> organizations;

    private String profilePic;

    private String occupation;

    private String occupationAt;

    private String github;

    private String linkedin;

    private String website;

    private String twitter;

    private String cv;

    private String about;

    private String livesAtCity;

    private String livesAtCountry;

    public User() {
    }

    public User(String id) {
        super(id);
    }

    public User(User other) {
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

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOccupationAt() {
        return occupationAt;
    }

    public void setOccupationAt(String occupationAt) {
        this.occupationAt = occupationAt;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedIn) {
        this.linkedin = linkedIn;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getLivesAtCity() {
        return livesAtCity;
    }

    public void setLivesAtCity(String livesAtCity) {
        this.livesAtCity = livesAtCity;
    }

    public String getLivesAtCountry() {
        return livesAtCountry;
    }

    public void setLivesAtCountry(String livesAtCountry) {
        this.livesAtCountry = livesAtCountry;
    }

}
