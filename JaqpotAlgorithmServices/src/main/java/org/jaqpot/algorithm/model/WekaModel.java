/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.algorithm.model;

import java.io.Serializable;
import weka.classifiers.Classifier;

/**
 *
 * @author hampos
 */
public class WekaModel implements Serializable {

    Classifier classifier;

    public WekaModel() {
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

}
