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
package org.jaqpot.core.service.httphandlers;

import javax.ejb.Stateless;
import org.jaqpot.core.model.MetaInfo;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class Rights {

    public Boolean canWrite(MetaInfo metaInfo, User user) {
        Boolean canWrite = false;
        if (metaInfo.getCreators().contains(user.get_id())) {
            canWrite = true;
        }
        try{
            for (String org : user.getOrganizations()) {
                if (metaInfo.getWrite() != null && metaInfo.getWrite().contains(org)) {
                    canWrite = true;
                }
            }
        }catch(NullPointerException e){
            return canWrite;
        }
        return canWrite;
    }

    public Boolean canTrash(MetaInfo metaInfo, User user) {
        Boolean canTrash = false;
        if (metaInfo.getCreators().contains(user.get_id())) {
            canTrash = true;
        }
        return canTrash;
    }

    public Boolean canView(MetaInfo mf, User user) {
        Boolean canView = false;
        if (mf.getCreators() != null && mf.getCreators().contains(user.get_id())) {
            canView = true;
        }
        if (mf.getRead() != null) {
            if (mf.getRead().contains("Jaqpot")) {
                canView = true;
            }
            try{
                for (String org : user.getOrganizations()) {
                    if (mf.getRead().contains(org)) {
                        canView = true;
                    }
                }
            }catch(NullPointerException e){
                return canView;
            }
        }
        return canView;
    }

}
