/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hampos
 */
public class ArrayCalculation {

    private List<String> colNames;

    private Map<String, List<Object>> values;

    public List<String> getColNames() {
        return colNames;
    }

    public void setColNames(List<String> colNames) {
        this.colNames = colNames;
    }

    public Map<String, List<Object>> getValues() {
        return values;
    }

    public void setValues(LinkedHashMap<String, List<Object>> values) {
        this.values = values;
    }

}
