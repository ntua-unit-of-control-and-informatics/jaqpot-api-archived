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
package org.jaqpot.core.model.factory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.UserBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalambos Chomenidis
 */
public class UserFactory {

    private static MessageDigest sha256;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static User newNormalUser(String userName, String password) {
        String hashedPassword = password != null ? new String(Base64.getEncoder().encode(sha256.digest(password.getBytes()))) : null;
        sha256.reset();
        return UserBuilder.builder(userName)
                .setHashedPassword(hashedPassword)
                .setMaxBibTeX(100)
                .setMaxModels(100)
                .setMaxAlgorithms(5)
                .setMaxParallelTasks(5)
                .setMaxSubstances(1000)
                .setMaxWeeklyPublishedModels(10)
                .setMaxWeeklyPublishedSubstances(10)
                .setMaxWeeklyPublishedBibTeX(10)
                .setMaxWeeklyPublishedAlgorithms(1)
                .build();
    }

    public static User newNormalUser() {
        return UserBuilder.builder((String) null)
                .setMaxBibTeX(100)
                .setMaxModels(100)
                .setMaxAlgorithms(5)
                .setMaxParallelTasks(5)
                .setMaxSubstances(1000)
                .setMaxWeeklyPublishedModels(10)
                .setMaxWeeklyPublishedSubstances(10)
                .setMaxWeeklyPublishedBibTeX(10)
                .setMaxWeeklyPublishedAlgorithms(1)
                .build();
    }

}
