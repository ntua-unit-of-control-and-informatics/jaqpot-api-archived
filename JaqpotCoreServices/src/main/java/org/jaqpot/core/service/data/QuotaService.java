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
package org.jaqpot.core.service.data;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.BibTeXHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.UserQuota;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class QuotaService {
    
    private static final Logger LOG = Logger.getLogger(QuotaService.class.getName());
    
    @EJB
    TaskHandler taskHandler;
    
    @EJB
    AlgorithmHandler algorithmHandler;
    
    @EJB
    ModelHandler modelHandler;
    
    @EJB
    BibTeXHandler bibtexHandler;
    
    @EJB
    DatasetHandler datasetHandler;
    
    @EJB
    ReportHandler reportHandler;
    
    public UserQuota getUserQuota(String userId) {
        UserQuota userQuota = new UserQuota();
        userQuota.setUserId(userId);
        userQuota.setAlgorithms(algorithmHandler.countAllOfCreator(userId));
        userQuota.setTasks(taskHandler.countByUser(userId));
        userQuota.setTasksRunning(taskHandler.countByUserAndStatus(userId, Task.Status.RUNNING));
        userQuota.setModels(modelHandler.countAllOfCreator(userId));
        userQuota.setBibtex(bibtexHandler.countAllOfCreator(userId));
        userQuota.setDatasets(datasetHandler.countAllOfCreator(userId));
        userQuota.setReports(reportHandler.countAllOfCreator(userId));
        return userQuota;
    }
    
}
