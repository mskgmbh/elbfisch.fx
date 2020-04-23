/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerInputField.java
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

import java.text.NumberFormat;
import java.text.ParsePosition;
import org.jpac.NumberOutOfRangeException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignedInteger;
import org.jpac.SignedIntegerValue;

/**
 *
 * @author berndschuster
 */
public class SignedIntegerInputField extends InputField{
    protected NumberFormat  numberFormat;    
    protected ParsePosition parsePosition;
    

    public SignedIntegerInputField(){
        super();
        this.numberFormat = NumberFormat.getInstance();  
        this.numberFormat.setGroupingUsed(false);
        this.numberFormat.setParseIntegerOnly(true);
        this.parsePosition = new ParsePosition(0);        
        this.defaultToolTipText  = "enter an integer value";
        setText("");
        toolTip.setText(defaultToolTipText);
    }
    
    public SignedIntegerInputField(String identifier) {
        this();
        this.identifier = identifier; 
    }        
    
    @Override
    protected String formatText(){
        String formattedValue;
        if (!signalsValueValid){
            formattedValue = "";
        }
        else{
            formattedValue = Integer.toString(((SignedIntegerValue)signalsValue).get());
        }
        return formattedValue; 
    };
    

    @Override
    protected void setAssignedSignal() throws SignalAccessException, NumberOutOfRangeException{
        if (isConfirmable()){
            try{((SignedInteger)assignedSignal).set(((SignedIntegerValue)enteredValue).get());}catch(Exception exc){/*cannot happen*/}
        }
    }
    
    @Override
    protected Connector instantiateConnector(String qualifiedIdentifier) {
        return new SignedIntegerConnector(qualifiedIdentifier);
    }
    
    @Override
    protected String getSignalDependentToolTipText(Signal signal) {
        String toolTipText = defaultToolTipText;
        if (((SignedInteger)signal).isRangeChecked()){
            toolTipText = Integer.toString(((SignedInteger)signal).getMinValue()) + " <= " + defaultToolTipText + " >= " + Integer.toString(((SignedInteger)signal).getMaxValue());
        }
        return toolTipText;
    }

    @Override
    public void evaluateEnteredValue() {
        int     localValue      = 0;
        boolean localValueValid = false;
        //check, if the user has entered a valid decimal value
        try{
            parsePosition.setIndex(0);
            String text   = getText().trim();
            Number number = numberFormat.parse(text, parsePosition);
            if (number != null && parsePosition.getIndex() == text.length() && 
                number.doubleValue() <= Integer.MAX_VALUE                   &&
                number.doubleValue() >= Integer.MIN_VALUE                      ){
                localValue      = number.intValue();
                localValueValid = true;
            }
        }
        catch(Exception exc){
            localValueValid = false;
        }
        //check, if the Decimal signal this input field is connected to is restricted to a certain range of values
        if (localValueValid && connected && ((SignedInteger)assignedSignal).isRangeChecked()){
            localValueValid = localValue <= ((SignedInteger)assignedSignal).getMaxValue() && localValue >= ((SignedInteger)assignedSignal).getMinValue();
        }
        enteredValueValid = localValueValid;
        if (enteredValue == null){
            enteredValue = new SignedIntegerValue();
        }
        ((SignedIntegerValue)enteredValue).set(enteredValueValid ? localValue : 0);
    }
    
}
