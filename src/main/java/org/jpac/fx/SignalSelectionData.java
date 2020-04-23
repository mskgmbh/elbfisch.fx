/**
 * PROJECT   : <???>
 * MODULE    : <???>.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : <???>
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 * LOG       : $Log$
 */

package org.jpac.fx;

import javafx.beans.property.SimpleStringProperty;
import org.jpac.Signal;

/**
 *
 * @author berndschuster
 */
public class SignalSelectionData {
    protected SimpleStringProperty qualifiedIdentifier;
    protected SimpleStringProperty type;
    protected boolean              selected;

    public SignalSelectionData(Signal signal){
        qualifiedIdentifier = new SimpleStringProperty(signal.getQualifiedIdentifier());
        type                = new SimpleStringProperty(signal.getClass().getCanonicalName());
        selected            = false;
    }
    public String getQualifiedIdentifier() {
        return qualifiedIdentifier.get();
    }

    public String getType() {
        return type.get();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
