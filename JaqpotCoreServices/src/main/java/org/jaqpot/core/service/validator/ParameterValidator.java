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
package org.jaqpot.core.service.validator;

import org.apache.commons.lang3.StringUtils;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Parameter;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.service.exceptions.parameter.ParameterRangeException;
import org.jaqpot.core.service.exceptions.parameter.ParameterScopeException;
import org.jaqpot.core.service.exceptions.parameter.ParameterTypeException;

import java.util.*;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.jaqpot.core.annotations.Jackson;

/**
 * @author Angelos Valsamis
 * @author Charalampos Chomenidis
 */
@Dependent
public class ParameterValidator {

    public enum Type {
        NUMERIC,
        STRING,
        NUMERIC_ARRAY,
        STRING_ARRAY,
        UNDEFINED
    }

    private final JSONSerializer serializer;

    @Inject
    public ParameterValidator(@Jackson JSONSerializer serializer) {
        this.serializer = serializer;
    }

    public void validateDataset(Dataset dataset, List<String> requiredFeatures) {
        if (dataset.getFeatures() == null) {
            throw new IllegalArgumentException("Input dataset does not have features");
        }
        HashSet<String> features = dataset.getFeatures().stream().map(FeatureInfo::getURI).collect(Collectors.toCollection(HashSet::new));

        if (!features.containsAll(requiredFeatures)) {
            throw new IllegalArgumentException("Dataset is not compatible with model");
        }
    }

