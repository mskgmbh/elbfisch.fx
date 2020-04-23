/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ControlPane.java
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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.ImpossibleEvent;
import org.jpac.JPac;
import org.jpac.Module;
import org.jpac.ProcessException;
import org.jpac.fx.auth.Privilege;
import org.jpac.fx.auth.UserRegistry;

/**
 *
 * @author berndschuster
 */
public class ControlPane extends Pane implements Connectable, Confirmable, Observer{
    static public Logger Log = LoggerFactory.getLogger("jpac.fx");  

    public enum MessageSeverity  {INFO,ALERT};
    public enum Result           {NONE, NOFAULT, ERROR, SEVEREERROR};    
    
    private   String          identifier;
    private   boolean         connected;
    private   boolean         enabled;
    private   Confirmed       confirmedEvent;
    private   Aborted         abortedEvent;
    protected boolean         toBeConfirmed;
    protected boolean         confirmed;
    protected boolean         aborted;
    protected Result          result;
    private   Privilege       privilege;
    
    
    private   ChangeListener  messageListener;
    private   String          message;
    private   MessageSeverity messageSeverity;
    
    private   int             updateControlsState;
    
    private   boolean         enabledByApplication;
    private   boolean         enabledByPrivilege;
    
    public ControlPane(){
        super();
        result               = Result.NONE;
        privilege            = Privilege.NONE;
        enabledByApplication = true;
        enabledByPrivilege   = false;
    }
    
    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
        return HmiUtitilities.getQualifiedIdentifier(this);
    }

    @Override
    public void connect() {
        connectContainedConnnectables(this, true);
        UserRegistry.getInstance().addObserver(this);
    }

    @Override
    public void disconnect() {
        UserRegistry.getInstance().deleteObserver(this);
        connectContainedConnnectables(this, false);
    }
    
    public void setEnabled(boolean enable) {
        SetEnabledStateRunner sesr = new SetEnabledStateRunner(enable);
        if (Platform.isFxApplicationThread()){
            sesr.run();
        }
        else{
            //synchronize call to dispatch thread
            Platform.runLater(sesr);
        }            
    }
    
    private void enableByPrivilege(boolean enable) {
        EnableByPrivilegeRunner epr = new EnableByPrivilegeRunner(enable);
        if (Platform.isFxApplicationThread()){
            epr.run();
        }
        else{
            //synchronize call to dispatch thread
            Platform.runLater(epr);
        }            
    }

    private void connectContainedConnnectables(Pane pane, boolean connect){
        for (Node node: pane.getChildren()){
            if (node instanceof Connectable){
                if (connect){
                    ((Connectable)node).connect();
                }
                else{
                    ((Connectable)node).disconnect();                    
                }
            }
            else if (node instanceof Pane){
                //pane found. Check, if it contains some Connectables
                connectContainedConnnectables((Pane)node, connect);
            }
        }
        connected = connect;
    }
        
    /**
     * @return the identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to be set
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public boolean isConnected(){
        return this.connected;
    }
    
    /**
     * used to confirm/decline alterations of Confirmables which reside directly or indirectly inside this ControlPanel.
     * If confirmation is requested (confirm = true) all Confirmables inside are confirmed by 
     * propagating the values of all changed items to there assigned signals. This is guarranteed to be done in one 
     * and the same jpac cycle, to maintain the integrity of the signals.
     * if confirmation is to be declined (confirm = false), all contained Confirmables are reset to their previous values.
     * @param confirm
     * @return 
     */
    @Override
    public void confirm(boolean confirm) {
        confirmed = confirm;
        if (Log.isInfoEnabled()) Log.info("user input for " + getClass().getSimpleName() + (confirm ? " confirmed" : " declined"));
        if (!(Thread.currentThread() instanceof JPac)){
            JPac.getInstance().invokeLater(new ConfirmationRunner(this,confirm));
        }
        else{
            doConfirmation(confirm);
        }
    }
    
    protected void confirmContainedConfirmables(Pane pane, boolean confirm){
        for (Node node: pane.getChildren()){
            if (node instanceof Confirmable){
                ((Confirmable)node).confirm(confirm);
            }
            else if (node instanceof Pane){
                //Pane found. Check, if it contains some Confirmables
                confirmContainedConfirmables((Pane)node, confirm);
            }
        }        
    }

    protected void doConfirmation(boolean confirm){
        confirmContainedConfirmables(this, confirmed);
        //let the Confirmed event be fired, if some module awaits and confirmation is done
        getConfirmedEvent().setConfirmed(confirmed);        
    }
    
    protected void doAbortion(){
        //let the Confirmed event be fired, if some module awaits and confirmation is done
        getAbortedEvent().setAborted(true);        
    }

    /**
     * used to abort this ControlPanel.
     * If abortion is requested a Aborted event is sent  
     */
    public void abort() {
        if (Log.isInfoEnabled()) Log.info("control panel " + getClass().getSimpleName() + " aborted by user");
        aborted = true;
        if (!(Thread.currentThread() instanceof JPac)){
            JPac.getInstance().invokeLater(new AbortionRunner(this));
        }
        else{
            doAbortion();
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

    /**
     * used to check, if changes of any Confirmable directly or indirectly contained by this ControlPanel can be
     * confirmed. If at least one Confirmable is not, (e.g. because the value entered by the user is invalid), this
     * method returns "false".
     * @return true : any Confirmable contained in this ControlPanel is confirmable
     *         false: at least one is not.
     */
    @Override
    public boolean isConfirmable() {
        return checkContainedConfirmables(this);
    }
    
    protected boolean checkContainedConfirmables(Pane pane){
        boolean confirmable = true;
        for (Node node: pane.getChildren()){
            if (node instanceof Confirmable){
                confirmable = ((Confirmable)node).isConfirmable();
                if (!confirmable){
                    //at least one component is not confirmable, abort recursion
                    break;
                }
            }
            else if (node instanceof Pane){
                //Container found. Check, if it contains some further Confirmables
                confirmable = checkContainedConfirmables((Pane)node);
            }
        } 
        return confirmable;
    }
    
    private Confirmed getConfirmedEvent(){
        if (confirmedEvent == null){
            confirmedEvent = new Confirmed();
        }
        return confirmedEvent;
    }
    
    private Aborted getAbortedEvent(){
        if (abortedEvent == null){
            abortedEvent = new Aborted();
        }
        return abortedEvent;
    }

    protected void setMessage(String message, MessageSeverity severity){
        throw new UnsupportedOperationException();
//        if ((message != null && this.message == null)                                  || 
//            (message == null && this.message != null)                                  ||
//            (message != null && this.message != null) && !this.message.equals(message) ||
//            (this.messageSeverity != severity))                                          {
//            this.message         = message;
//            this.messageSeverity = severity;
//            if (messageListener != null){
//                messageListener.stateChanged(new ChangeEvent(this));
//            }
//            if (Log.isInfoEnabled()) Log.info(getClass().getSimpleName() + ".setMessage('" + message + "', " + severity + ")");
//        }
    }

    @Override
    public Confirmed confirmed() {
        return getConfirmedEvent();
    }

    public Aborted aborted() {
        return getAbortedEvent();
    }
    
    
    /**
     * can be called inside a jpac module to handle process step specific tasks.
     * CAUTION: Must NOT be called outside a module !!!! 
     * @param module
     * @return arbitrary, application specific object 
     * @throws ProcessException 
     */
    public Object handle(Module module) throws ProcessException{
        Logger Log = module.getLogger();//copy this line to overriding handle() methods for proper logging
        try{
            if (Log.isInfoEnabled()) Log.info("entering " + getClass().getSimpleName() + ".handle()");
            new ImpossibleEvent().await();
        }
        finally{
            if (Log.isInfoEnabled()) Log.info("leaving " + getClass().getSimpleName() + ".handle()");
        }
        return null;
    }
    
    /**
     * used to reset the control pane to a default state
     */
    public void reset(){
        if (Platform.isFxApplicationThread()){
            confirmed = false;
            aborted   = false;
            doReset();
        }
        else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    confirmed = false;
                    aborted   = false;
                    doReset();
                }
            });
        }        
    }

    /**
     * called by reset() on the swing event dispatch thread
     */
    protected void doReset(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 
     * @param listener a listener for changes of the message property 
     */
    public void setMessageListener(ChangeListener listener){
        throw new UnsupportedOperationException();
//        messageListener = listener;
//        //send the recent message to the listener right away
//        listener.changed(this, null, null);
    }
    
    /**
     * 
     * @return the message recently set. Can be used by an registered message listener
     */
    public String getMessage(){
        return message;
    }
    
    /**
     * 
     * @return the severity of the recent set. Can be used by an registered message listener
     */
    public MessageSeverity getMessageSeverity(){
        return messageSeverity;
    }

    /**
     * 
     * @return true, if the control panel has been confirmed by the user. State is reset
     *         on invocation of reset()
     */
    public boolean isConfirmed(){
        return confirmed;
    }
    
    public void setConfirmed(boolean confirmed){
        this.confirmed = confirmed;
    }
    
    /**
     * 
     * @return true, if the control panel has been aborted by the user. State is reset
     *         on invocation of reset()
     */
    public boolean isAborted(){
        return aborted;
    }
    
    /**
     * used to set/reset aborted state of the control panel
     * @param aborted 
     */
    public void setAborted(boolean aborted){
        this.aborted = aborted;
    }
    
    public void updateControls(int state){
        updateControlsState = state;
        if (Platform.isFxApplicationThread()){
            doUpdateControls(updateControlsState);
        }
        else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    doUpdateControls(getUpdateControlsState());
                }
            });
        }
    }
    
    /**
     * called by updateControls() on the swing event dispatch thread
     */
    protected void doUpdateControls(int state){
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected void setResult(Result result){
        this.result = result;
    }
    
    /**
     * a controlPanel can (if implemented by the application) supply a result when it is closed by the user.
     * @return result
     */
    public Result getResult(){
        return this.result;
    }
   
    @Override
    public void update(Observable o, Object o1) {
        if (o instanceof UserRegistry){
            Privilege p = (Privilege) o1;
            enableByPrivilege(p != null && p.isGreaterEqual(this.privilege));
        }
    }
        
    /**
     * @return the privilege
     */
    public Privilege getPrivilege() {
        return privilege;
    }

    /**
     * sets the privilege of the control panel to the given privilege and 
     * checks the privilege of the logged in user and enables/disables the 
     * access to the panel accordingly 
     * @param privilege the privilege to set
     */
    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
        enableByPrivilege(UserRegistry.getInstance().getCurrentPrivilege().isGreaterEqual(privilege));
    }
    
    /**
     * @return the enabledByApplication
     */
    public boolean isEnabledByApplication() {
        return enabledByApplication;
    }
    
    /**
     * @return the enabledByPrivilege
     */
    public boolean isEnabledByPrivilege() {
        return enabledByPrivilege;
    }   

    /**
     * @return the updateControlsState
     */
    public int getUpdateControlsState() {
        return updateControlsState;
    }

    /**
     * @param updateControlsState the updateControlsState to set
     */
    public void setUpdateControlsState(int updateControlsState) {
        this.updateControlsState = updateControlsState;
    }    
    
    class SetEnabledStateRunner implements Runnable{
        private boolean enable;
        
        public SetEnabledStateRunner(boolean enable){
            this.enable = enable;
        }
        
        @Override
        public void run() {
            if (Log.isDebugEnabled()) Log.debug(getClass().getSimpleName() + ".setEnabled(" + enable + ")");
            enabledByApplication = enable;
            setDisable(!(enabledByApplication && enabledByPrivilege));
            if (enable){
               updateControls(updateControlsState); //... restore state of all controls depending on the present state of the panel
            }
        }
    }
    
    class EnableByPrivilegeRunner implements Runnable{
        private boolean enable;
        
        public EnableByPrivilegeRunner(boolean enable){
            this.enable = enable;
        }
        
        @Override
        public void run() {
            if (Log.isDebugEnabled()) Log.debug("control panel " + getClass().getSimpleName() + ".enableByPrivilege(" + enable + ")");
            enabledByPrivilege = enable;
            setDisable(!(enabledByApplication && enabledByPrivilege));
            if (enable){
               updateControls(updateControlsState); //... restore state of all controls depending on the present state of the panel
            }
        }
    }

    class ConfirmationRunner implements Runnable{
        private ControlPane panel;
        private boolean      confirm;
        
        public ConfirmationRunner(ControlPane panel, boolean confirm){
            this.panel   = panel;
            this.confirm = confirm;
        }
        
        @Override
        public void run() {
            confirmContainedConfirmables(panel, confirm);
            //let the Confirmed event be fired, if some module awaits and confirmation is done
            getConfirmedEvent().setConfirmed(confirm);
        }
    }

    class AbortionRunner implements Runnable{
        private ControlPane panel;
        private boolean      abort;
        
        public AbortionRunner(ControlPane panel){
            this.panel = panel;
        }
        
        @Override
        public void run() {
            //let the Aborted event be fired
            getAbortedEvent().setAborted(true);
        }
    }
}
