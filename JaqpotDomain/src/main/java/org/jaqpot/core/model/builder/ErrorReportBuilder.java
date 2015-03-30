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
package org.jaqpot.core.model.builder;

import java.util.UUID;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class ErrorReportBuilder implements EntityBuilder<ErrorReport> {

    private final ErrorReport errorReport;

    private ErrorReportBuilder(String id) {
        errorReport = new ErrorReport(id);
    }

    public static ErrorReportBuilder builder(String id) {
        return new ErrorReportBuilder(id);
    }

    public static ErrorReportBuilder builderRandomId() {
        ROG rog = new ROG(true);
        return new ErrorReportBuilder("ERR" + rog.nextString(10));
    }

    public ErrorReportBuilder setCode(String code) {
        errorReport.setCode(code);
        return this;
    }

    public ErrorReportBuilder setActor(String actor) {
        errorReport.setActor(actor);
        return this;
    }

    public ErrorReportBuilder setMessage(String message) {
        errorReport.setMessage(message);
        return this;
    }

    public ErrorReportBuilder setDetails(String details) {
        errorReport.setDetails(details);
        return this;
    }

    public ErrorReportBuilder setHttpStatus(Integer httpStatus) {
        errorReport.setHttpStatus(httpStatus);
        return this;
    }

    public ErrorReportBuilder setTrace(ErrorReport trace) {
        errorReport.setTrace(trace);
        return this;
    }

    @Override
    public ErrorReport build() {
        return this.errorReport;
    }

}
