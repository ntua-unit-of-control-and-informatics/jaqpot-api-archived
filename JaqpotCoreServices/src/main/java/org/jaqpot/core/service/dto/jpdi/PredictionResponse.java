/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.jpdi;

import java.util.List;

/**
 *
 * @author hampos
 */
public class PredictionResponse {

    List<Object> predictions;

    public List<Object> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Object> predictions) {
        this.predictions = predictions;
    }

}
