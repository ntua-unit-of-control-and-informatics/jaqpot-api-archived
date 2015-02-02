/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Set;

/**
 *
 * @author chung
 */
public class Algorithm extends JaqpotEntity {
    
    private Set<Parameter> parameters;
    private int ranking;
    private BibTeX bibtex;
    private User createdBy;
    
}
