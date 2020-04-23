/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignalSelectionTable.java
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

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class SignalSelectionTable extends TableView<SignalSelectionData> {
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 
    
    public  final static int              IDENTIFIERCOLUMN     = 0;
    public  final static int              TYPECOLUMN           = 1;
    
    protected TableColumn<SignalSelectionData, String> identifierColumn;
    protected TableColumn<SignalSelectionData, String> typeColumn;
    
    public SignalSelectionTable(){
        super();
        //this.setSelectionModel(new SelectionModel(this));
        this.identifierColumn = new TableColumn<>("Signal");
        this.typeColumn       = new TableColumn<>("Type");
        
        identifierColumn.setPrefWidth(50);
        identifierColumn.setResizable(true);
        identifierColumn.setSortable(false);
        identifierColumn.setCellValueFactory(new PropertyValueFactory<SignalSelectionData,String>("qualifiedIdentifier"));
        identifierColumn.setId("identifierColumn");
        typeColumn.setResizable(true);
        typeColumn.setSortable(false);
        typeColumn.setCellValueFactory(new PropertyValueFactory<SignalSelectionData,String>("type"));
        typeColumn.setId("typeColumn");
        
        getColumns().addAll(identifierColumn, typeColumn);    
        adjustDefaultStateCellSize();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);        
    }        
        
    public void adjustDefaultStateCellSize(){
        FontMetrics fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault());
        
        identifierColumn.setResizable(true);
        identifierColumn.setPrefWidth(fontMetrics.computeStringWidth("Main.AnyModule.AnySubModule.Signal"));
        typeColumn.setResizable(true);
        typeColumn.setPrefWidth(fontMetrics.computeStringWidth("org.jpac.ArbitrarySignalType"));
    }

    public double getDefaultWidth(){
        return identifierColumn.getPrefWidth() + typeColumn.getPrefWidth();
    }
        
    protected double getPreferredCellHeight(){
        double cellHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault()).getLineHeight() + 10;
        return cellHeight;
    }
            
    public void addEntry(Signal signal) throws SignalNotRegisteredException{
            getItems().add(new SignalSelectionData(signal));
    }
}
