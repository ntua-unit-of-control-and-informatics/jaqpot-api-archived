package org.jaqpot.core.search.dto;

public class Total {
 
    private Integer value;
    private String relation;

    public void setValue(Integer value){
        this.value = value;
    }

    public Integer getValue(){
        return this.value;
    }

    public void setRelation(String relation){
        this.relation = relation;
    }

    public String gerRelation(){
        return this.relation;
    }

}
