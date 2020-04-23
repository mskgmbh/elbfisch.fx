/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RequestButton.java
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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.Handshake;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.LogicalValue;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyConnectedException;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.SignedIntegerValue;
import org.jpac.Value;
import org.jpac.WrongUseException;

/**
 *
 * @author berndschuster
 */

public class RequestButton extends Button implements Connectable, Confirmable, Observer{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx");
    
    public enum EnableRule{
        ALWAYSENABLED,          //control is always enabled
        WHILENOTREQUESTED,      //control is disabled on pressing the button until the assigned handshake is idle again
        WHILENOTACTIVE;         //control is disabled on the active state of the assigned handshake
                                //until it is idle again
                                //HINT: The chosen enable strategy of the request button may be overwritten by the assigned
                                //      enable signal
        
    }
    
    private   Confirmed    confirmedEvent;
    
    protected Logical       assignedRequestSignal;
    protected Logical       assignedActiveSignal;
    protected Logical       assignedAcknowledgeSignal;
    protected SignedInteger assignedResultSignal;
    protected Logical       assignedEnableSignal;
    protected String        identifier;
    protected String        qualifiedIdentifier;
    protected String        enableIdentifier;
    protected String        qualifiedEnableIdentifier;
    protected boolean       connected;
    protected String        buttonText;
    protected boolean       blankOutIfDisconnected;
    protected Connector     requestConnector;
    protected Connector     activeConnector;
    protected Connector     acknowledgeConnector;
    protected Connector     enableConnector;
    protected Connector     resultConnector;
    
    protected LogicalValue        requestValue;
    protected LogicalValue        activeValue;
    protected LogicalValue        acknowledgeValue;
    protected LogicalValue        enableValue;
    protected SignedIntegerValue  resultValue;
    protected boolean             lastRequestedState;
    protected boolean             valuesValid;    
    protected boolean             valuesWereValid;
    protected boolean             toBeConfirmed;
    protected boolean             justClicked;
    protected boolean             requested;
    protected String              idleStyle;
    protected String              requestedStyle;
    protected String              activeStyle;
    protected String              disabledStyle;
    
    protected RequestRunner       requestRunner;
    protected ResetRequestRunner  resetRequestRunner;
    
    protected EnableRule          enableRule;
    protected boolean             enabledByStrategy; 
    