    public void validate(String input, Set<Parameter> parameters) throws ParameterTypeException, ParameterRangeException, ParameterScopeException {

        Map<String, Object> parameterMap = null;
        if (input != null) {
            parameterMap = serializer.parse(input, new HashMap<String, Object>().getClass());
        }

        //For each mandatory parameter in stored algorithm, check if it exists in user input
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.getScope().equals(Parameter.Scope.MANDATORY) && (parameterMap == null || !parameterMap.containsKey(parameter.getId()))) {
                    throw new ParameterScopeException("Parameter with id: '" + parameter.getId() + "' is mandatory.");
                }
            }
        }

        //For each parameter in set
        if (parameterMap != null && parameters != null) {
            for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                String parameterId = entry.getKey();

                Parameter parameter = parameters.stream()
                        .filter(p -> p.getId().equals(parameterId))
                        .findFirst().orElseThrow(() -> new ParameterTypeException("Could not recognise parameter with id:" + parameterId));
                Object value = entry.getValue();

                //Get type of algorithm's parameter
                Type typeOfParameter = Type.UNDEFINED;
                if (isNumeric(parameter.getValue().toString())) {
                    typeOfParameter = Type.NUMERIC;
                } else if (StringUtils.isAlphanumericSpace(parameter.getValue().toString())) {
                    typeOfParameter = Type.STRING;
                } else if (parameter.getValue() instanceof Collection) {
                    typeOfParameter = getTypeOfCollection((Collection) parameter.getValue());
                }

                if (typeOfParameter == Type.UNDEFINED) {
                    continue; //parameter is of a type that we cannot validate.
                }
                //Get type of user's value and validate
                switch (typeOfParameter) {
                    case NUMERIC:
                        if (isNumeric(value.toString())) {
                            if (parameter.getAllowedValues() != null) {
                                checkAllowedValues(parameterId, value, parameter.getAllowedValues());
                            }
                            if (parameter.getMinValue() != null && isNumeric(parameter.getMinValue().toString())) {
                                checkIsLessThan(parameterId, Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMinValue().toString())));
                            }
                            if (parameter.getMaxValue() != null && isNumeric(parameter.getMaxValue().toString())) {
                                checkIsGreaterThan(parameterId, Double.parseDouble(value.toString()), (Double.parseDouble(parameter.getMaxValue().toString())));
                            }
                        } else {
                            throw new ParameterTypeException("Parameter with id:" + parameterId + " must be Numeric");
                        }
                        break;
                    case STRING:
                        if (StringUtils.isAlphanumericSpace(value.toString())) {
                            if (parameter.getAllowedValues() != null) {
                                checkAllowedValues(parameterId, value.toString(), parameter.getAllowedValues());
                            }
                            if (parameter.getMinValue() != null && isNumeric(parameter.getMinValue().toString())) {
                                checkIsLessThan(parameterId, value.toString(), parameter.getMinValue().toString());
                            }
                            if (parameter.getMaxValue() != null && isNumeric(parameter.getMaxValue().toString())) {
                                checkIsGreaterThan(parameterId, value.toString(), parameter.getMaxValue().toString());
                            }
                        } else {
                            throw new ParameterTypeException("Parameter with id: '" + parameterId + "' must be Alphanumeric");
                        }
                        break;
                    case NUMERIC_ARRAY:
                        if ((value instanceof Collection && getTypeOfCollection((Collection) value) == Type.NUMERIC_ARRAY)) {
                            if (parameter.getAllowedValues() != null) {
                                checkAllowedValues(parameterId, value, parameter.getAllowedValues());
                            }
                            checkMinMaxSize(parameterId, (Collection) value, parameter.getMinArraySize(), parameter.getMaxArraySize());
                            for (Object o : (Collection) value) {
                                if (parameter.getMinValue() != null && isNumeric(parameter.getMinValue().toString())) {
                                    checkIsLessThan(parameterId, Double.parseDouble(o.toString()), (Double.parseDouble(parameter.getMinValue().toString())));
                                }
                                if (parameter.getMaxValue() != null && isNumeric(parameter.getMaxValue().toString())) {
                                    checkIsGreaterThan(parameterId, Double.parseDouble(o.toString()), (Double.parseDouble(parameter.getMaxValue().toString())));
                                }
                            }
                        } else {
                            throw new ParameterTypeException("Parameter with id: '" + parameterId + "' must be Array of numeric values");
                        }
                        break;
                    case STRING_ARRAY:
                        if ((value instanceof Collection && getTypeOfCollection((Collection) value) == Type.STRING_ARRAY)) {
                            if (parameter.getAllowedValues() != null) {
                                checkAllowedValues(parameterId, value, parameter.getAllowedValues());
                            }
                            checkMinMaxSize(parameterId, (Collection) value, parameter.getMinArraySize(), parameter.getMaxArraySize());
                            for (Object o : (Collection) value) {
                                if (parameter.getMinValue() != null && isNumeric(parameter.getMinValue().toString())) {
                                    checkIsLessThan(parameterId, o.toString(), parameter.getMinValue().toString());
                                }
                                if (parameter.getMaxValue() != null && isNumeric(parameter.getMaxValue().toString())) {
                                    checkIsGreaterThan(parameterId, o.toString(), parameter.getMaxValue().toString());
                                }
                            }
                        } else {
                            throw new ParameterTypeException("Parameter with id: '" + parameterId + "' must be Array of alphanumeric values");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static <T> boolean checkAllowedValues(String parameterId, T value, List<T> elements) throws ParameterRangeException {
        if (value != null) {
            if (elements != null) {
                for (T o : elements) {
                    if (o.equals(value)) {
                        return true;
                    }
                }
                throw new ParameterRangeException("Parameter with id: '" + parameterId + "' has a value not found in allowed values.");
            }
        }
        return true;
    }

    private static <T extends Comparable<? super T>> Boolean checkIsLessThan(String parameterId, T value, T minimum) throws ParameterRangeException {
        if (minimum != null && isNumeric(minimum.toString())) {
            if (value.compareTo(minimum) < 0) {
                throw new ParameterRangeException("Parameter with id: '" + parameterId + "' has a value less than the parameter's allowed minimum");
            }
        }
        return true;
    }

    private static <T extends Comparable<? super T>> Boolean checkIsGreaterThan(String parameterId, T value, T maximum) throws ParameterRangeException {
        if (maximum != null) {
            if (value.compareTo(maximum) > 0) {
                throw new ParameterRangeException("Parameter with id: '" + parameterId + "' has a value greater than the parameter's allowed maximum");
            }
        }
        return true;
    }

    private static Boolean checkMinMaxSize(String parameterId, Collection collection, Integer minSize, Integer maxSize) throws ParameterRangeException {
        if (minSize != null && isNumeric(minSize.toString())) {
            if (collection.size() < minSize) {
                throw new ParameterRangeException("Parameter with id: '" + parameterId + "' has an array size lees than the parameter's allowed minimum array size");
            }
        }
        if (maxSize != null && isNumeric(maxSize.toString())) {
            if (collection.size() > maxSize) {
                throw new ParameterRangeException("Parameter with id: '" + parameterId + "' has an array size greater than the parameter's allowed maximum array size");
            }
        }
        return true;
    }

    //Returns if array is a (consistent) collection of Strings (Type.STRING) or Numbers (Type.NUMERIC).
    //else returns Type.UNDEFINED
    private static Type getTypeOfCollection(Collection collection) {
        Type content = null;
        for (Object value : collection) {
            if (!isNumeric(value.toString())) {
                if (!StringUtils.isAlphanumericSpace(value.toString())) {
                    return Type.UNDEFINED;
                } else if (content == Type.NUMERIC_ARRAY) {
                    return Type.UNDEFINED;
                } else {
                    content = Type.STRING_ARRAY;
                }
            } else if (content == Type.STRING_ARRAY) {
                return Type.UNDEFINED;
            } else {
                content = Type.NUMERIC_ARRAY;
            }
        }
        return content;
    }

    //Probably most performant solution to check for isNumeric, according to discussion here
    //http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java/1102916#1102916
    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
