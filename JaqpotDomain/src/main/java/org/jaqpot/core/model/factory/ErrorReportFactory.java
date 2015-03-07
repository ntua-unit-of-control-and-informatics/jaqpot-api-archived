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

    public static ErrorReport notFoundError(String uriNotFound) {
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor("client").
                setCode("NotFound").
                setMessage("URI " + uriNotFound + " was not found on the server").
                setDetails("The server has not found anything matching the Request-URI. "
                        + "No indication is given of whether the condition is temporary or permanent. ").
                setHttpStatus(404).
                build();
    }

    public static ErrorReport internalServerError(String code, String message, String details) {
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor("server").
                setCode(code).
                setMessage(message).
                setDetails(details).
                setHttpStatus(500).
                build();
    }

    public static ErrorReport badRequest(String message, String details) {
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor("client").
                setCode("Remote Invocation Error").
                setMessage("Remote invocation error").
                setDetails((details != null ? details : "") + "The request could not be "
                        + "understood by the server due to malformed syntax. The client "
                        + "SHOULD NOT repeat the request without modifications. ").
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
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor("client").
                setCode("Unauthorized").
                setMessage("You are not authorized to access " + uri).
                setDetails("The server understood the request, but is refusing to fulfill it. "
                        + "Authorization will not help and the request SHOULD NOT be repeated.").
                setHttpStatus(403).
                build();
    }

    public static ErrorReport authenticationRequired() {
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor("client").
                setCode("Authentication Required").
                setMessage("Authentication is required tp access this URI").
                setDetails("Access to this URI requires authentication while the client "
                        + "has not provided any authentication credentials. Please, try again "
                        + "using a proper authentication token (you need to login first). HTTP statsus "
                        + "401 The request requires user authentication. The response MUST include a "
                        + "WWW-Authenticate header field (section 14.47) containing a challenge applicable "
                        + "to the requested resource. The client MAY repeat the request with a suitable "
                        + "Authorization header field ").
                setHttpStatus(401).
                build();
    }

    public static ErrorReport remoteError(String remoteUri, ErrorReport remoteException) {
        return ErrorReportBuilder.
                builderRandomUuid().
                setActor(remoteUri).
                setCode("Remote Invocation Error").
                setMessage("Remote invocation error").
                setDetails("HTTP status code 502: The server, while acting as a gateway or "
                        + "proxy, received an invalid response from the upstream server "
                        + "it accessed in attempting to fulfill the request. ").
                setHttpStatus(502).
                setTrace(remoteException).
                build();
    }

}
