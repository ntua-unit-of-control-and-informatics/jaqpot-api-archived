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

    public void validate(String input, Set<Parameter> parameters) {

        Map<String, Object> parameterMap = serializer.parse(input, new HashMap<String, Object>().getClass());
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            String parameterId = entry.getKey();
            Parameter parameter = parameters.stream()
                    .filter(p -> p.getId().equals(parameterId))
                    .findFirst().orElseThrow(() -> new BadRequestException("Could not recognise parameter with id:" + parameterId));
            Object value = entry.getValue();

            if(!isNumeric(value.toString()) && isNumeric(parameter.getValue().toString()))
                throw new BadRequestException("Parameter with id:" + parameterId + " should be Numeric");
            if(!StringUtils.isAlphanumericSpace(value.toString()) && StringUtils.isAlphanumericSpace(parameter.getValue().toString()))
                throw new BadRequestException("Parameter with id:" + parameterId + " should be Alphanumeric");
            if(!(value instanceof Collection) && parameter instanceof Collection)
                throw new BadRequestException("Parameter with id:" + parameterId + " should be Alphanumeric");
            else if ((value instanceof Collection) && (parameter instanceof Collection))
            {
                Type typeOfValue = getTypeOfCollection((Collection)value);
                Type typeOfParameter = getTypeOfCollection((Collection)parameter.getValue());

                if (typeOfParameter == Type.NUMERIC && typeOfValue!=Type.NUMERIC)
                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of numeric values");
                if (typeOfParameter == Type.STRING && typeOfValue!=Type.STRING)
                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of alphanumeric values");
            }


              /*  if (parameter.getType() != null) {
                    switch (parameter.getType()) {
                        case CONTINUOUS:
                            if (!(value instanceof Double || value instanceof Float)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Continuous");
                            }
                            break;
                        case CATEGORICAL:
                            if (!(value instanceof String)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Categorical");
                            }
                            break;
                        case DISCRETE:
                            if (!(value instanceof Integer || value instanceof Long)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Discrete");
                            }
                            break;
                        case BOOLEAN:
                            if (!(value instanceof Boolean)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Boolean");
                            }
                            break;
                        case ARRAY_CONTINUOUS:
                            if (!(value instanceof Collection)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Array");
                            }
                            Collection valueCollection = (Collection) value;
                            if (parameter.getMinArraySize() != null) {
                                if (valueCollection.size() < parameter.getMinArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with minimum size:" + parameter.getMinArraySize());
                                }
                            }
                            if (parameter.getMaxArraySize() != null) {
                                if (valueCollection.size() > parameter.getMaxArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with maximum size:" + parameter.getMaxArraySize());
                                }
                            }
                            for (Object v : valueCollection) {
                                if (!(v instanceof Double || v instanceof Float)) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of Continuous values");
                                }
                            }
                            break;
                        case ARRAY_CATEGORICAL:
                            if (!(value instanceof Collection)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Array");
                            }
                            valueCollection = (Collection) value;
                            if (parameter.getMinArraySize() != null) {
                                if (valueCollection.size() < parameter.getMinArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with minimum size:" + parameter.getMinArraySize());
                                }
                            }
                            if (parameter.getMaxArraySize() != null) {
                                if (valueCollection.size() > parameter.getMaxArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with maximum size:" + parameter.getMaxArraySize());
                                }
                            }
                            for (Object v : valueCollection) {
                                if (!(v instanceof String)) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of Categorical values");
                                }
                            }
                            break;
                        case ARRAY_DISCRETE:
                            if (!(value instanceof Collection)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Array");
                            }
                            valueCollection = (Collection) value;
                            if (parameter.getMinArraySize() != null) {
                                if (valueCollection.size() < parameter.getMinArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with minimum size:" + parameter.getMinArraySize());
                                }
                            }
                            if (parameter.getMaxArraySize() != null) {
                                if (valueCollection.size() > parameter.getMaxArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with maximum size:" + parameter.getMaxArraySize());
                                }
                            }
                            for (Object v : valueCollection) {
                                if (!(v instanceof Integer || v instanceof Long)) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of Discrete values");
                                }
                            }
                            break;
                        case ARRAY_BOOLEAN:
                            if (!(value instanceof Collection)) {
                                throw new BadRequestException("Parameter with id:" + parameterId + " should be Array");
                            }
                            valueCollection = (Collection) value;
                            if (parameter.getMinArraySize() != null) {
                                if (valueCollection.size() < parameter.getMinArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with minimum size:" + parameter.getMinArraySize());
                                }
                            }
                            if (parameter.getMaxArraySize() != null) {
                                if (valueCollection.size() > parameter.getMaxArraySize()) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array with maximum size:" + parameter.getMaxArraySize());
                                }
                            }
                            for (Object v : valueCollection) {
                                if (!(v instanceof Boolean)) {
                                    throw new BadRequestException("Parameter with id:" + parameterId + " should be Array of Boolean values");
                                }
                            }
                            break;
                    }
                }
                if (parameter.getAllowedValues() != null && !parameter.getAllowedValues().isEmpty()) {
                    if (!parameter.getAllowedValues().contains(value)) {
                        throw new BadRequestException("Parameter with id:" + parameterId + " is not one of the allowed values");
                    }
                }
            }
        }
        catch (JaqpotSerializationException ex) {
            throw new BadRequestException(ex);
        }*/

        }
    }

    //Returns if array is a (consistent) collection of Strings (Type.STRING) or Numbers (Type.NUMERIC).
    //else returns Type.UNDEFINED
    Type getTypeOfCollection(Collection collection)
    {
        Type content = null;
        for (Object value:collection)
        {
            if(!isNumeric(value.toString()))
                if (!StringUtils.isAlphanumericSpace(value.toString()))
                    return Type.UNDEFINED;
                else
                    if (content == Type.NUMERIC)
                        return Type.UNDEFINED;
                    else
                        content=Type.STRING;
            else
                if (content == Type.STRING)
                    return Type.UNDEFINED;
            else
                    content=Type.NUMERIC;
        }
        return content;
    }

    //Probably most performant solution to check for isNumeric, according to discussion here
    //http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java/1102916#1102916
    public static boolean isNumeric(String str)
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
