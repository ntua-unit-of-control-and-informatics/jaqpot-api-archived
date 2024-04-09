/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.models;

import java.util.List;

/**
 *
 * @author pantelispanka
 */
public class User {
    
    private String _id;
    private String occupation;
    private String occupationAt;
    private Meta meta;
    private Boolean emailVerified;
    private String name;
    private String preferredUsername;
    private String givenName;
    private String familyName;
    private String email;
    private List<String> organizations;
    private float credits;
    private List<Spent> spenton;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta metaInfo) {
        this.meta = metaInfo;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public float getCredits() {
        return credits;
    }

    public void setCredits(float credits) {
        this.credits = credits;
    }

    public List<Spent> getSpenton() {
        return spenton;
    }

    public void setSpenton(List<Spent> spenton) {
        this.spenton = spenton;
    }
    
    

}
