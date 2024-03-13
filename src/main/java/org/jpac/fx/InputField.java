/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : InputField.java
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

import org.jpac.Observer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.JPac;
import org.jpac.NumberOutOfRangeException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignalRegistry;
import org.jpac.Value;

/**
 *
 * @author berndschuster
 */
abstract public class InputField extends TextField implements Connectable, Confirmable, Runnable, Observer<Connector>{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx");    
    
    private   Confirmed     confirmedEvent;
    
    protected Signal        assignedSignal;
    protected String        identifier;
    protected String        qualifiedIdentifier;
    protected boolean       connected;
    
    protected boolean       toBeConfirmed;
    protected boolean       toBeInvalidated;

    protected Connector     connector;
    protected boolean       justConnected;
    protected Value         lastValidValue;
    protected Value         enteredValue;
    protected boolean       enteredValueValid;
    protected Value         signalsValue;
    protected boolean       signalsValueValid;
    protected boolean       signalsValueWasValid;
    
    protected String        defaultToolTipText;
    protected Tooltip       toolTip;
    
    protected String        idleStyle;
    protected String        unsynchronizedStyle;
    protected String        unconfirmableStyle;
    
    protected ContextMenu   contextMenu;
    protected MenuItem      invalidate;
    
    /**
     * Creates new form InputField
     */
    public InputField() {
        super();
        initComponents();
        connected            = false;
        justConnected        = false;
        toBeConfirmed        = false;
        toBeInvalidated      = false;
        connector            = null;
        signalsValue         = null;
        signalsValueValid    = false;
        signalsValueWasValid = false;
        lastValidValue       = null;
        toolTip              = new Tooltip();
        setTooltip(toolTip);
        setIdleStyle(getStyle());
        setUnconfirmableStyle("-fx-text-fill: red;");
        setUnsynchronizedStyle("-fx-text-fill: blue;");
        setDisabled(true);
        textProperty().addListener((observable, oldValue, newValue) -> {
                            evaluateEnteredValue();
                            updateStyle();
                        });
        contextMenu = new ContextMenu();
        invalidate  = new MenuItem("invalidate");
        contextMenu.getItems().addAll(invalidate);
        setContextMenu(contextMenu);
        invalidate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toBeInvalidated = true;
                if (isConfirmable() && !isToBeConfirmed()){
                    propagateValueToSignal();
                }
                updateStyle();                
            }
        });    
    }
    
    public InputField(String identifier) {
        this();
        this.identifier = identifier; 
    }    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        setText("12345.678");
        setBorder(null);
        
        setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleOnAction(event);
            }
        });
    }
    
    private void handleOnAction(ActionEvent evt) {
        toBeInvalidated = false;
        evaluateEnteredValue();
        if (isConfirmable() && !isToBeConfirmed()){
            propagateValueToSignal();
        }
        updateStyle();                
    }
   
    protected void updateStyle(){
        if (connected()){
            if (isConfirmable()){
                if (isSynchronized() && getIdleStyle() != null){
                    setStyle(getIdleStyle());                
                }
                else if (getUnsynchronizedStyle() != null){
                    setStyle(getUnsynchronizedStyle());                                    
                }
            }
            else{
                if (!getText().isEmpty()){
                    if (getUnconfirmableStyle() != null){
                        setStyle(getUnconfirmableStyle()); 
                    }
                }
                else{
                    if (getIdleStyle() != null){
                        setStyle(getIdleStyle());   
                    }
                }
            }
        }
    }
    
    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
          if (qualifiedIdentifier == null){
            this.qualifiedIdentifier = HmiUtitilities.getQualifiedIdentifier(this);
        }
        return this.qualifiedIdentifier;    
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void connect() {
        try{
            Signal signal = SignalRegistry.getInstance().getSignal(getQualifiedIdentifier());
            if (!signal.isConnectedAsTarget()){
                assignedSignal = signal;
                if (connector == null){
                    connector = instantiateConnector(getQualifiedIdentifier());
                }
                connector.addObserver(this);
                assignedSignal.connect(connector);
                toolTip.setText(getSignalDependentToolTipText(assignedSignal));
                connected             = true;
                justConnected         = true;
                setDisabled(false);
                Log.debug(this + " connected to target signal");
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            assignedSignal = null;
            connected      = false;
        }
    }

    @Override
    public void disconnect() {
        if (connected()){
            connected = false;
            JPac.getInstance().invokeLater(this);  
            connector.deleteObserver(this);        
            assignedSignal.disconnect(connector);      
            setText("");
            signalsValueValid = false;
            signalsValue      = null;
            setDisabled(true);
            Log.debug(this + " disconnected from target signal");
        }
    }
    
    @Override
    public void update(Connector o) {
        signalsValueValid = connector.isValueValid();
        boolean signalsValueGotValid   = signalsValueValid  && !signalsValueWasValid;
        boolean signalsValueGotInvalid = !signalsValueValid && signalsValueWasValid;
        signalsValueWasValid = signalsValueValid;

        signalsValue      = signalsValueValid ? (Value)connector.getValue() : null;
        if (signalsValue != null){
            if (lastValidValue == null){
                try{lastValidValue = signalsValue.clone();}catch(CloneNotSupportedException exc){/*cannot happen*/};            
            }
            else{
                lastValidValue.copy(signalsValue);            
            }        
        }
        if (justConnected || signalsValueGotValid){
            //if just connected, show up the current signalsValue of the assigned signal inside the input field
            setText(formatText());
            evaluateEnteredValue();
            justConnected = false;
        } else if (signalsValueGotInvalid){
            setText("");
            evaluateEnteredValue();            
        }
        updateStyle();
    } 
    
    protected String formatText(){
        String formattedValue;
        if (!signalsValueValid){
            formattedValue = "";
        }
        else{
            formattedValue = signalsValue.toString();
        }
        return formattedValue; 
    };
    
    protected void propagateValueToSignal(){
        if (Thread.currentThread() instanceof JPac){
            //if called by jPac run propagation instantly
            run();
        }
        else{
            //else defer propagation until next jPac cycle
            JPac.getInstance().invokeLater(this);       
        }
    }
    
    protected boolean isSynchronized() {
        return isConfirmable() && signalsValue != null && enteredValue != null && signalsValue.equals(enteredValue);
    }
        
    @Override
    public boolean connected() {
       return connected;
    }
    
    
    @Override
    public void setToBeConfirmed(boolean toBeConfirmed){
        this.toBeConfirmed = toBeConfirmed;
    }
    
    @Override
    public boolean isToBeConfirmed(){
        return toBeConfirmed;
    }

    @Override
    public boolean isConfirmable(){
        return enteredValueValid;
    };

    @Override
    public void confirm(boolean confirm) {
        if (confirm){
            //may be done on jPac or awt thread
            propagateValueToSignal();
        }
        else{
            //is done on the awt thread
            if (lastValidValue != null){
                signalsValue.copy(lastValidValue);                
            }
            else{
                signalsValueValid = false;
                signalsValue      = null;                
            }
            setText(formatText());
            updateStyle();            
        }    
    }    
    
    @Override
    public void run() {
        try{
            if (connected()){
                if (toBeInvalidated){
                    toBeInvalidated = false;
                    assignedSignal.invalidate();
                }
                else{
                    setAssignedSignal();
                }
                //let the Confirmed event be fired, if some module is awaiting it
                getConfirmedEvent().setConfirmed(true);
            }
            else{
                //TODO invalidate signal on disconnection: not useful in dashboard applications
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + (identifier != null ? " " + identifier : "") + "(connected = " + connected + " text = " + getText() + ")";
    }

    abstract protected void      setAssignedSignal() throws SignalAccessException, NumberOutOfRangeException;    
    abstract protected Connector instantiateConnector(String qualifiedIdentifier);
    abstract protected String    getSignalDependentToolTipText(Signal signal);
    
    /**
     * must be implemented by an extending class to set the instance variables
     * enteredValue, enteredValueValid
     */
    abstract public void evaluateEnteredValue();
    
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
     * @return the idleStyle
     */
    public String getIdleStyle() {
        return idleStyle;
    }

    /**
     * @param idleStyle the idleStyle to set
     */
    public void setIdleStyle(String idleStyle) {
        this.idleStyle = idleStyle;
    }

    /**
     * @return the unsynchronizedStyle
     */
    public String getUnsynchronizedStyle() {
        return unsynchronizedStyle;
    }

    /**
     * @param unsynchronizedStyle the unsynchronizedStyle to set
     */
    public void setUnsynchronizedStyle(String unsynchronizedStyle) {
        this.unsynchronizedStyle = unsynchronizedStyle;
    }

    /**
     * @return the unconfirmableStyle
     */
    public String getUnconfirmableStyle() {
        return unconfirmableStyle;
    }

    /**
     * @param unConfirmableStyle the unconfirmableStyle to set
     */
    public void setUnconfirmableStyle(String unConfirmableStyle) {
        this.unconfirmableStyle = unConfirmableStyle;
    }
}