    /**
     * Creates new form ToggleButton
     */
    public RequestButton() {
        super();
        setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleOnAction(event);
            }
        });        
            	
        connected              = false;
        buttonText             = getText();
        lastRequestedState     = isPressed();
        valuesValid            = false;
        valuesWereValid        = false;
        requestRunner          = new RequestRunner();
        resetRequestRunner     = new ResetRequestRunner();
        justClicked            = false;
        requested              = false;
        enableRule             = EnableRule.ALWAYSENABLED;
        if (blankOutIfDisconnected){
            setText("");
        }
        setIdleStyle(getStyle());
        updateStyle();
    }
    
    public RequestButton(String identifier) {
        this();
        this.identifier = identifier; 
    }
    
    private void handleOnAction(ActionEvent evt) {//GEN-FIRST:event_formActionPerformed
        if (!justClicked){
            justClicked = true;     //ignore all user action, until the request is propagated to jPac (see handleUpdateFor())
            requested = !requested; //toggle requested state
            if (connected){
                if (!toBeConfirmed){
                    //set signal right away
                    lastRequestedState = requested;
                    JPac.getInstance().invokeLater(lastRequestedState ? requestRunner : resetRequestRunner);
                    if (Log.isDebugEnabled())Log.debug("button pressed/released requested state = " + lastRequestedState);
                }
                //else defer propagation to the signal until confirmation (confirm())
            }
            else{
                if (blankOutIfDisconnected){
                    setText("");
                }
                setDisable(true);            
            }
            updateStyle();
        }
    }
    
    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     */
    public String getEnableIdentifier() {
        return this.enableIdentifier;
    }

    /**
     * @param identifier of the signal which is used to enable/disable the button
     */
    public void setEnableIdentifier(String identifier) {
        this.enableIdentifier = identifier;
    }

    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
        if (qualifiedIdentifier == null){
            qualifiedIdentifier = HmiUtitilities.getQualifiedIdentifier(this);
        }
        return qualifiedIdentifier;    
    }
       
    public String getQualifiedEnableIdentifier() throws QualifiedIdentifierException {
        if (qualifiedEnableIdentifier == null){
            qualifiedEnableIdentifier = HmiUtitilities.getQualifiedIdentifier(this);
        }
        return qualifiedEnableIdentifier;    
    }

    @Override
    public void update(Observable o, Object o1) {
        handleUpdateFor(((Connector) o));
        boolean valuesValid      = requestConnector.isValueValid() && activeConnector.isValueValid() && 
                                   acknowledgeConnector.isValueValid() && resultConnector.isValueValid() &&
                                   (assignedEnableSignal != null ? enableConnector.isValueValid() : true);
        boolean valuesGotInvalid = !valuesValid && valuesWereValid;
        valuesWereValid = valuesValid;
        
        if (valuesGotInvalid){
            setPressed(false);
        }
        else{
            setPressed(requested);
        }
        if (valuesValid){
            //check, if the button is enabled by an assigned enable signal
            boolean enabledByAssignedEnableSignal = true;
            if (assignedEnableSignal != null && enableConnector.isValueValid()){
                enabledByAssignedEnableSignal = enableValue.is(true);
            }
            //check, if the button is enabled by rule
            boolean enabledByRule = false;
            switch(enableRule){
                case ALWAYSENABLED:
                    enabledByRule = true;
                    break;
                case WHILENOTREQUESTED:
                    enabledByRule = requestValue.is(false);
                    break;
                case WHILENOTACTIVE:
                    enabledByRule = activeValue.is(false);
            }
            
            setDisable(!(enabledByAssignedEnableSignal && enabledByRule));
            updateStyle();
        }
    }
    
    protected void handleUpdateFor(Connector connector){
        if (connector.equals(requestConnector)){
            requestValue = (LogicalValue)(connector.isValueValid() ? (Value)connector.getValue() : null);
            justClicked  = false;//request was successfully forwarded to jPac
            requested = requestValue != null ? requestValue.is(true) : false;
            if (Log.isDebugEnabled())Log.debug("RequestButton" + identifier + ".request updated: " + requestValue.is(true));
        } else if (connector.equals(activeConnector)){
            activeValue = (LogicalValue)(connector.isValueValid() ? (Value)connector.getValue() : null);
            if (Log.isDebugEnabled())Log.debug("RequestButton" + identifier + ".active updated: " + activeValue.is(true));
        } else if (connector.equals(acknowledgeConnector)){
            acknowledgeValue = (LogicalValue)(connector.isValueValid() ? (Value)connector.getValue() : null);
            if (Log.isDebugEnabled())Log.debug("RequestButton " + identifier + ".acknowledge updated: " + acknowledgeValue.is(true));
            JPac.getInstance().invokeLater(resetRequestRunner);
        } else if (connector.equals(resultConnector)){
            resultValue = (SignedIntegerValue)(connector.isValueValid() ? (Value)connector.getValue() : null);
            if (Log.isDebugEnabled())Log.debug("RequestButton " + identifier + ".result updated: " + resultValue.get());            
        } else if (connector.equals(enableConnector)){
            enableValue = (LogicalValue)(connector.isValueValid() ? (Value)connector.getValue() : null);
            if (Log.isDebugEnabled())Log.debug("RequestButton enable updated: " + enableValue.get());            
        } 
    }
        
    protected void updateStyle(){
        if (isConnected()){
            if (requested && activeValue != null && activeValue.is(false) && acknowledgeValue != null && acknowledgeValue.is(false) && getRequestedStyle() != null){
                setStyle(getRequestedStyle());
            } else if (requested && activeValue != null && activeValue.is(true) && acknowledgeValue != null && acknowledgeValue.is(false) && getActiveStyle() != null){
                setStyle(getActiveStyle());
            } else if (isDisabled() && getDisabledStyle() != null){
                setStyle(getDisabledStyle());
            } else if (getIdleStyle() != null){
                setStyle(getIdleStyle());
            }
        }
    }    

    @Override
    public void connect() {
        try{
            Signal signal = (Logical)SignalRegistry.getInstance().getSignal(getQualifiedIdentifier() + ".request");
            if (signal.isConnectedAsTarget()){
                throw new SignalAlreadyConnectedException(signal);
            }
            assignedRequestSignal     = (Logical)signal;
            assignedActiveSignal      = (Logical)SignalRegistry.getInstance().getSignal(getQualifiedIdentifier() + ".active");
            assignedAcknowledgeSignal = (Logical)SignalRegistry.getInstance().getSignal(getQualifiedIdentifier() + ".acknowledge");
            assignedResultSignal      = (SignedInteger)SignalRegistry.getInstance().getSignal(getQualifiedIdentifier() + ".result");
            connected      = true;
            if (requestConnector == null){
                //first connection
                requestConnector  = new LogicalConnector(getQualifiedIdentifier() + ".request");
                buttonText = getText();//save actual button text 
            }
            requestConnector.addObserver(this);
            assignedRequestSignal.connect(requestConnector);            
            if (activeConnector == null){
                //first connection
                activeConnector  = new LogicalConnector(getQualifiedIdentifier() + ".active");
            }
            activeConnector.addObserver(this);
            assignedActiveSignal.connect(activeConnector);            
            if (acknowledgeConnector == null){
                //first connection
                acknowledgeConnector  = new LogicalConnector(getQualifiedIdentifier() + ".acknowledge");
            }
            acknowledgeConnector.addObserver(this);            
            assignedAcknowledgeSignal.connect(acknowledgeConnector);            
            if (resultConnector == null){
                //first connection
                resultConnector  = new SignedIntegerConnector(getQualifiedIdentifier() + ".result");
            }
            resultConnector.addObserver(this);            
            assignedResultSignal.connect(resultConnector);            
            if (blankOutIfDisconnected){
                setText(buttonText);
            } 
            if(enableIdentifier != null){
                //enable signal assigned
                //connect it
                String qualifiedIdentifier  = HmiUtitilities.getQualifiedIdentifier(this, enableIdentifier);
                assignedEnableSignal = (Logical)SignalRegistry.getInstance().getSignal(qualifiedIdentifier);
                if (enableConnector == null){
                    //first connection
                    enableConnector  = new LogicalConnector(qualifiedIdentifier);
                }
                enableConnector.addObserver(this);            
                assignedEnableSignal.connect(enableConnector);            
                //setDisable(!(assignedEnableSignal.isValid() && assignedEnableSignal.is(true)));
            }
            else{
                //setDisable(false);
            }
            updateStyle();
            if (Log.isDebugEnabled()) Log.debug(this + " connected to target signal");
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            connected                 = false;
            disconnect();
            assignedRequestSignal     = null;
            assignedActiveSignal      = null;
            assignedAcknowledgeSignal = null;
            assignedResultSignal      = null;
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()){
            assignedRequestSignal.disconnect(requestConnector); 
            assignedActiveSignal.disconnect(activeConnector);
            assignedAcknowledgeSignal.disconnect(acknowledgeConnector);
            assignedResultSignal.disconnect(resultConnector);
            connected = false;
            if (blankOutIfDisconnected){
                setText("");
            }
            setDisable(true);
            if (Log.isDebugEnabled()) Log.debug(this + " disconnected from target signal");
        }
        updateStyle();
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
            lastRequestedState = requested;
            JPac.getInstance().invokeLater(lastRequestedState ? requestRunner : resetRequestRunner);
            if (Log.isDebugEnabled())Log.debug("button pressed/released requested state = " + lastRequestedState);
        }
        updateStyle();
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
    
    /**
     * used to set the enable strategy
     * @param rule : ALWAYSENABLED     : control is always enabled
     *               WHILENOTREQUESTED : control is disabled on pressing the button until the assigned handshake is idle again
     *               WHILENOTREQUESTED : control is disabled on the active state of the assigned handshake
     *                                   until it is idle again
     *        HINT : if an enable signal is assigned by setting its identifier using setEnableIdentifier(), 
     *               this signal might be used to disable the button regardless of the chosen enable rule. If the signal
     *               enables the button, the button follows the chosen rule.
     */
    public void setEnableRule(EnableRule rule){
        this.enableRule = rule;
    }
    
    /**
     * used to retrieve the actual enable rule
     * @return the actual enable rule
     */
    public EnableRule getEnableRule(){
        return this.enableRule;
    }

    /**
     * @return the requestedStyle
     */
    public String getRequestedStyle() {
        return requestedStyle;
    }

    /**
     * @param style the style to set
     */
    public void setRequestedStyle(String style) {
        this.requestedStyle = style;
    }

    /**
     * @return the activeStyle
     */
    public String getActiveStyle() {
        return activeStyle;
    }

    /**
     * @param style the style to set
     */
    public void setActiveStyle(String style) {
        this.activeStyle = style;
    }

    /**
     * @return the disabledStyle
     */
    public String getDisabledStyle() {
        return disabledStyle;
    }

    /**
     * @param style the style to set
     */
    public void setDisabledStyle(String style) {
        this.disabledStyle = style;
    }
    
    /**
     * @return the disabledStyle
     */
    public String getIdleStyle() {
        return idleStyle;
    }

    /**
     * @param style the style to set
     */
    public void setIdleStyle(String style) {
        this.disabledStyle = style;
    }

    protected class RequestRunner implements Runnable{
        @Override
        public void run() {
            try{
                if (connected){
                    if (activeValue.is(true) || acknowledgeValue.is(true) || resultValue.get() != Handshake.OK){
                        throw new WrongUseException("handshake " + identifier + " not idle: acknowledge= " + acknowledgeValue.is(true) + " active= " + activeValue.is(true) + " result= " + resultValue.get());
                    }
                    assignedRequestSignal.set(true);
                    //let the Confirmed event be fired, if some module is awaiting it
                    getConfirmedEvent().setConfirmed(true);
                }
            }
            catch(SignalAccessException exc){
                Log.error("Error: ", exc);
            }               
            catch(WrongUseException exc){
                Log.error("Error: ", exc);
            }               
        }
        
    }

    protected class ResetRequestRunner implements Runnable{
        @Override
        public void run() {
            try{
                if (connected){
                    assignedRequestSignal.set(false);
                    getConfirmedEvent().setConfirmed(true);
                }
            }
            catch(SignalAccessException exc){
                Log.error("Error: ", exc);
            }           
        }
        
    }
}
