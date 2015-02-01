/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author chung
 */
public class User extends JaqpotCoreComponent {
    
      private String uid;
    private String name;
    private String mail;
    private String hashedPass;
    private int maxParallelTasks = 0;
    private int maxModels = 0;
    private int maxBibTeX = 0;
    private int maxConformers = 0;
    public static final User GUEST = new User("guest@opensso.in-silico.ch", 
            "Guest", 
            "anonymous@anonymous.org", 
            "{SSHA}ficDnnD49QMLnwStKABXzDvFIgrd/c4H");

    public int getMaxBibTeX() {
        return maxBibTeX;
    }

    public void setMaxBibTeX(int maxBibTeX) {
        this.maxBibTeX = maxBibTeX;
    }

    public int getMaxModels() {
        return maxModels;
    }

    public void setMaxModels(int maxModels) {
        this.maxModels = maxModels;
    }

    public int getMaxParallelTasks() {
        return maxParallelTasks;
    }

    public void setMaxParallelTasks(int maxParallelTasks) {
        this.maxParallelTasks = maxParallelTasks;
    }

    public User() {
    }

    private User(String uid, String name, String mail, String hashedPass) {
        this.uid = uid;
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
     * @throws ToxOtisException
     *      In case the provided e-mail address is not compliant to the
     *      specifications of RFC 2822.
     * @see EmailValidator
     */
//    public User setMail(String mail) throws ToxOtisException {
//        if (!EmailValidator.validate(mail)) {
//            throw new org.opentox.toxotis.exceptions.impl.ToxOtisException("Bad email address according to RFC 2822 : '" + mail + "'");
//        }
//        this.mail = mail;
//        return this;
//
//    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UID   : ");
        sb.append(uid);
        sb.append("\n");
        sb.append("Name  : ");
        sb.append(name);
        sb.append("\n");
        sb.append("Mail  : ");
        sb.append(mail);
        sb.append("\n");
        sb.append("Pass  : ");
        sb.append(hashedPass);
        return new String(sb);
    }

   
    
}
