/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.Threshold;

/**
 * <p>Abstract BaseThresholdDefConfigWrapper class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public abstract class BaseThresholdDefConfigWrapper {
    Basethresholddef m_baseDef;
    
    /**
     * <p>Constructor for BaseThresholdDefConfigWrapper.</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    protected BaseThresholdDefConfigWrapper(Basethresholddef baseDef) {
        m_baseDef=baseDef;
    }
    
    /**
     * <p>getConfigWrapper</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     * @return a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     * @throws org.opennms.netmgt.threshd.ThresholdExpressionException if any.
     */
    public static BaseThresholdDefConfigWrapper getConfigWrapper(Basethresholddef baseDef) throws ThresholdExpressionException {
        if(baseDef instanceof Threshold) {
            return new ThresholdConfigWrapper((Threshold)baseDef);
        } else if(baseDef instanceof Expression) {
            return new ExpressionConfigWrapper((Expression)baseDef);
        }
        return null;
    }
    
    /**
     * <p>getDatasourceExpression</p>
     *
     * @return a descriptive string for the data source - typically either a data source name, or an expression of data source names
     */
    public abstract String getDatasourceExpression();
    
    /**
     * Returns the names of the datasources required to evaluate this threshold
     *
     * @return Collection of the names of datasources
     */
    public abstract Collection<String> getRequiredDatasources();
    
    /**
     * Evaluate the threshold expression/datasource in terms of the named values supplied, and return that value
     *
     * @param values named values to use in evaluating the expression/data source
     * @return the value of the evaluated expression
     * @throws org.opennms.netmgt.threshd.ThresholdExpressionException if any.
     */
    public abstract double evaluate(Map<String, Double> values)  throws ThresholdExpressionException;
    
    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_baseDef.getDsType();
    }
    
    /**
     * <p>getDsLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsLabel() {
        return m_baseDef.getDsLabel();
    }
    
    /**
     * <p>getRearm</p>
     *
     * @return a double.
     */
    public double getRearm() {
        return m_baseDef.getRearm();
    }
    
    /**
     * <p>getTrigger</p>
     *
     * @return a int.
     */
    public int getTrigger() {
        return m_baseDef.getTrigger();
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_baseDef.getType();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a double.
     */
    public double getValue() {
        return m_baseDef.getValue();
    }
    
    /**
     * <p>hasRearm</p>
     *
     * @return a boolean.
     */
    public boolean hasRearm() {
        return m_baseDef.hasRearm();
    }
    
    /**
     * <p>hasTrigger</p>
     *
     * @return a boolean.
     */
    public boolean hasTrigger() {
        return m_baseDef.hasTrigger();
    }
    
    /**
     * <p>hasValue</p>
     *
     * @return a boolean.
     */
    public boolean hasValue() {
        return m_baseDef.hasValue();
    }
    
    /**
     * <p>getTriggeredUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTriggeredUEI() {
        return m_baseDef.getTriggeredUEI();
    }
    
    /**
     * <p>getRearmedUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRearmedUEI() {
        return m_baseDef.getRearmedUEI();
    }
    
    /**
     * <p>getBasethresholddef</p>
     *
     * @return a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    public Basethresholddef getBasethresholddef() {
        return m_baseDef;
    }

    /*
     * Threshold merging config will use this to check if a new configuration is the same as other.
     * The rule will be, if the threshold type (i.e., hiqh, low, relative), the datasource type, and
     * the threshold expression matches, they are the same, even if they have different triger/rearm
     * values.
     */
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof BaseThresholdDefConfigWrapper)) return false;
        BaseThresholdDefConfigWrapper o = (BaseThresholdDefConfigWrapper)obj;
        return getType().equals(o.getType())
        && getDsType().equals(o.getDsType())
        && getDatasourceExpression().equals(o.getDatasourceExpression());
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(97, 3)
            .append(m_baseDef)
            .toHashCode();
    }

    /*
     * Returns true only if parameter object has exactly the same values for all attributes of
     * the current object.
     */
    /**
     * <p>identical</p>
     *
     * @param o a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     * @return a boolean.
     */
    public boolean identical(BaseThresholdDefConfigWrapper o) {
        return equals(o)
        && (getDsLabel() == o.getDsLabel() || (getDsLabel() != null && getDsLabel().equals(o.getDsLabel())))
        && (getTriggeredUEI() == o.getTriggeredUEI() || (getTriggeredUEI() != null && getTriggeredUEI().equals(o.getTriggeredUEI())))
        && (getRearmedUEI() ==  o.getRearmedUEI() || (getRearmedUEI() != null && getRearmedUEI().equals(o.getRearmedUEI())))
        && getValue() == o.getValue()
        && getRearm() == o.getRearm()
        && getTrigger() == o.getTrigger()
        && getBasethresholddef().getFilterOperator().equals(o.getBasethresholddef().getFilterOperator())
        && Arrays.equals(getBasethresholddef().getResourceFilter(), o.getBasethresholddef().getResourceFilter());
    }
    
    /**
     * <p>merge</p>
     *
     * @param threshold a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public void merge(BaseThresholdDefConfigWrapper threshold) {
        m_baseDef = threshold.getBasethresholddef();
    }
    
}

