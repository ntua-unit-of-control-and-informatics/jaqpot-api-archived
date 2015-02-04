/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.db.JaqpotEntityManager;
import org.jaqpot.core.model.User;

/**
 *
 * @author hampos
 */
@Stateless
public class UserHandler extends AbstractHandler<User>{
    
    @Inject JaqpotEntityManager em;

    public UserHandler() {
        super(User.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }
    
}
