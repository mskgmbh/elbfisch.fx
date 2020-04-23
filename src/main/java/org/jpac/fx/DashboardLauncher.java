/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DashboardLauncher.java
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 *
 * @author berndschuster
 */
public class DashboardLauncher extends Stage {
    static  Logger Log = LoggerFactory.getLogger("jpac.fx");     
        
    private boolean                            errorLoadingDashboards;     
    private boolean                            errorStoringDashboard;
    private ArrayList<DashboardData>           dashboardDatas;
    
    private Scene                              scene;
    private Button                             btnCreateNew;
    private Button                             btnDeleteSelectedDashboards;
    private Button                             btnLaunchSelectedDashboards;
    private javafx.scene.control.ToggleButton  btnSelectDeselect;
    private HBox                               creationControlPanel;
    private HBox                               infoTextPanel;
    private Label                              lbInfoText;
    private HBox                               selectionControlPanel;
    private DashboardSelectionTable            tabSelectDashboards;
    private VBox                               contentPane;
    private DashboardEditor                    dashboardEditor;
    private ContextMenu                        contextMenu;
    private MenuItem                           menutItemLaunchDashboard;
    private MenuItem                           menutItemDeleteDashboard;
    
    public DashboardLauncher() {
        super();
        initComponents();
        this.initStyle(StageStyle.DECORATED);
        this.setTitle("Elbfisch dashboard launcher");
        tabSelectDashboards.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<DashboardData>() {
			@Override
			public void onChanged(Change<? extends DashboardData> c) {
				controlButtons();
                controlInfoText(); 				
			}
        });
        
        errorLoadingDashboards = !loadExistingDashboards();
        
        setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                tabSelectDashboards.getItems().forEach(dbd -> {
                    try{
                        if (dbd.isDisplayed()){
                            dbd.getDashboard().close();
                        }
                        dbd.setSelected(tabSelectDashboards.getSelectionModel().isSelected(tabSelectDashboards.getItems().indexOf(dbd)));
                        dbd.store();
                    }
                    catch(Exception exc){
                        Log.error("Error: ", exc);
                    }
                }); 
                if (dashboardEditor != null){
                    dashboardEditor.close();
                }
            }      
        });
        
        addEventHandler(DashboardEditor.DashboardCreationEvent.DASHBOARD_CREATED, new EventHandler<DashboardEditor.DashboardCreationEvent>(){
            @Override
            public void handle(DashboardEditor.DashboardCreationEvent event) {
                dashboardDatas.add(event.getDashboardData());
                tabSelectDashboards.setItems(FXCollections.observableArrayList(dashboardDatas));
                //select all selected dashboards
                tabSelectDashboards.getSelectionModel().clearSelection();
                tabSelectDashboards.getItems().forEach(dbd -> {if (dbd.isSelected()) tabSelectDashboards.getSelectionModel().select(tabSelectDashboards.getItems().indexOf(dbd));});
                controlButtons();
                controlInfoText();                
            }
        });
        
        addEventHandler(Dashboard.DashboardClosedEvent.DASHBOARD_CLOSED, new EventHandler<Dashboard.DashboardClosedEvent>(){
            @Override
            public void handle(Dashboard.DashboardClosedEvent event) {
                controlButtons();
                controlInfoText();
            } 
        });
        
        tabSelectDashboards.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<DashboardData>() {
			@Override
			public void onChanged(Change<? extends DashboardData> c) {
				controlButtons();
                controlInfoText();				
			}
        });
        
        tabSelectDashboards.setOnMousePressed(new EventHandler<MouseEvent>() {
           @Override 
           public void handle(MouseEvent mouseEvent) {
              if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
                try{
                    DashboardData dbd = tabSelectDashboards.getSelectionModel().getSelectedItem();
                    if (!dbd.isDisplayed()){
                        Dashboard dashboard = new Dashboard(dbd, getThis());
                        dashboard.show();
                    }
                }
                catch(Exception exc){
                    Log.error("Error", exc);
                }
                controlButtons();
                controlInfoText();
                mouseEvent.consume();
              }
           }
        });        
        
        contextMenu               = new ContextMenu();
        menutItemLaunchDashboard  = new MenuItem("launch");
        menutItemLaunchDashboard.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                DashboardData dbd = tabSelectDashboards.getSelectionModel().getSelectedItem();
                if (!dbd.isDisplayed()){
                    Dashboard dashboard = new Dashboard(dbd, getThis());
                    dashboard.show();
                }
                controlButtons();
                controlInfoText();
            }
        });
        menutItemDeleteDashboard  = new MenuItem("delete");
        menutItemDeleteDashboard.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                DashboardData dbd = tabSelectDashboards.getSelectionModel().getSelectedItem();
                dbd.delete();
                if (dbd.isDisplayed()){
                    //close dashboard, if displayed
                    dbd.getDashboard().close();                
                }
                dashboardDatas.remove(dbd);
                tabSelectDashboards.getItems().remove(dbd);
                controlButtons();
                controlInfoText();
            }
        });
        contextMenu.getItems().addAll(menutItemLaunchDashboard, menutItemDeleteDashboard);
        tabSelectDashboards.setContextMenu(contextMenu);
        
        scene = new Scene(contentPane,contentPane.getPrefWidth(), contentPane.getPrefHeight());
        setScene(scene);
        centerOnScreen();

        controlButtons();
        controlInfoText();
    }
    
    private DashboardLauncher getThis(){
        return this;
    }

    private void initComponents() {
        contentPane                 = new VBox();
        selectionControlPanel       = new HBox();
        selectionControlPanel.setPadding(new Insets(5,5,5,5));
        selectionControlPanel.setSpacing(5.0);
        btnSelectDeselect           = new ToggleButton();
        tabSelectDashboards         = new DashboardSelectionTable();
        creationControlPanel        = new HBox();
        creationControlPanel.setPadding(new Insets(5,5,5,5));
        creationControlPanel.setSpacing(5.0);      
        btnLaunchSelectedDashboards = new Button();
        btnCreateNew                = new Button();
        btnDeleteSelectedDashboards = new Button();
        infoTextPanel               = new HBox();
        infoTextPanel.setPadding(new Insets(5,5,5,5));
        infoTextPanel.setSpacing(5.0);              
        lbInfoText                  = new Label();
        btnSelectDeselect.setText("select all");
        btnSelectDeselect.setPrefWidth(120);
        btnSelectDeselect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnSelectDeselectActionPerformed(event);
            }
        });
        selectionControlPanel.getChildren().add(btnSelectDeselect);

        contentPane.getChildren().add(selectionControlPanel);
        contentPane.getChildren().add(tabSelectDashboards);

        creationControlPanel.setMaxWidth(400);
        creationControlPanel.setMaxHeight(29);
        creationControlPanel.setPrefSize(400, 29);

        btnLaunchSelectedDashboards.setText("launch selected");
        btnLaunchSelectedDashboards.setPrefWidth(120);
        btnLaunchSelectedDashboards.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnLaunchSelectedDashboardsActionPerformed(event);
            }
        });
        creationControlPanel.getChildren().add(btnLaunchSelectedDashboards);

        btnCreateNew.setText("create new");
        btnCreateNew.setPrefWidth(120);
        btnCreateNew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnCreateNewActionPerformed(event);
            }
        });
        creationControlPanel.getChildren().add(btnCreateNew);

        btnDeleteSelectedDashboards.setText("delete selected");
        btnDeleteSelectedDashboards.setPrefWidth(120);
        btnDeleteSelectedDashboards.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnDeleteSelectedDashboardsActionPerformed(event);
            }
        });
        creationControlPanel.getChildren().add(btnDeleteSelectedDashboards);

        contentPane.getChildren().add(creationControlPanel);

        infoTextPanel.setMaxWidth(1000);
        infoTextPanel.setMaxHeight(25);
        infoTextPanel.setMinWidth(125);
        infoTextPanel.setMaxHeight(29);
        infoTextPanel.setPrefSize(400, 29);
        lbInfoText.setText("Dies ist ein langer Text, der aber nicht so wichtig ist. .......");
        infoTextPanel.getChildren().add(lbInfoText);

        contentPane.getChildren().add(infoTextPanel);
    }
    
    private void btnLaunchSelectedDashboardsActionPerformed(Event event) {
        try{
            for (DashboardData dbd : tabSelectDashboards.getSelectionModel().getSelectedItems()){
                if (!dbd.isDisplayed()){
                    Dashboard dashboard = new Dashboard(dbd, this);
                    dashboard.show();
                }
            }   
        }
        catch(Exception exc){
            Log.error("Error", exc);
        }
        controlButtons();
        controlInfoText();
    }

    private void btnDeleteSelectedDashboardsActionPerformed(Event event) {
        final String OK     = "Ok";
        final String CANCEL = "Cancel";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Delete dashboards");
        alert.setHeaderText("Delete dashboards");
        alert.setContentText("do you really want to delete selected dashboards ?");
        String[] options = new String[]{OK, CANCEL};

        List<ButtonType> buttons = new ArrayList<>();
        for (String option : options) {
            buttons.add(new ButtonType(option));
        }
        alert.getButtonTypes().setAll(buttons);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().getText().equals(OK)) {
            List<DashboardData> selectedItems = tabSelectDashboards.getSelectionModel().getSelectedItems();
            for (DashboardData dbd : selectedItems){
                //remove file representing this dashboard
                dbd.delete();
                if (dbd.isDisplayed()){
                    //close dashboard, if displayed
                    dbd.getDashboard().close();                
                }
            }   
            dashboardDatas.removeAll(selectedItems);
            tabSelectDashboards.getItems().removeAll(selectedItems);
            tabSelectDashboards.getSelectionModel().clearSelection();
        }
        controlButtons();
        controlInfoText();
    }

    private void btnSelectDeselectActionPerformed(Event evt) {
        if (!btnSelectDeselect.isSelected()){
            tabSelectDashboards.getSelectionModel().clearSelection();
        }
        else{
            tabSelectDashboards.getSelectionModel().selectAll();
        }
        controlButtons();
        controlInfoText();
    }

    private void btnCreateNewActionPerformed(Event evt) {
        btnCreateNew.setDisable(true);
        dashboardEditor = new DashboardEditor(this);
        dashboardEditor.setX(getX() + 100);
        dashboardEditor.setY(getY() + 100);
        dashboardEditor.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @SuppressWarnings("unchecked")
					@Override
                    public void handle(WindowEvent e) {
                        btnCreateNew.setDisable(false);
                        //refresh list of dashboards
                        tabSelectDashboards.getItems().removeAll();
                        tabSelectDashboards.getSelectionModel().clearSelection();
                        //errorLoadingDashboards = !loadExistingDashboards();
                        if (dashboardDatas != null){
                            Collections.sort(dashboardDatas);
                            tabSelectDashboards.setItems(FXCollections.observableArrayList(dashboardDatas));
                        }
                        //select all previously selected dashboards
                        tabSelectDashboards.getItems().forEach(dbd -> {if (dbd.isSelected()) tabSelectDashboards.getSelectionModel().select(tabSelectDashboards.getItems().indexOf(dbd));});
                        
                        controlButtons();
                        controlInfoText();
                        dashboardEditor = null;
                    }
                });        
        dashboardEditor.show();
    }

    private void controlButtons(){
        btnLaunchSelectedDashboards.setDisable(errorLoadingDashboards || tabSelectDashboards.getItems().size() == 0 || !isAtLeastOneInvisibleDashboardSelected());
        btnDeleteSelectedDashboards.setDisable(errorLoadingDashboards || tabSelectDashboards.getItems().size() == 0 || !isAtLeastOneDashboardSelected());
        if (menutItemLaunchDashboard != null){
            boolean dashboardAlreadyLaunched = tabSelectDashboards.getSelectionModel() != null && 
                                               tabSelectDashboards.getSelectionModel().getSelectedItem() != null && 
                                               tabSelectDashboards.getSelectionModel().getSelectedItem().isDisplayed();
            menutItemLaunchDashboard.setDisable(errorLoadingDashboards || tabSelectDashboards.getItems().size() == 0 || dashboardAlreadyLaunched);
        }
        if (menutItemDeleteDashboard != null){
            menutItemDeleteDashboard.setDisable(errorLoadingDashboards || tabSelectDashboards.getItems().size() == 0 || !isAtLeastOneDashboardSelected());
        }
        if (errorLoadingDashboards){
            btnSelectDeselect.setDisable(true);
        } else {
            if (tabSelectDashboards.getItems().size() > 0){
                btnSelectDeselect.setDisable(false);            
                if (isAtLeastOneDashboardSelected()){
                    btnSelectDeselect.setSelected(true);
                    btnSelectDeselect.setText("deselect all");
                }
                else{
                    btnSelectDeselect.setSelected(false);
                    btnSelectDeselect.setText("select all");                
                }
            }
            else{
                btnSelectDeselect.setDisable(true);            
                btnSelectDeselect.setSelected(false);
                btnSelectDeselect.setText("select all");                            
            }
        }
    }
    
    private void controlInfoText(){
        if (errorLoadingDashboards){
            lbInfoText.setText("error occured while loading dashboards");
            lbInfoText.setStyle("-fx-text-fill: red;");     
            errorLoadingDashboards = false;
        } else if (errorStoringDashboard){
            lbInfoText.setText("error occured while storing dashboard");
            lbInfoText.setStyle("-fx-text-fill: red;");                                         
            errorStoringDashboard = false;
        } 
        else {
            if (tabSelectDashboards.getItems().size() > 0){
                if (isAtLeastOneDashboardSelected()){
                    lbInfoText.setText("launch selected dashboards");
                    lbInfoText.setStyle("-fx-text-fill: black;");
                }
                else{
                    lbInfoText.setText("select dashboards to launch");
                    lbInfoText.setStyle("-fx-text-fill: black;");                                    
                }
            }
            else{
                lbInfoText.setText("no dashboards available");
                lbInfoText.setStyle("-fx-text-fill: black;");                                    
            }
        }
    }
        
    private boolean isAtLeastOneDashboardSelected(){
        return !tabSelectDashboards.getSelectionModel().isEmpty();
    }

    private boolean isAtLeastOneInvisibleDashboardSelected(){
        int  numberOfSelectedInvisibleDashboards = 0;
        List<DashboardData> selectedItems = tabSelectDashboards.getSelectionModel().getSelectedItems();
        for (DashboardData dbd : selectedItems){
            if (dbd != null && !dbd.isDisplayed()){
                numberOfSelectedInvisibleDashboards++;
            }
        }   
        return numberOfSelectedInvisibleDashboards > 0;
    }

    private boolean loadExistingDashboards(){
        boolean success = false;
        try{
            dashboardDatas = DashboardData.getAvailableDashboards();
            errorLoadingDashboards = false;
            errorStoringDashboard  = false;
            if (dashboardDatas != null){
                tabSelectDashboards.setItems(FXCollections.observableArrayList(dashboardDatas));
                //select all previously selected dashboards
                tabSelectDashboards.getItems().forEach(dbd -> {if (dbd.isSelected()) tabSelectDashboards.getSelectionModel().select(tabSelectDashboards.getItems().indexOf(dbd));});
            }   
            success = true;
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            success = false;
        }
        return success;
    }
    
    public void selectAllListedDashboards(boolean select){
        tabSelectDashboards.getSelectionModel().selectAll();
        saveSelectedStateOfDashboards();
        controlButtons();
        controlInfoText();
    }
    
    public void saveSelectedStateOfDashboards(){
        try{
            tabSelectDashboards.getItems().forEach(dbd -> dbd.setSelected(false));
            tabSelectDashboards.getSelectionModel().getSelectedItems().forEach(dbd -> dbd.setSelected(true));
            for (DashboardData dbd: tabSelectDashboards.getItems()){
                dbd.store();
            }   
        }
        catch(FileNotFoundException exc){
            errorStoringDashboard = true;
        }        
        catch(IOException exc){
            errorStoringDashboard = true;
        }        
    }            
}
