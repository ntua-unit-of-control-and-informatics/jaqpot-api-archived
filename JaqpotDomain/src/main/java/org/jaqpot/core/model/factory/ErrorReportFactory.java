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
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class ErrorReportFactory {

    private final static String ERROR400 = "The request could not be understood by the server due to malformed syntax. "
            + "The client SHOULD NOT repeat the request without modifications.",
            ERROR401 = "The request requires user authentication.",
            ERROR403 = "The server understood the request, but is refusing to fulfill it. "
            + "Authorization will not help and the request SHOULD NOT be repeated. "
            + "If the request method was not HEAD and the server wishes to make public why "
            + "the request has not been fulfilled, it SHOULD describe the reason for the "
            + "refusal in the entity. If the server does not wish to make this information "
            + "available to the client, the status code 404 (Not Found) can be used instead. ",
            ERROR404 = "The server has not found anything matching the Request-URI. "
            + "No indication is given of whether the condition is temporary or permanent.",
            ERROR500 = "The server encountered an unexpected condition which prevented it "
            + "from fulfilling the request. ",
            ERROR501 = "The server does not support the functionality required to fulfill the request. "
            + "This is the appropriate response when the server does not recognize the request method "
            + "and is not capable of supporting it for any resource. ",
            ERROR502 = "The server, while acting as a gateway or proxy, received an invalid response "
            + "from the upstream server it accessed in attempting to fulfill the request. ",
            ERROR503 = "Service overloaded or down. ";

    public static ErrorReport unauthorized() {
        return ErrorReportFactory.unauthorized(null, null, null);
    }

    public static ErrorReport unauthorized(String message) {
        return ErrorReportFactory.unauthorized(message, null, null);
    }

    public static ErrorReport unauthorized(String message, String code) {
        return ErrorReportFactory.unauthorized(message, code, null);
    }

    public static ErrorReport unauthorized(String message, String code, String details) {
        ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setActor("client")
                .setMessage(message != null ? message : "You are not authorized to perform this operation.")
                .setDetails(details != null ? details : ERROR401)
                .setCode("Unauthorized" + (code != null ? "::" + code : ""))
                .setHttpStatus(400)
                .build();
        return error;
    }

    public static ErrorReport alreadyInDatabase(String details) {
        ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setActor("server")
                .setMessage("You tried to register a resource, but it is already registered in our DB.")
                .setDetails(details)
                .setCode("AlreadyRegistered")
                .setHttpStatus(400)
                .build();
        return error;
    }

    public static ErrorReport notImplementedYet() {
        ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setActor("server")
                .setMessage("This is not implemented yet.")
                .setDetails(ERROR501)
                .setCode("NotImplemented")
                .setHttpStatus(501)
                .build();
        return error;
    }

    public static ErrorReport serviceUnavailable(
            String message,
            String details) {
         ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setActor("server")
                .setMessage(message != null ? message :"Service overloaded or down.")
                .setDetails(ERROR503)
                .setCode("ServiceUnavailable")
                .setHttpStatus(503)
                .build();
        return error;
    }

    /**
     * Resource not found error. Error report that is generated when a resource
     * is not found.
     *
     * @param uriNotFound The URI that is not found on the server.
     * @return Error report with HTTP status code 404.
     */
    public static ErrorReport notFoundError(
            String uriNotFound) {
        return ErrorReportBuilder.builderRandomId().
                setActor("client").
                setCode("NotFound").
                setMessage("URI was not found on the server").
                setDetails(ERROR404).
                setHttpStatus(404).
                build();
    }

    /**
     * Generates an ISE error report.
     *
     * @param code Error code (to identify errors of such type).
     * @param message Message that helps the client understand, in simple words,
     * the reason for this exceptional event.
     * @param details Details of the error to help debugging.
     * @return Error report with HTTP status code 500.
     */
    public static ErrorReport internalServerError(
            String code,
            String message,
            String details) {
        return ErrorReportBuilder.builderRandomId().
                setActor("server").
                setCode(code).
                setMessage(message).
                setDetails(details).
                setHttpStatus(500).
                build();
    }

    public static ErrorReport internalServerError() {
        return ErrorReportBuilder.builderRandomId().
                setActor("server").
                setCode("UnknownServerError").
                setMessage("Utterly unexpected condition").
                setDetails(ERROR500).
                setHttpStatus(500).
                build();
    }

    /**
     * Generates an ISE error report out of a Throwable.
     *
     * @param ex A Throwable that leads to this exceptional event for which this
     * report is generated. MUST NOT be <code>null</code>!
     * @param code Error code (to identify errors of such type).
     * @param additionalMessage Additional custom message to explain the
     * situation. Set to <code>null</code> if you don't want to specify an
     * additional message.
     * @param addittionalDetails Additional details (apart from the ones
     * provided with the Throwable). Set to <code>null</code> if you don't want
     * to provide additional details.
     * @return Error report with HTTP status code 500.
     */
    public static ErrorReport internalServerError(
            Throwable ex,
            String code,
            String additionalMessage,
            String addittionalDetails) {
        return ErrorReportBuilder.builderRandomId().
                setHttpStatus(500).
                build();
    }

    /**
     * Error report corresponding to a bad request from a client. Accompanied by
     * HTTP status code 400.
     *
     * @param message Message to explain what went wrong.
     * @param details Details.
     * @return Error report with status code 400 and code
     * <code>"BadRequest"</code>.
     */
    public static ErrorReport badRequest(
            String message,
            String details) {
        return ErrorReportBuilder.builderRandomId().
                setActor("client").
                setCode("BadRequest").
                setMessage(message != null ? message : "Bad request").
                setDetails((details != null ? (details + ".\n") : "")
                        + ERROR400).
                setHttpStatus(400).
                build();
    }

    /**
     * Error report returned when the client is not authorized to access a
     * particular resource. The client is given an error report with HTTP status
     * code 403.
     *
     * @param uri The URI that the client is not authorized to access
     * @return
     */
    public static ErrorReport unauthorizedAccessError(String uri) {
        return ErrorReportBuilder.builderRandomId().
                setActor("client").
                setCode("UnauthorizedAccessError").
                setMessage("You are not authorized to access " + uri).
                setDetails(ERROR403).
                setHttpStatus(403).
                build();
    }

    public static ErrorReport authenticationRequired(String details) {
        return ErrorReportBuilder.builderRandomId().
                setActor("client").
                setCode("Authentication Required (Missing/insuficient credentials)").
                setMessage("Authentication is required tp access this URI").
                setDetails((details != null ? "Details: " + details + "." : "")
                        + ERROR401).
                setHttpStatus(401).
                build();
    }

    public static ErrorReport remoteError(
            String remoteUri,
            ErrorReport remoteException) {
        return ErrorReportBuilder.builderRandomId().
                setActor(remoteUri).
                setCode("RemoteInvocationError").
                setMessage("Remote invocation error").
                setDetails(ERROR502).
                setHttpStatus(502).
                setTrace(remoteException).
                build();
    }

}
