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
package org.jaqpot.core.service.cron;

/**
 *
 * @author chung
 */
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import com.microtripit.mandrillapp.lutung.view.MandrillUserInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.BibTeXHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.PmmlHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.Task;

@Stateless
public class SendStatisticsCron {

    private static final Logger LOG = Logger.getLogger(SendStatisticsCron.class.getName());

    private ResourceBundle configResourceBundle;

    @PostConstruct
    private void init() {
        configResourceBundle = ResourceBundle.getBundle("config");
    }

    @EJB
    TaskHandler taskHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    BibTeXHandler bibTeXHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    PmmlHandler pmmlHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    FeatureHandler featureHandler;

    @Schedule(dayOfWeek = "Sun", hour = "0", minute = "0", second = "0", info = "Mailer", persistent = false) // Every Sunday midnight a mail will be sent
    public void mailStatisticsWeekly() throws MandrillApiError, IOException {
        String doSendMail = configResourceBundle.getString("jaqpot.mail.dosend");
        if (doSendMail == null || !"true".equals(doSendMail)) {
            return;
        }
        String mandrillApiKey = configResourceBundle.getString("jaqpot.mail.mandrillApiKey");

        LOG.log(Level.INFO, "running every minute .. now it''s: {0}", new Date().toString());
        MandrillApi mandrill = new MandrillApi(mandrillApiKey);
        MandrillUserInfo user = mandrill.users().info();
        LOG.log(Level.INFO, "User {0} with reputation {1}",
                new Object[]{user.getUsername(), user.getReputation().toString()});

        Long allTasks = taskHandler.countAll();
        Long errorTasks = taskHandler.countByStatus(Task.Status.ERROR);
        Long runningTasks = taskHandler.countByStatus(Task.Status.RUNNING);
        Long completedTasks = taskHandler.countByStatus(Task.Status.COMPLETED);
        Long queuedTasks = taskHandler.countByStatus(Task.Status.QUEUED);
        Long totalModels = modelHandler.countAll();
        Long totalBibTeX = bibTeXHandler.countAll();
        Long totalAlgorithms = algorithmHandler.countAll();
        Long totalPmml = pmmlHandler.countAll();
        Long totalUsers = userHandler.countAll();
        Long totalFeatures = featureHandler.countAll();
        Long totalDatasets = datasetHandler.countAll();

        String msgContent = String.format("Dear Jaqpot administrator, \n\n"
                + "This is a briefing of your system's DB status:"
                + "\n\n"
                + "Tasks\n"
                + "+ total      : %d\n"
                + "+ error      : %d\n"
                + "+ running    : %d\n"
                + "+ complete   : %d\n"
                + "+ in queue   : %d\n"
                + "\n"
                + "Models       : %d\n"
                + "PMML         : %d\n"
                + "Users        : %d\n"
                + "Algorithms   : %d\n"
                + "Datasets     : %d\n"
                + "Features     : %d\n"
                + "BibTeX       : %d\n"
                + "\n\n"
                + "Best regards,\n"
                + "%s",
                allTasks, errorTasks, runningTasks, completedTasks, queuedTasks,
                totalModels,
                totalPmml,
                totalUsers,
                totalAlgorithms,
                totalDatasets,
                totalFeatures,
                totalBibTeX,
                configResourceBundle.getString("jaqpot.mail.fromName"));

        MandrillMessage message = new MandrillMessage();
        message.setSubject("Jaqpot Statistics");
        message.setText(msgContent);
        message.setFromEmail(configResourceBundle.getString("jaqpot.mail.fromMail"));
        message.setFromName(configResourceBundle.getString("jaqpot.mail.fromName"));

        List<MandrillMessage.Recipient> recipients = new ArrayList<>();

        String admins = configResourceBundle.getString("jaqpot.mail.recipients");
        List<String> recipientsList = Arrays.asList(admins.split("\\s*,\\s*"));

        MandrillMessage.Recipient recipient;

        for (String r : recipientsList) {
            LOG.log(Level.INFO, "adding {0} to the list of recipients", r);
            recipient = new MandrillMessage.Recipient();
            recipient.setEmail(r);
            recipients.add(recipient);
        }

        message.setTo(recipients);

        message.setPreserveRecipients(true);

        MandrillMessageStatus[] statuses = mandrill.messages().send(message, Boolean.FALSE);
        LOG.log(Level.INFO, "Message sending completed {0}", new Date().toString());
        boolean sendingSuccessful = true;
        for (MandrillMessageStatus s : statuses) {
            sendingSuccessful = sendingSuccessful && s.getRejectReason() == null;
            if (s.getRejectReason() != null) {
                LOG.log(Level.INFO, "Rejection reason : {0}", s.getRejectReason());
            }
        }
        LOG.log(Level.INFO, "Sending email {0}", sendingSuccessful ? "succeeded" : "failed");
        if (!sendingSuccessful) {
            LOG.log(Level.SEVERE, "Email was rejected! (check logs for details)");
        }

    }
}
