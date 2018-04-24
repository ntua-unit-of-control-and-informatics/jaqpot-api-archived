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
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel( value = "ErrorReport", description = "Error report for various exceptions in the Jaqpot Framework" )
public class ErrorReport extends JaqpotEntity {
    
    /** Error code. */
    @ApiModelProperty(value = "Error code")
    private String code;
    /**
     * Who is to blame.
     */
    @ApiModelProperty(value = "Who is to blame")
    private String actor;
    /**
     * Short error message;
     */
    @ApiModelProperty(value = "Short error message")
    private String message;
    /**
     * Details to be used for debugging.
     */
    @ApiModelProperty(value = "Details to be used for debugging.")
    private String details;
    /**
     * Accompanying HTTP status.
     */
    @ApiModelProperty(value = "Accompanying HTTP status.")
    private int httpStatus = 0;
    /**
     * Trace error report.
     */
//    @ApiModelProperty(value = "Trace error report.")
//    private ErrorReport trace;

    public ErrorReport() {
    }

    public ErrorReport(String id) {
        super(id);
    }
    
    public ErrorReport(ErrorReport other) {
        super(other);
        this.actor = other.actor;
        this.code = other.code;
        this.details = other.details;
        this.httpStatus = other.httpStatus;
        this.message = other.message;
//        this.trace = new ErrorReport(other.trace);
    } 

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

//    public ErrorReport getTrace() {
//        return trace;
//    }
//
//    public void setTrace(ErrorReport trace) {
//        this.trace = trace;
//    }
    
    
}
