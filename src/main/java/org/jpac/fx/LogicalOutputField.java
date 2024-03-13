/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LogicalOutputField.java
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
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.image.ImageView;
import org.jpac.LogicalValue;

/**
 *
 * @author berndschuster
 */
public class LogicalOutputField extends OutputField{
    private static final String ICONDIR           = "file:/org/jpac/fx/resources/";
    private static final String ICONFILEEXTENSION = ".png";
    private static final String ON                = "-on-";
    private static final String OFF               = "-off-";

    public enum LedType {ROUND("round"),SQUARE("square");
        public String value;
        
        LedType(String value){
            this.value = value;
        }
    };
    
    public enum LedColor {BLUE("blue"),GREEN("green"),AMBER("amber"),RED("red");
        public String value;
        
        LedColor(String value){
            this.value = value;
        }
    };
    
    private LedType   ledType;
    private LedColor  ledColor;
    private ImageView onLedIcon;
    private ImageView offLedIcon;
    
    public LogicalOutputField(){
        this(NONE);
    }
    
    public LogicalOutputField(String identifier){
        super(identifier);
        this.ledType  = LedType.ROUND;
        this.ledColor = LedColor.BLUE;
        value = new LogicalValue();
        ((LogicalValue)value).set(true);
        valid = Boolean.TRUE;
        setDisplayText(false);
        loadLedImageIcons();
        super.setText("");
    }
    
    public void setLedType(LedType ledType){
        if (ledType != this.ledType){
            this.ledType = ledType;
            loadLedImageIcons();
            updateView();
        }
    }
    
    public LedType getLedType(){
        return this.ledType;
    }

    public void setLedColor(LedColor ledColor){
        if (ledColor != this.ledColor){
            this.ledColor = ledColor;
            loadLedImageIcons();
            updateView();
        }
        this.ledColor = ledColor;
    }
    
    public LedColor getLedColor(){
        return this.ledColor;
    }
    
    // @Override
    // public void update(Signal o) {
    //     super.update(o);
    // }
    
    protected void loadLedImageIcons() {
        String onPath  = ICONDIR + ledColor.value       + ON  + ledType.value + ICONFILEEXTENSION;
        String offPath = ICONDIR + ledColor.value       + OFF + ledType.value + ICONFILEEXTENSION;
        onLedIcon      = loadImageIcon(onPath);
        offLedIcon     = loadImageIcon(offPath);
    }    
    
    protected ImageView loadImageIcon(String path){
        ImageView imageView = null;
        try{
            imageView = new ImageView(HmiUtitilities.getImageIcon(new URI(path).toURL()));
            imageView = scaleImageIcon(imageView);
        }
        catch(URISyntaxException | MalformedURLException exc){
            Log.error("Error: ", exc);
        }
        return imageView;
    }  
    
    protected ImageView scaleImageIcon(ImageView imageView){
        imageView.fitWidthProperty().bind(this.widthProperty());
        imageView.fitHeightProperty().bind(this.heightProperty());
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }
    
    private ImageView selectIcon(){
        ImageView selectedIcon;
        selectedIcon = ((LogicalValue)value).get() ? onLedIcon: offLedIcon;
        return selectedIcon;
    }
    
    @Override
    protected boolean updateView(){
        boolean consumed = super.updateView();
        if (!consumed){
            showIcon(selectIcon());
            consumed = true;
        }
        return consumed;
    }    
                
    @Override
    protected Connector instantiateConnector(String qualifiedIdentifier) {
        return new LogicalConnector(qualifiedIdentifier);
    }  
}
