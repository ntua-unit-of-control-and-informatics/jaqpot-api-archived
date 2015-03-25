/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.algorithm.model;

import java.io.Serializable;

/**
 *
 * @author hampos
 */
public class LeverageModel implements Serializable {

    double[][] omega;
    double gamma;

    public LeverageModel() {
    }

    public double[][] getOmega() {
        return omega;
    }

    public void setOmega(double[][] omega) {
        this.omega = omega;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

}
