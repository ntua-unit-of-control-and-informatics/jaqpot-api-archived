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
package org.jaqpot.core.service.dto.study;

public class Result {

    private String loQualifier;
    private Number loValue;
    private String upQualifier;
    private Number upValue;
    private String unit;
    private String errQualifier;
    private Number errorValue;
    private String textValue;

    public String getLoQualifier() {
        return this.loQualifier;
    }

    public void setLoQualifier(String loQualifier) {
        this.loQualifier = loQualifier;
    }

    public Number getLoValue() {
        return this.loValue;
    }

    public void setLoValue(Number loValue) {
        this.loValue = loValue;
    }

    public String getUpQualifier() {
        return upQualifier;
    }

    public void setUpQualifier(String upQualifier) {
        this.upQualifier = upQualifier;
    }

    public Number getUpValue() {
        return upValue;
    }

    public void setUpValue(Number upValue) {
        this.upValue = upValue;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getErrQualifier() {
        return errQualifier;
    }

    public void setErrQualifier(String errQualifier) {
        this.errQualifier = errQualifier;
    }

    public Number getErrorValue() {
        return errorValue;
    }

    public void setErrorValue(Number errorValue) {
        this.errorValue = errorValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

}
