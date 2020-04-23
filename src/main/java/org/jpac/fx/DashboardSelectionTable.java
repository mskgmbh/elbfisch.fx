/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DashboardSelectionTable.java
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

import com.sun.javafx.tk.Toolkit;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import org.jpac.SignalNotRegisteredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class DashboardSelectionTable extends TableView<DashboardData> {
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 
    
    public  final static int              IDENTIFIERCOLUMN     = 0;
    public  final static int              TYPECOLUMN           = 1;
    
    protected TableColumn<DashboardData, String> identifierColumn;
    
    public DashboardSelectionTable(){
        super();
        //this.setSelectionModel(new SelectionModel(this));
        this.identifierColumn = new TableColumn<>("dashboard");
        
        identifierColumn.setPrefWidth(400);
        identifierColumn.setResizable(false);
        identifierColumn.setSortable(false);
        identifierColumn.setCellValueFactory(new PropertyValueFactory<DashboardData,String>("name"));
        identifierColumn.setId("identifierColumn");
        
        getColumns().addAll(identifierColumn);    
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);        
    }        
        
    public double getDefaultWidth(){
        return identifierColumn.getPrefWidth();
    }
        
    protected double getPreferredCellHeight(){
        double cellHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault()).getLineHeight() + 8;
        return cellHeight;
    }
            
    public void addEntry(DashboardData dashboardData) throws SignalNotRegisteredException{
            getItems().add(dashboardData);
    }
}
