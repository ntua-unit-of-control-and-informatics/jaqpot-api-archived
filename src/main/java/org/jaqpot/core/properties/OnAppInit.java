/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.properties;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.User;

/**
 *
 * @author pantelispanka
 */

@Startup
@Singleton
@DependsOn("MongoDBEntityManager")
public class OnAppInit {
    
    @Inject
    UserHandler userHandler;

    @PostConstruct
    void init(){
        
        String userToSearch = "guest";
        User user = userHandler.find(userToSearch);
        if(user == null){
            User initialUser = new User();
            initialUser.setId("guest");
            initialUser.setName("guest");
            Map<String, Integer> cap = new HashMap<>();
            cap.put("models", 180);
            cap.put("reports", 180);
            cap.put("algorithms", 200);
            cap.put("tasksParallel", 80);
            cap.put("substances" , 3000);
            cap.put("bibtex", 200);
            cap.put("datasets", 220);
            initialUser.setCapabilities(cap);
            Map<String, Integer> pub = new HashMap<>();
            pub.put( "models" , 100);
            pub.put("algorithms" , 10);
            pub.put("substances" , 100);
            pub.put("bibtex" , 100);
            initialUser.setPublicationRatePerWeek(pub);
            userHandler.create(initialUser);
        }
        
        
    }
    
    
}
