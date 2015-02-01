/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

/**
 *
 * @author chung
 */
public class Task extends JaqpotCoreComponent {
    
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
         * The task is cancelled by the User.
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
    private float percentageCompleted = -1;
    private ErrorReport errorReport;
    private float httpStatus = -1;
    private User createdBy;
    private long duration = 0L;

    public Task() {
        super();
    }

    

    /**
     * The duration of the task, that is the overall time since its execution started,
     * not including the time it was queued.
     * @return
     *      The duration of the task in millisenconds or <code>0</code> if no duration
     *      is assigned.
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Setter method for the duration of the task in milliseconds. The time during
     * which the task was in a queue should not be included in the execution
     * duration.
     * @param duration
     *      The duration of the task in milliseconds
     * @return
     *      The current updated modifiable Task object.
     */
    public Task setDuration(Long duration) {
        if (duration == null) {
            this.duration = 0;
            return this;
        }
        this.duration = duration;
        return this;
    }

    /**
     * Retrieve the user which created the task.
     * @return
     *      Creator of the task or <code>null</code> if not available.
     */
    public User getCreatedBy() {
        return createdBy;
    }

    public Task setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * The status of the task as an element of the enumeration {@link Status }.
     * A task can either be <code>RUNNING</code>, <code>COMPLETED</code>, <code>
     * CANCELLED</code> and <code>ERROR</code>.
     * @return
     *      The status of the task.
     */
    public Status getStatus() {
        return hasStatus;
    }

    /**
     * ParameterValue the status of a task.
     * 
     * @param status
     *      The new value for the status of the task.
     * 
     * @return 
     *      The current modifiable instance of Task.
     */
    public Task setStatus(Status status) {
        this.hasStatus = status;
        return this;
    }

    /**
     *
     * Get the percentage of completion of a running task.
     * 
     * @return
     *      Percentage of completion as a number in the range <code>[0, 100]</code>.
     */
    public float getPercentageCompleted() {
        return percentageCompleted;
    }

    public Task setPercentageCompleted(float percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
        return this;
    }

    public String getResultUri() {
        return resultUri;
    }

    public Task setResultUri(String resultUri) {
        this.resultUri = resultUri;
        return this;
    }

    public ErrorReport getErrorReport() {
        return errorReport;
    }

    public Task setErrorReport(ErrorReport errorReport) {
        this.errorReport = errorReport;
        return this;
    }

    public float getHttpStatus() {
        return httpStatus;
    }

    public Task setHttpStatus(float httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task {");
        sb.append(getId());
        sb.append("}\n");
        sb.append("HTTP Status : ");
        sb.append(getHttpStatus());
        sb.append("\n");
        sb.append("Status      : ");
        sb.append(hasStatus);
        sb.append("\n");
        if (resultUri != null) {
            sb.append("Result URI  : ");
            sb.append(resultUri);
            sb.append("\n");
        }
        return new String(sb);
    }
    
}
