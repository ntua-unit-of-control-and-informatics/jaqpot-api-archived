/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hampos
 */
@XmlRootElement(name = "Report")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report extends JaqpotEntity {

    private Map<String, Object> singleCalculations;

    private Map<String, ArrayCalculation> arrayCalculations;

    private Map<String, String> figures;

    public Map<String, Object> getSingleCalculations() {
        return singleCalculations;
    }

    public void setSingleCalculations(LinkedHashMap<String, Object> singleCalculations) {
        this.singleCalculations = singleCalculations;
    }

    public Map<String, ArrayCalculation> getArrayCalculations() {
        return arrayCalculations;
    }

    public void setArrayCalculations(LinkedHashMap<String, ArrayCalculation> arrayCalculations) {
        this.arrayCalculations = arrayCalculations;
    }

    public Map<String, String> getFigures() {
        return figures;
    }

    public void setFigures(LinkedHashMap<String, String> figures) {
        this.figures = figures;
    }
    
    public static void main(String[] args) throws JsonProcessingException{
        
        Report report = new Report();
        
        LinkedHashMap<String,Object> single = new LinkedHashMap<>();
        single.put("calculation1", 325.15);
        single.put("calculation2", "A");
        single.put("calculation3", "whatever");
        single.put("calculation4", 15);
        
        LinkedHashMap<String,ArrayCalculation> arrays = new LinkedHashMap<>();
        
        ArrayCalculation a1 = new ArrayCalculation();
        a1.setColNames(Arrays.asList("column A","column B", "column C"));
        LinkedHashMap<String,List<Object>> v1 = new LinkedHashMap<>();
        v1.put("row1", Arrays.asList(5.0,1,30));
        v1.put("row2", Arrays.asList(6.0,12,34));
        v1.put("row3", Arrays.asList(7.0,11,301));       
        a1.setValues(v1);
        
        ArrayCalculation a2 = new ArrayCalculation();
        a2.setColNames(Arrays.asList("column 1","column 2", "column 3"));
        LinkedHashMap<String,List<Object>> v2 = new LinkedHashMap<>();
        v2.put("row1", Arrays.asList(5.0,1,30));
        v2.put("row2", Arrays.asList(6.0,12,34));
        v2.put("row3", Arrays.asList(7.0,11,301));       
        a2.setValues(v2);
        
        arrays.put("calculation 5", a1);
        arrays.put("calcluation 6", a2);
        
        
        LinkedHashMap<String,String> figures = new LinkedHashMap<>();
        figures.put("figure1", "fa9ifj2ifjaspldkfjapwodfjaspoifjaspdofijaf283jfo2iefj");
        figures.put("figure2", "1okwejf-o2eifj-2fij2e-fijeflksdjfksdjfpskdfjspdokfjsdpf");
        
        
        report.setSingleCalculations(single);
        report.setArrayCalculations(arrays);
        report.setFigures(figures);
        
        ObjectMapper mapper = new ObjectMapper();
        
        String reportString = mapper.writeValueAsString(report);
        
        System.out.println(reportString);
        
        
    }

}
