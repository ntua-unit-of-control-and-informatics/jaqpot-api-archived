/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;


/**
 *
 * @author chung
 */
public class ErrorReport extends JaqpotCoreComponent {
    
    private String code;
    private String actor;
    private String message;
    private String details;
    private int httpStatus = 0;
    private ErrorReport errorReportGetter;
}
