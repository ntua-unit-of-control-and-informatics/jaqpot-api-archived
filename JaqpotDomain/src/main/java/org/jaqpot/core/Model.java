/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

import java.util.Set;

/**
 *
 * @author chung
 */
public class Model extends JaqpotCoreComponent {
    
    private Set<String> dependentFeatures;
    private Set<String> independentFeatures;
    private Set<String> predictedFeatures;
    private User creator;
    private int reliability = 0;
    private ActualModel actualModel;
    private String datasetUri;
    private Set<Parameter> parameters;
    private String algorithmUri;
    
    
    
}
