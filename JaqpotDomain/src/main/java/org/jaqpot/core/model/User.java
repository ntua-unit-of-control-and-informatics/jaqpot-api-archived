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
package org.jaqpot.core.model;

/**
 *
 * @author chung
 */
public class User extends JaqpotEntity {
    
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

    /*
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
