/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : HmiUtitilities.java
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

import java.net.URL;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 *
 * @author berndschuster
 */
public class HmiUtitilities {
    
    static HashMap<URL,Image> imageIconsCache;

    @SuppressWarnings("empty-statement")
    static public String getQualifiedIdentifier(Connectable connectable, String id) throws QualifiedIdentifierException{
        Node      container;
        String    qualifiedIdentifier         = null;
        String    parentsQualifiedIdentifier  = null;
        
        if (id == null || id.length() == 0){
            throw new QualifiedIdentifierException("no identifier specified for " + connectable);
        }
        //count preceding dots
        int precedingDots = 0;
        for (; precedingDots < id.length() && id.charAt(precedingDots) == '.'; precedingDots++);        
        if (precedingDots > 0){
            //given Connectable is not fully qualified
            //traverse container hierarchy upwards, until fully qualified Connectable found 
            container = (Node)connectable;
            //skip non Connectable containers
            do{
                container = container.getParent();
            }
            while(container != null && !(container instanceof Connectable));
            if (container != null){
                //containing Connectable found
                parentsQualifiedIdentifier = ((Connectable)container).getQualifiedIdentifier();
                //skip dot separated partial identifiers until number of preceding dots of the given connectable reached
                for(int i = precedingDots - 1; i > 0; i--){
                    int endIndex = parentsQualifiedIdentifier.length() - 1;
                    if (endIndex < 0){
                        throw new QualifiedIdentifierException("identifier '" + id + "' cannot be resolved in context of '" + parentsQualifiedIdentifier + "'");                
                    }
                    int lastIndex = parentsQualifiedIdentifier.lastIndexOf('.', endIndex);
                    if (lastIndex < 0){
                        throw new QualifiedIdentifierException("identifier '" + id + "' cannot be resolved in context of '" + parentsQualifiedIdentifier + "'");                
                    }
                    parentsQualifiedIdentifier = parentsQualifiedIdentifier.substring(0, lastIndex);
                }
                //construct the qualified identifier of the given connectable
                id = id.substring(precedingDots, id.length()).trim();
                if (id.length() == 0){
                    throw new QualifiedIdentifierException("incomplete identifier specified for " + connectable);            
                }
                qualifiedIdentifier = parentsQualifiedIdentifier + '.' + id;
            }
            else{
                throw new QualifiedIdentifierException("identifier '" + id + "' cannot be resolved");
            }
        }
        else{
            //given identifier is already fully qualified
            qualifiedIdentifier = id;
        }
        return qualifiedIdentifier;   
    }
    
    @SuppressWarnings("empty-statement")
    static public String getQualifiedIdentifier(Connectable connectable) throws QualifiedIdentifierException{
        return getQualifiedIdentifier(connectable, connectable.getIdentifier());
    }
    static public Image getImageIcon(URL iconUrl){
        Image icon = null;
        if (iconUrl != null) {
            if (getImageIconsCache().containsKey(iconUrl)){
                icon = getImageIconsCache().get(iconUrl);
            }
            else{
                //TODO !!!!
                icon = new Image(iconUrl.getFile());
                icon = new Image(iconUrl.getFile(), icon.getHeight()/2, icon.getWidth()/2, true, true);              
                getImageIconsCache().put(iconUrl, icon);
            }
        }
        return icon;
    }
    
    private static HashMap<URL, Image> getImageIconsCache(){
        if (imageIconsCache == null){
            imageIconsCache = new HashMap<URL, Image>();
        }        
        return imageIconsCache;
    }
    
}
