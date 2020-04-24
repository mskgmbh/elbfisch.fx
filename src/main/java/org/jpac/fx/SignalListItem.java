/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignalListItem.java
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

import java.io.Serializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Control;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Generic;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.alarm.Alarm;

/**
 *
 * @author berndschuster
 */
public class SignalListItem implements Serializable{
    private           String  signalIdentifier;
    private           String  visibleIdentifier;
    private transient Signal  signal; 
    private transient Control stateControl; 
    private transient Control editControl;
    private           Integer rowIndex;       
    
    private transient SimpleStringProperty signalIdentifierProperty;
    private transient SimpleStringProperty visibleIdentifierProperty;
        
    public SignalListItem(String signalIdentifier, String visibleIdentifier){
        this.signalIdentifier          = signalIdentifier;
        this.visibleIdentifier         = visibleIdentifier;
        this.signalIdentifierProperty  = new SimpleStringProperty(signalIdentifier);
        this.visibleIdentifierProperty = new SimpleStringProperty(visibleIdentifier);
        this.instantiateControls();
    }
    
    public SignalListItem(String signalIdentifier){
        this(signalIdentifier,signalIdentifier);
    }
    
    protected Control instantiateStateControl(Signal signal){
        Control control = null;
        if (signal instanceof Logical || signal instanceof Alarm){
            control = new LogicalOutputField(signal.getQualifiedIdentifier());
            ((LogicalOutputField)control).setLedColor(LogicalOutputField.LedColor.BLUE);
            ((LogicalOutputField)control).setLedType(LogicalOutputField.LedType.SQUARE);
        } else if (signal instanceof Decimal){
            control = new DecimalOutputField(signal.getQualifiedIdentifier()); 
        } else if (signal instanceof SignedInteger){
            control = new SignedIntegerOutputField(signal.getQualifiedIdentifier()); 
        } else if (signal instanceof CharString){
            control = new CharStringOutputField(signal.getQualifiedIdentifier());    
        } else if (signal instanceof Generic){
            control = new GenericOutputField(signal.getQualifiedIdentifier());    
        }         
        return control;
    }

    protected Control instantiateEditControl(Signal signal){
        Control control = null;
        if (signal instanceof Logical || signal instanceof Alarm){
            control = new ToggleButton(signal.getQualifiedIdentifier());
            ((ToggleButton)control).enableInvalidation(true);
        } else if (signal instanceof Decimal){
            control = new DecimalInputField(signal.getQualifiedIdentifier()); 
        } else if (signal instanceof SignedInteger){
            control = new SignedIntegerInputField(signal.getQualifiedIdentifier()); 
        } else if (signal instanceof CharString){
            control = new CharStringInputField(signal.getQualifiedIdentifier());    
        }        
        return control;
    }
    
    public boolean instantiateControls(){
        this.signal = null;
        try{
            this.signal = SignalRegistry.getInstance().getSignal(signalIdentifier);
        }
        catch(SignalNotRegisteredException exc){
            this.signal = null;
        }
        if (signal != null){
            this.signalIdentifierProperty  = new SimpleStringProperty(signalIdentifier);
            this.visibleIdentifierProperty = new SimpleStringProperty(visibleIdentifier);
            this.stateControl              = instantiateStateControl(signal);
            this.editControl               = instantiateEditControl(signal);        
        }
        return this.signal != null;
    }
 
    public String getSignalIdentifier() {
        return signalIdentifier;
    }

    public void setSignalIdentifier(String identifier) {
        signalIdentifier = identifier;
        signalIdentifierProperty.set(identifier);
    }

    public SimpleStringProperty signalIdentifierProperty() {
        return signalIdentifierProperty;
    }

    public String getVisibleIdentifier() {
        return visibleIdentifier;
    }
    
    public void setVisibleIdentifier(String identifier) {
        visibleIdentifier = identifier;
        visibleIdentifierProperty.set(identifier);
    }

    public SimpleStringProperty visibleIdentifierProperty() {
        return visibleIdentifierProperty;
    }
    
    public void clipVisibleIdentifier(int hierarchyLevel){
        int    index = 0;
        int    lastValidIndex = 0;
        if (signalIdentifier != null){
            for (int i = 0; i < hierarchyLevel; i++){
                index = signalIdentifier.indexOf('.', index+1);
                if (index > 0){
                    lastValidIndex = index;
                }
            }
            setVisibleIdentifier(signalIdentifier.substring(lastValidIndex));
        }
        
    }

    /**
     * @return the stateControl
     */
    public Control getStateControl() {
        return this.stateControl;
    }

    /**
     * @return the editControl
     */
    public Control getEditControl() {
        return this.editControl;
    }

    /**
     * @return the Signal
     */
    public Signal getSignal() {
        return this.signal;
    }

    public void setRowIndex(int rowIndex){
        this.rowIndex = rowIndex;
    }

    public Integer getRowIndex(){
        return rowIndex;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + signalIdentifier + ", " + visibleIdentifier + ")";
    }   
    
}
