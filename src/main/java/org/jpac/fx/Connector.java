/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Connector.java
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

import org.jpac.Observable;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.Signal;
import org.jpac.SignalObserver;
import org.jpac.Value;

/**
 *
 * @author berndschuster
 */
 public class Connector extends Observable implements Runnable, SignalObserver{
    static    Logger Log = LoggerFactory.getLogger("jpac.fx");     
    protected Value   newValue;
    protected Value   value;
    private   Boolean newValid;
    private   Boolean valid;
   
    boolean connectedAsTarget;
    boolean newConnectedAsTarget;
    
    String  identifier;
    
    
    public Connector(){
        this(null);
    }
    
    public Connector(String identifier){
        super();
        this.newValue             = null;
        this.value                = null;
        this.newValid             = false;
        this.valid                = false;
        this.connectedAsTarget    = false;
        this.newConnectedAsTarget = false;
        this.identifier           = identifier;
    }
        
    @Override
    public void run() {
        valid = newValid;
        if (valid){
        	if (value == null) {
        		try {
					value = newValue.clone();
				} catch (CloneNotSupportedException exc) {
					Log.error("Error: ", exc);
				}
        	} else {
            	synchronized(newValue){
            		value.copy(newValue);
            	}
        	}
        }
        connectedAsTarget = newConnectedAsTarget;
        setChanged();
        notifyObservers();
    }

    @Override
    public void update(Signal o) {
        Signal sourceSignal = (Signal)o;
    
        newValid = sourceSignal.isValid();
        if(newValid){
        	if (newValue == null) {
        		try {
					newValue = sourceSignal.getValue().clone();
				} catch (CloneNotSupportedException exc) {
					Log.error("Error: ", exc);
				}
        	} else {
	            synchronized(newValue){
	                newValue.copy(sourceSignal.getValue());
	            }
            }
        }
        Platform.runLater(this);
    }   

    @Override
    public void setConnectedAsTarget(boolean connected) {
       this.newConnectedAsTarget = connected;
       if (!connected){
          //source signal is disconnected from this connector
          //invalidate it
          newValid = false;
          Platform.runLater(this);
       }
    }

    @Override
    public boolean isConnectedAsTarget() {
        return this.connectedAsTarget;
    }

    public Object getValue(){
        return value;
    }
    
    public boolean isValueValid(){
        return valid;
    }
        
    @Override
    public String toString(){
         return this.getClass().getSimpleName() + (identifier != null ? " " + identifier : "") + "(connected = " + connectedAsTarget + " value = " + (valid ? value : "???") + ")";   
    }
}
