/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : OutputField.java
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.Signal;
import org.jpac.SignalRegistry;

/**
 *
 * @author berndschuster
 */
public abstract class OutputField extends Label implements Observer, Connectable{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx");

    protected static final String WARNINGICONPATH = "file:/org/jpac/fx/resources/warning.png";
    protected static final String NONE = "<none>";
    protected static final int    GAP  = 2;
    
    protected Object     value;
    protected boolean    valid;
    protected boolean    displayText;
    protected boolean    connected;
    protected Boolean    wasValid;
    protected boolean    wasConnected;
    
    protected Connector  connector;
    protected Signal     signal;
    
    String               identifier;
    String               qualifiedIdentifier;
    
    protected ImageView  warningIconImageView;
    protected ImageView  shownImageView;
    protected Image      warningIcon;
    protected Color      warningBackground;
    
    protected int        actualWidth;
    protected int        actualHeight;
    protected int        actualWarningIconXPosition;
    protected int        actualWarningIconYPosition;
    
    protected String     disconnectedText;
    
    protected Tooltip    toolTip;
    
    protected String     invalidStyle;
    protected String     disconnectedStyle;
    protected String     defaultStyle;
    
    public OutputField(){
        this(NONE);
    }
    
    public OutputField(String identifier){
        super();
        this.value                      = 1234;
        this.valid                      = false;
        this.displayText                = true;
        this.connected                  = false;
        this.wasValid                   = null;
        this.wasConnected               = false;
        this.identifier                 = identifier;
        this.warningIcon                = null;
        this.actualWidth                = -1; //invalid
        this.actualHeight               = -1; //invalid
        this.actualWarningIconXPosition = 0;
        this.actualWarningIconYPosition = 0;
        this.disconnectedText           = null;
        this.toolTip                    = new Tooltip();
        this.defaultStyle               = getStyle();
        this.invalidStyle               = "-fx-graphic-vpos:center; -fx-graphic-hpos:center";
        this.disconnectedStyle          = "";
        loadImageIcons();
        this.warningIconImageView       = new ImageView(warningIcon);
        this.warningIconImageView.fitWidthProperty().bind(this.widthProperty().subtract(2 * GAP));
        this.warningIconImageView.fitHeightProperty().bind(this.heightProperty().subtract(2 * GAP));
        this.warningIconImageView.setPreserveRatio(true);
        this.warningIconImageView.setSmooth(true);
        this.warningIconImageView.setCache(true);
        this.setAlignment(Pos.CENTER_LEFT);
        setTooltip(toolTip);
        
        widthProperty().addListener(e -> {
            alignWarningIcon();
        });

        heightProperty().addListener(e ->{
                alignWarningIcon();
            }  
        );

        // widthProperty().addListener(new ChangeListener(){
        //     @Override
        //     public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        //         alignWarningIcon();
        //     }  
        // });

        // heightProperty().addListener(new ChangeListener(){
        //     @Override
        //     public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        //         alignWarningIcon();
        //     }  
        // });

    }
    
    protected void setDisplayText(boolean set){
        this.displayText = set;
    }
    
    @Override
    public void setIdentifier(String identifier){
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier(){
        return this.identifier;
    }
    
    @Override
    public void update(Observable o, Object o1) {
        valid = connector.isValueValid();
        value = valid ? connector.getValue() : 0;
        String text = formatText();
        setText(text);
        toolTip.setText(valid ? value.toString() : null);
        updateView();
        if (Log.isDebugEnabled()) Log.debug(this + ".update()");        
    }

    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
        if (qualifiedIdentifier == null){
            qualifiedIdentifier = HmiUtitilities.getQualifiedIdentifier(this);
        }
        return qualifiedIdentifier;
    }
                
    @Override
    public void connect(){
        try{
            //the led is off until it is connected
            value    = null;
            valid    = false;
            wasValid = null;
            if (disconnectedText == null){
                //on first connect:
                //save text initially set by the implementor
                //for display while control is disconnected
                disconnectedText = getText();
            }
            if (connector == null){
                connector = instantiateConnector(getQualifiedIdentifier());
                connector.addObserver(this);
            }
            signal    = SignalRegistry.getInstance().getSignal(getQualifiedIdentifier());
            signal.connect(connector);
            connected = true;
            if (Log.isDebugEnabled()) Log.debug(this + " connected to target signal");            
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }

    @Override
    public void disconnect(){
        try{
            if (connected){
                connected = false;
                signal.disconnect(connector);
            }
            value    = null;
            valid    = false;
            wasValid = false;
            //restore initial text set by the implementor
            setText(disconnectedText);
            if (Log.isDebugEnabled()) Log.debug(this + " disconnected from target signal");            
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    protected String formatText(){
        String formattedValue;
        if (!valid){
            formattedValue = "";
        }
        else{
            formattedValue = displayText ? value.toString() : "";
        }
        return formattedValue; 
    };
    
    protected void loadImageIcons(){
        try{
            warningIcon = HmiUtitilities.getImageIcon(new URL(WARNINGICONPATH));
        }
        catch(MalformedURLException exc){
            Log.error("Error: ", exc);
        };
    }    
           
    protected boolean updateView() {
        boolean consumed = false;
        if (connected){
            if ((!valid && wasValid == null) || (valid != (wasValid != null && wasValid))){
                //update display of warning icon if valid state changed or called first time in connected state
                wasValid = valid;
                if (!valid){
                    alignWarningIcon();
                    showIcon(warningIconImageView);
                }
            }
            wasConnected = true;
        }
        else{
            if (wasConnected){
                //connected state changed or called first time
                wasConnected = false;
                this.setText(null);
                removeIcon();
            }
        }
        consumed = !connected || !valid;
        return consumed;
    }
    
    protected void showIcon(ImageView imageView){
        if (shownImageView != null){
            getChildren().remove(shownImageView);
            shownImageView = null;
        }
        if (imageView != null){
            getChildren().add(imageView);
            shownImageView = imageView;            
        }
    }
    
    protected void removeIcon(){
        if (shownImageView != null){
            getChildren().remove(shownImageView);
            shownImageView = null;
        }
    }

    protected void alignWarningIcon(){
        double iw = warningIconImageView.boundsInParentProperty().getValue().getWidth();
        double ih = warningIconImageView.boundsInParentProperty().getValue().getHeight();
        warningIconImageView.setX((getWidth()  - iw)/2);
        warningIconImageView.setY((getHeight() - ih)/2);        
    }
        
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    
    public String toString() {
        return this.getClass().getSimpleName() + (identifier != null ? " " + identifier : "") + "(connected = " + connected + " value = " + value + ")";
    }
            
    protected abstract Connector instantiateConnector(String qualifiedIdentifier);
}
