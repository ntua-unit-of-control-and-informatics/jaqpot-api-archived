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

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author chung
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Task extends JaqpotEntity {
    
    /**
     * Status of the task revealing information about the asynchronous job with
     * which the task is related and runs on a server.
     */
    public enum Status {

        /**
         * The task is still Running in the background as an asynchronous job. This
         * status means that the task has been submitted to the Execution Pool but has
         * not completed yet.
         */
        RUNNING,
        /**
         * The task has completed execution successfully. The result can be found under
         * #resultUri and is accessible via {@link Task#getResultUri() #getResultUri()}.
         * The corresponding status is either 200 if the result is the URI of a created
         * resource or 201 if it redirects to some other task (most probably on some
         * other server)
         */
        COMPLETED,
        /**
         * The task is canceled by the User.
         */
        CANCELLED,
        /**
         * Task execution was interrupted due to some error related with the asynchronous
         * job, either due to a bad request by the client, an internal server error or an
         * error related to a third remote service. In such a case, the task is accompanied
         * by an error report that provides access to details about the exceptional event.
         */
        ERROR,
        /**
         * Due to large load on the server or issues related to A&amp;A or user quota,
         * the task was rejected for execution.
         */
        REJECTED,
        /**
         * The task is created and put in an execution queue but is not running yet.
         * HTTP status codes of queued tasks is 202.
         */
        QUEUED;
    }

    private String resultUri;
    private Status hasStatus;
    private Float percentageCompleted;
    private ErrorReport errorReport;
    private Integer httpStatus;
    private String createdBy;
    private Long duration;

    public Task() {
        super();
    }

    public Task(String id) {
        super(id);
    }

    public String getResultUri() {
        return resultUri;
    }

    public void setResultUri(String resultUri) {
        this.resultUri = resultUri;
    }
    
    public Status getStatus() {
        return hasStatus;
    }
     
    public void setStatus(Status hasStatus) {
        this.hasStatus = hasStatus;
    }

    public Float getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(Float percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public ErrorReport getErrorReport() {
        return errorReport;
    }

    public void setErrorReport(ErrorReport errorReport) {
        this.errorReport = errorReport;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }        
}
