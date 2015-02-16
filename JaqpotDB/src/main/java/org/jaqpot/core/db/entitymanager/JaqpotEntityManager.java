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
package org.jaqpot.core.db.entitymanager;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import org.jaqpot.core.model.JaqpotEntity;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public interface JaqpotEntityManager extends Closeable{

    public void persist(Object entity);

    public <T extends JaqpotEntity> T merge(T entity);

    public void remove(JaqpotEntity entity);

    public <T extends JaqpotEntity> T find(Class<T> entityClass, Object primaryKey);

    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max);
    
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, List<String> keys, List<String> fields);

    public <T extends JaqpotEntity> List<T> findAll(Class<T> entityClass, Integer start, Integer max);
    
}
