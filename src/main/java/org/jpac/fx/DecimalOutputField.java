/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalOutputField.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpac.fx;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.jpac.DecimalValue;

/**
 *
 * @author berndschuster
 */
public class DecimalOutputField extends OutputField{
    public static final int    DEFAULTPRECISION  = 2;
    public static final double BIGNUMBER         = 1.0E6;
    
    protected int           precision;
    protected double        multi;
    protected NumberFormat  numberFormat;
    protected DecimalFormat bigNumberFormat;
    
    public DecimalOutputField(){
        this(NONE);
    }
    
    public DecimalOutputField(String identifier){
        super(identifier);
        this.numberFormat = NumberFormat.getInstance();
        this.numberFormat.setGroupingUsed(false);
        setPrecision(DEFAULTPRECISION);
        this.bigNumberFormat = new DecimalFormat("#0.0#E0");
    }    
    
    @Override
    protected String formatText(){
        String formattedValue;
        if (!valid){
            formattedValue = "";
        } else{
        	double dval    = ((DecimalValue)value).get();
        	if(Math.abs(dval) > BIGNUMBER) {
        		formattedValue = bigNumberFormat.format(dval);
        	} else {
        		formattedValue = numberFormat.format(dval);
        	}
        }
        return formattedValue; 
    };

    @Override
    protected Connector instantiateConnector(String qualifiedIdentifier) {
        return new DecimalConnector(qualifiedIdentifier);
    }

    public void setPrecision(int precision){
        this.precision = precision;
        this.numberFormat.setMaximumFractionDigits(precision);
    }

    public int getPrecision(int precision){
        return this.precision;
    }    
}
