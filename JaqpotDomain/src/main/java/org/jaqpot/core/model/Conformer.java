/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Map;

/**
 *
 * @author chung
 */
public class Conformer extends Substance {

    private Map<String, String> representations; //e.g., SMILES --> c1ccccc1 
    private String fatherCompound;
    private BibTeX bibtex;

}