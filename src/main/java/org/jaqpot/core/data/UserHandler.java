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
package org.jaqpot.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.User;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class UserHandler extends AbstractHandler<User> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public UserHandler() {
        super(User.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public List<User> findAllWithPattern(Map<String, Object> searchFor) {
        Map<String, Object> properties = new HashMap<>();
        searchFor.keySet().forEach((key) -> {
            Object pattern = ".*" + searchFor.get(key) + ".*";
            properties.put(key, pattern);
        });

        List<String> fields = new ArrayList<>();
        fields.add("_id");

        return em.findAllWithReqexp(User.class, properties, fields, 0, Integer.MAX_VALUE);
    }

    public User getProfPic(String id) {
        List<String> fields = new ArrayList<>();
        fields.add("meta.picture");
        return em.find(User.class, id, fields);
    }

    public User getOccupation(String id) {
        List<String> fields = new ArrayList<>();
        fields.add("occupation");
        return em.find(User.class, id, fields);
    }

    public User getOccupationAt(String id) {
        List<String> fields = new ArrayList<>();
        fields.add("occupationAt");
        return em.find(User.class, id, fields);
    }
    
    public User getName(String id) {
        List<String> fields = new ArrayList<>();
        fields.add("name");
        return em.find(User.class, id, fields);
    }
    
    public User getOrganizations(String id){
        List<String> fields = new ArrayList<>();
        fields.add("organizations");
        return em.find(User.class, id, fields);
    }

}
