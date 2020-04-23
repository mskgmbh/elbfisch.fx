/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CharStringInputField.java
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

import javax.swing.InputVerifier;
import org.jpac.CharString;
import org.jpac.CharStringValue;
import org.jpac.NumberOutOfRangeException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;

/**
 *
 * @author berndschuster
 */
public class CharStringInputField extends InputField{
    protected InputVerifier inputVerifier;

    public CharStringInputField(){
        super();
        this.defaultToolTipText  = "enter an arbitrary text";
        setText("");
        toolTip.setText(defaultToolTipText);
    }
    
    public CharStringInputField(String identifier) {
        this();
        this.identifier = identifier; 
    }        

    @Override
    protected void setAssignedSignal() throws SignalAccessException, NumberOutOfRangeException{
         if (isConfirmable()){
            try{((CharString)assignedSignal).set(((CharStringValue)enteredValue).get());}catch(Exception exc){/*cannot happen*/}
        }     
    }

    @Override
    public boolean isConfirmable() {
        return true;
    }

    @Override
    protected Connector instantiateConnector(String qualifiedIdentifier) {
        return new CharStringConnector(qualifiedIdentifier);
    }
    
    @Override
    protected String getSignalDependentToolTipText(Signal signal) {
        return defaultToolTipText;
    }

    @Override
    public void evaluateEnteredValue() {
       enteredValueValid = true;
        if (enteredValue == null){
            enteredValue = new CharStringValue();
        }
        ((CharStringValue)enteredValue).set(getText());
    }
}
