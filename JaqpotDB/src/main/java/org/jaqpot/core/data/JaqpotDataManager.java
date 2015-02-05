/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jaqpot.core.data.serialize.EntityJSONSerializer;
import org.jaqpot.core.annotations.Jackson;

/**
 *
 * @author hampos
 */
@Singleton
public class JaqpotDataManager {

    @Inject
    @Jackson
    EntityJSONSerializer jsonSerializer;

    @Produces
    public EntityJSONSerializer getJSONSerializer() {
        return jsonSerializer;
    }
}
