/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.models;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author pantelispanka
 */
public class Organization {
  
    private String _id;
    private String title;
    private Meta meta;
    private String creator;
    private List<String> admins;
    private List<String> users;
    private String markdown;
    private HashMap<String, String> contact;
    private float credits;
    private List<String> contacttypes;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public HashMap<String, String> getContact() {
        return contact;
    }

    public void setContact(HashMap<String, String> contact) {
        this.contact = contact;
    }

    public float getCredits() {
        return credits;
    }

    public void setCredits(float credits) {
        this.credits = credits;
    }

    public List<String> getContacttypes() {
        return contacttypes;
    }

    public void setContacttypes(List<String> contacttypes) {
        this.contacttypes = contacttypes;
    }
    
}
