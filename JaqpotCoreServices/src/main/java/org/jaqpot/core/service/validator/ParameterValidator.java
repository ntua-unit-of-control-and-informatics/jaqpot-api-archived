/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.validator;

import org.apache.commons.lang3.StringUtils;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Parameter;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import javax.ws.rs.BadRequestException;
import java.util.*;
import java.util.stream.Collectors;


/**
 *
 * @author hampos
 */
public class ParameterValidator {

    public enum Type {
        NUMERIC,
        STRING,
        NUMERIC_ARRAY,
        STRING_ARRAY,
        UNDEFINED
    }

    private final JSONSerializer serializer;

    public ParameterValidator(JSONSerializer serializer) {
        this.serializer = serializer;
    }

    public ParameterValidator() {
        serializer = null;
    }

    public void validateDataset(Dataset dataset, List<String> requiredFeatures) {
        if (dataset.getFeatures() == null || dataset.getFeatures().isEmpty())
            throw new IllegalArgumentException("Resulting dataset is empty");
        HashSet<String> features = dataset.getFeatures().stream().map(FeatureInfo::getURI).collect(Collectors.toCollection(HashSet::new));

        if (!features.containsAll(requiredFeatures))
            throw new IllegalArgumentException("Dataset is not compatible with model");
    }

    //TODO add scope check in validation logic
    public void validate(String input, Set<Parameter> parameters) {

        Map<String, Object> parameterMap = serializer.parse(input, new HashMap<String, Object>().getClass());
        //For each parameter in set
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            String parameterId = entry.getKey();
            Parameter parameter = parameters.stream()
                    .filter(p -> p.getId().equals(parameterId))
                    .findFirst().orElseThrow(() -> new BadRequestException("Could not recognise parameter with id:" + parameterId));
            Object value = entry.getValue();

            //Get type of algorithm's parameter
            Type typeOfParameter = Type.UNDEFINED;
            if (isNumeric(parameter.getValue().toString()))
                typeOfParameter = Type.NUMERIC;
            else if (StringUtils.isAlphanumericSpace(parameter.getValue().toString()))
                typeOfParameter = Type.STRING;
            else if (parameter.getValue() instanceof Collection)
                typeOfParameter = getTypeOfCollection((Collection) parameter.getValue());

            if (typeOfParameter==Type.UNDEFINED)
                continue; //parameter is of a type that we cannot validate.

            //Get type of user's value and validate
            switch (typeOfParameter) {
                case NUMERIC:
                    if (isNumeric(value.toString())) {
                        if (parameter.getAllowedValues()!=null)
                            checkAllowedValues(Double.parseDouble(value.toString()), (List<Double>) (List<?>) parameter.getAllowedValues());
                        if (parameter.getMinValue()!=null)
                            checkIsLessThan(Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMinValue().toString())));
                        if (parameter.getMaxValue()!=null)
                            checkIsGreaterThan(Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMaxValue().toString())));
                    }
                    else
                        throw new BadRequestException("Parameter with id:" + parameterId + " should be Numeric");
                    break;
                case STRING:
                    if (StringUtils.isAlphanumericSpace(value.toString())) {
                        checkAllowedValues(value.toString(), (List<String>) (List<?>) parameter.getAllowedValues());
                        if (parameter.getMinValue()!=null)
                            checkIsLessThan(value.toString(), parameter.getMinValue().toString());
                        if (parameter.getMaxValue()!=null)
                            checkIsGreaterThan(value.toString(), parameter.getMaxValue().toString());
                    }
                    else
                        throw new BadRequestException("Parameter with id:" + parameterId + " should be Alphanumeric");
                    break;
                case NUMERIC_ARRAY:
                    if ((value instanceof Collection && getTypeOfCollection((Collection) value) == Type.NUMERIC_ARRAY)) {
                        checkMinMaxSize((Collection) value, parameter.getMinArraySize(), parameter.getMaxArraySize());
                        for (Object o : (Collection) value) {
                            checkAllowedValues(Double.parseDouble(o.toString()), (List<Double>) (List<?>) parameter.getAllowedValues());
                            if (parameter.getMinValue()!=null)
                                checkIsLessThan(Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMinValue().toString())));
                            if (parameter.getMaxValue()!=null)
                                checkIsGreaterThan(Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMaxValue().toString())));
                        }
                    }
                    else
                        throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of numeric values");
                    break;
                case STRING_ARRAY:
                    if ((value instanceof Collection && getTypeOfCollection((Collection) value) == Type.STRING_ARRAY)) {
                        checkMinMaxSize((Collection) value, parameter.getMinArraySize(), parameter.getMaxArraySize());
                        for (Object o : (Collection) value) {
                            checkAllowedValues(o.toString(), (List<String>) (List<?>) parameter.getAllowedValues());
                            if (parameter.getMinValue()!=null)
                                checkIsLessThan(value.toString(),parameter.getMinValue().toString());
                            if (parameter.getMaxValue()!=null)
                                checkIsGreaterThan(value.toString(),parameter.getMaxValue().toString());
                        }
                    }
                    else
                        throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of alphanumeric values");
                    break;
                default:
                    break;

            }
        }
    }

    static <T> boolean  checkAllowedValues(T value, List<T> elements) {
        if (value!=null)
            if (elements!=null) {
                for (T o : elements) {
                    if (o.equals(value))
                        return true;
                }
                return false;
            }
        return true;
    }

    static <T extends Comparable<? super T>> Boolean  checkIsLessThan (T value, T minimum) {
        if (minimum != null)
            if (value.compareTo(minimum)<0)
                return false;
        return true;
    }

    static <T extends Comparable<? super T>> Boolean  checkIsGreaterThan (T value, T maximum) {
        if (maximum != null)
            if (value.compareTo(maximum)>0)
                return false;
        return true;
    }

    static Boolean checkMinMaxSize(Collection collection, Integer minSize, Integer maxSize){
        if (minSize!=null)
            if (collection.size()<minSize)
                return false;
        if (maxSize!=null)
            if (collection.size()>maxSize)
                return false;
        return true;
    }

    //Returns if array is a (consistent) collection of Strings (Type.STRING) or Numbers (Type.NUMERIC).
    //else returns Type.UNDEFINED
    static Type getTypeOfCollection(Collection collection)
    {
        Type content = null;
        for (Object value:collection)
        {
            if(!isNumeric(value.toString()))
                if (!StringUtils.isAlphanumericSpace(value.toString()))
                    return Type.UNDEFINED;
                else
                    if (content == Type.NUMERIC_ARRAY)
                        return Type.UNDEFINED;
                    else
                        content=Type.STRING_ARRAY;
            else
                if (content == Type.STRING_ARRAY)
                    return Type.UNDEFINED;
            else
                    content=Type.NUMERIC_ARRAY;
        }
        return content;
    }

    //Probably most performant solution to check for isNumeric, according to discussion here
    //http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java/1102916#1102916
    static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
