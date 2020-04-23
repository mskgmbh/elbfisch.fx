/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalInputField.java
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
import java.text.ParsePosition;
import javax.swing.InputVerifier;
import org.jpac.Decimal;
import org.jpac.DecimalValue;
import org.jpac.NumberOutOfRangeException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;

/**
 *
 * @author berndschuster
 */
public class DecimalInputField extends InputField{
    public static final double BIGNUMBER  = 1.0E6;
    
    protected InputVerifier inputVerifier;
    protected NumberFormat  numberFormat; 
    protected DecimalFormat bigNumberFormat;
    protected ParsePosition parsePosition;

    public DecimalInputField(){
        super();
        this.numberFormat = NumberFormat.getInstance();  
        this.numberFormat.setGroupingUsed(false);
        this.bigNumberFormat = new DecimalFormat("#0.0#E0");        
        this.parsePosition = new ParsePosition(0);
        this.defaultToolTipText  = "enter a decimal value";
        setText("");
        toolTip.setText(defaultToolTipText);
    }
    
    public DecimalInputField(String identifier) {
        this();
        this.identifier   = identifier; 
    }    
    
    @Override
    protected String formatText(){
        String formattedValue;
        if (!signalsValueValid){
            formattedValue = "";
        }
        else{
        	double dval    = ((DecimalValue)signalsValue).get();
        	if(Math.abs(dval) > BIGNUMBER) {
        		formattedValue = bigNumberFormat.format(dval);
        	} else {
        		formattedValue = numberFormat.format(dval);
        	}
        }
        return formattedValue; 
    };

    @Override
    protected void setAssignedSignal() throws SignalAccessException, NumberOutOfRangeException{
        if (isConfirmable()){
            try{((Decimal)assignedSignal).set(((DecimalValue)enteredValue).get());}catch(Exception exc){/*cannot happen*/}
        }
    }
    
    @Override
    protected Connector instantiateConnector(String qualifiedIdentifier) {
        return new DecimalConnector(qualifiedIdentifier);
    }

    @Override
    public void evaluateEnteredValue() {
        double  localValue      = 0.0;
        boolean localValueValid = false;
        //check, if the user has entered a valid decimal value
        try{
            parsePosition.setIndex(0);
            String text   = getText().trim();
            Number number = numberFormat.parse(text, parsePosition);
            if (number != null && parsePosition.getIndex() == text.length()){
                localValue      = number.doubleValue();
                localValueValid = true;
            }
        }
        catch(Exception exc){
            localValueValid = false;
        }
        //check, if the Decimal signal this input field is connected to is restricted to a certain range of values
        if (localValueValid && connected && ((Decimal)assignedSignal).isRangeChecked()){
            localValueValid = localValue <= ((Decimal)assignedSignal).getMaxValue() && localValue >= ((Decimal)assignedSignal).getMinValue();
        }
        enteredValueValid = localValueValid;
        if (enteredValue == null){
            enteredValue = new DecimalValue();
        }
        ((DecimalValue)enteredValue).set(enteredValueValid ? localValue : 0.0);
    }
    
    /**
     * returns the maximum number of fraction digits
     * @return 
     */
    public int getMaximumFractionDigits(){
        return numberFormat.getMaximumFractionDigits();
    }

    /**
     * sets the maximum number of fraction digits 
     * @param maximumFractionDigits 
     */
    public void setMaximumFractionDigits(int maximumFractionDigits){
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);
    }
    
    /**
     * returns the minimum number of fraction digits
     * @return 
     */
    public int getMinimumFractionDigits(){
        return numberFormat.getMinimumFractionDigits();
    }

    /**
     * sets the minimum number of fraction digits 
     * @param minimumFractionDigits 
     */
    public void setMinimumFractionDigits(int minimumFractionDigits){
        numberFormat.setMinimumFractionDigits(minimumFractionDigits);
    }
    
    /**
     * returns the maximum number of integer digits
     * @return 
     */
    public int getMaximumIntegerDigits(){
        return numberFormat.getMaximumIntegerDigits();
    }
    
    /**
     * used to set the maximum number of integer digits
     * @param maximumIntegerDigits 
     */
    public void setMaximumIntegerDigits(int maximumIntegerDigits){
        numberFormat.setMaximumIntegerDigits(maximumIntegerDigits);
    }

    /**
     * returns the minimum number of integer digits
     * @return 
     */
    public int getMinimumIntegerDigits(){
        return numberFormat.getMinimumIntegerDigits();
    }

    /**
     * used to set the maximum number of integer digits
     * @param minimumIntegerDigits 
     */
    public void setMinimumIntegerDigits(int minimumIntegerDigits){
        numberFormat.setMinimumIntegerDigits(minimumIntegerDigits);
    }

    @Override
    protected String getSignalDependentToolTipText(Signal signal) {
        String toolTipText = defaultToolTipText;
        if (((Decimal)signal).isRangeChecked()){
            toolTipText = numberFormat.format(((Decimal)signal).getMinValue()) + " <= " + defaultToolTipText + " >= " + numberFormat.format(((Decimal)signal).getMaxValue());
        }
        return toolTipText;
    }
}
