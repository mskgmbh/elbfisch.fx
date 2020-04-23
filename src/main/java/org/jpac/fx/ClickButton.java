/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ClickButton.java
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

import java.util.Observable;
import java.util.Observer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.LogicalValue;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyConnectedException;
import org.jpac.SignalRegistry;
import org.jpac.Value;
import org.jpac.alarm.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */

public class ClickButton extends Button implements Connectable, Confirmable, Runnable, Observer{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx");
    
    private   Confirmed    confirmedEvent;
    
    protected Signal       assignedSignal;
    protected String       identifier;
    protected String       qualifiedIdentifier;
    protected boolean      connected;
    protected String       buttonText;
    protected boolean      blankOutIfDisconnected;
    protected Connector    connector;
    protected boolean      justConnected; 
    protected LogicalValue value;
    protected boolean      lastPressedState;
    protected boolean      valueValid;    
    protected boolean      valueWasValid;
    protected boolean      toBeConfirmed;
    
    /**
     * Creates new form ToggleButton
     */
    public ClickButton() {
        super();
        setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleOnAction(event);
            }
        });        
        connected              = false;
        buttonText             = getText();
        connector              = null;
        justConnected          = false;
        value                  = null;
        lastPressedState       = isPressed();
        valueValid             = false;
        valueWasValid          = false;
        if (blankOutIfDisconnected){
            setText("");
        }
    }
    
    public ClickButton(String identifier) {
        this();
        this.identifier = identifier; 
    }
    
    private void handleOnAction(ActionEvent evt) {
        if (connected){
            if (!toBeConfirmed){
                //set signal right away
                JPac.getInstance().invokeLater(this);
                lastPressedState = isPressed();
            }
            //else defer propagation to the signal until confirmation (confirm())
            updateForeGroundColor();
        }
        else{
            if (blankOutIfDisconnected){
                setText("");
            }
            setDisable(true);            
        }
    }

    @Override
    public void run() {
        try{
            if (connected){
//                if (!connectedSignal.isConnectedAsTarget()){
//                    connectedSignal.setConnectedAsTarget(true);
//                }
                if (assignedSignal instanceof Logical){
                    ((Logical)assignedSignal).set(lastPressedState);
                } else if (assignedSignal instanceof Alarm){
                    ((Alarm)assignedSignal).set(lastPressedState);
                }
                //let the Confirmed event be fired, if some module is awaiting it
                getConfirmedEvent().setConfirmed(true);
            }
            else{
//                connectedSignal.invalidate();
//                connectedSignal.setConnectedAsTarget(false);
            }
        }
        catch(SignalAccessException exc){
            Log.error("Error: ", exc);
        }
    }

    /**
     * @return the identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
        if (qualifiedIdentifier == null){
            qualifiedIdentifier = HmiUtitilities.getQualifiedIdentifier(this);
        }
        return qualifiedIdentifier;    
    }
    
   
    @Override
    public void update(Observable o, Object o1) {
        valueValid     = connector.isValueValid();
        boolean valueGotValid   = valueValid  && !valueWasValid;
        boolean valueGotInvalid = !valueValid && valueWasValid;
        valueWasValid = valueValid;
        
        value = (LogicalValue)(valueValid ? (Value)connector.getValue() : null);
        if (justConnected || valueGotValid){
            //if just connected, show up the current value of the assigned signal 
            //setText(valueValid ? buttonText : "");
            setPressed(valueValid ? value.is(true) : false);
            justConnected = false;
        } else if (valueGotInvalid){
            setPressed(false);
        }
        updateForeGroundColor();
        if (Log.isDebugEnabled()) Log.debug(this + ".update()");        
    }
    
    protected void updateForeGroundColor(){
        //nothing to do
    }    
    
    @Override
    public void connect() {
        try{
            Signal signal = SignalRegistry.getInstance().getSignal(getQualifiedIdentifier());
            if (signal.isConnectedAsTarget()){
                throw new SignalAlreadyConnectedException(signal);
            }
            assignedSignal = signal;
            connected      = true;
            if (connector == null){
                //first connection
                connector  = new LogicalConnector(getQualifiedIdentifier());
                buttonText = getText();//save actual button text 
            }
            connector.addObserver(this);
            assignedSignal.connect(connector);            
            if (blankOutIfDisconnected){
                setText(buttonText);
            }
            justConnected = true;            
            //setEnabled(true);
            setDisabled(false);
            //repaint();
            if (Log.isDebugEnabled()) Log.debug(this + " connected to target signal");
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            assignedSignal = null;
            connected      = false;
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()){
            assignedSignal.disconnect(connector);            
            connected = false;
            if (blankOutIfDisconnected){
                setText("");
            }
            //setEnabled(false);
            setDisabled(true);
            if (Log.isDebugEnabled()) Log.debug(this + " disconnected from target signal");
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
    
    public void setBlankOutIfDisconnected(boolean blankOut){
        this.blankOutIfDisconnected = blankOut;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + (identifier != null ? " " + identifier : "") + "(connected = " + connected + " value = " + isPressed() + ")";
    }
    
    @Override
    public void confirm(boolean confirm) {
        if (isToBeConfirmed() && confirm){
            JPac.getInstance().invokeLater(this);
            lastPressedState = isPressed();            
        }
        else{
            setPressed(lastPressedState);
            updateForeGroundColor();
        }
    }

    @Override
    public void setToBeConfirmed(boolean toBeConfirmed) {
        this.toBeConfirmed = toBeConfirmed;
    }

    @Override
    public boolean isToBeConfirmed() {
        return this.toBeConfirmed;
    }

    @Override
    public boolean isConfirmable() {
        return true;
    }

    private Confirmed getConfirmedEvent(){
        if (confirmedEvent == null){
            confirmedEvent = new Confirmed();
        }
        return confirmedEvent;
    }

    @Override
    public Confirmed confirmed() {
        return getConfirmedEvent();
    }
}
