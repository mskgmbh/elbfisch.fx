/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Dashboard.java
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.SignalNotRegisteredException;

/**
 *
 * @author berndschuster
 */
public class Dashboard extends Stage {
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 
    private static final int  SLIDERWIDTH         = 20;
    private static final int  DEFAULTMAXNUMOFROWS = 20;
    private static final int  STAGETITLEBARHEIGHT = 40;//estimate
    private static final int  TABLETITLEBARHEIGHT = 60;//estimate
    private static final int  CONTROLPANELHEIGHT  = 45;//estimate
    
//    static  HashMap<String, Dashboard> displayedDashboards;
    
    private DashboardData     dashboardData;
    private boolean           dashboardSaved;
    private boolean           failedToSaveDashboardSaved;
    private boolean           someSignalsNotFound;
    private Scene             scene;
    private VBox              contentPanel;
    private HBox              bottomControlPanel;
    private Button            btnClose;
    private ToggleButton      btnConnect;
    private Button            btnSaveDashboard;
    private HBox              buttonControlPanel;
    private HBox              infoTextPanel;
    private Label             lbHideHierarchyLevel;
    private Label             lbInfoText;
    private SignalTable       signalTable;
    private Spinner<Integer>  spnHierarchyLevel;
    private DashboardLauncher dashboardLauncher;
    
    public Dashboard(DashboardData dashboardData, DashboardLauncher dashboardLauncher) throws SignalNotRegisteredException {
        initComponents();
        this.initStyle(StageStyle.DECORATED);
        controlInfoText(); 
        dashboardSaved             = false;
        failedToSaveDashboardSaved = false;
        showConnectButton(true);
        showSaveDashboardButton(true);
        showCloseButton(true);
        showHierarchyLevelControls(true);
        setScene(scene);
        this.dashboardData     = dashboardData;
        this.dashboardLauncher = dashboardLauncher;
        signalTable.addEntries(dashboardData);
        someSignalsNotFound = signalTable.isSomeSignalsNotFound();
//        //replace list of items with corresponding SortedList
//        SortedList<SignalListItem> sortedList = new SortedList<>(signalTable.getItems(), (SignalListItem s1, SignalListItem s2) -> s1.getSignalIdentifier().compareTo(s2.getSignalIdentifier()));
//        sortedList.comparatorProperty().bind(signalTable.comparatorProperty());
//        signalTable.setItems(sortedList);
        
        this.setTitle("Dashboard for " + dashboardData.getName());
        if (dashboardData.getDialogSize() != null){
            this.setWidth(dashboardData.getDialogSize().getWidth());
            this.setMaxHeight(CONTROLPANELHEIGHT + TABLETITLEBARHEIGHT + signalTable.getPreferredCellHeight() * signalTable.getItems().size() + 2 * CONTROLPANELHEIGHT);
            this.setHeight(dashboardData.getDialogSize().getHeight());
        }
        else{
            this.setWidth(signalTable.getDefaultWidth() + SLIDERWIDTH);
            int numOfRows = signalTable.getItems().size() > DEFAULTMAXNUMOFROWS ? DEFAULTMAXNUMOFROWS : signalTable.getItems().size();
            double height = CONTROLPANELHEIGHT + TABLETITLEBARHEIGHT + signalTable.getPreferredCellHeight() * numOfRows + 2 * CONTROLPANELHEIGHT;
            this.setMaxHeight(CONTROLPANELHEIGHT + TABLETITLEBARHEIGHT + signalTable.getPreferredCellHeight() * signalTable.getItems().size() + 2 * CONTROLPANELHEIGHT);
            this.setHeight(height);
            dashboardData.setDialogSize(new Dimension2D(this.getWidth(), this.getHeight()));
        }
        if (dashboardData.getScreenPosition() != null){
            Rectangle2D screenDimension = Screen.getPrimary().getVisualBounds();
            int x = (int)dashboardData.getScreenPosition().getX();
            if (x + getWidth() > screenDimension.getWidth()){
                x = (int)(screenDimension.getWidth() - getWidth());
            }
            int y = (int)dashboardData.getScreenPosition().getY();
            if (y + getHeight() > screenDimension.getHeight()){
                y = (int)(screenDimension.getHeight() - getHeight());
            }
            this.setX(x);
            this.setY(y);
        } else {
            dashboardData.setScreenPosition(new Point2D(this.getX(), this.getY()));        	
        }
        if (dashboardData.getIdentifierColumnWidth() > 0){
            signalTable.identifierColumn.setPrefWidth(dashboardData.getIdentifierColumnWidth());
        } else {
        	dashboardData.setIdentifierColumnWidth((int)signalTable.identifierColumn.getPrefWidth());
        }
        if (dashboardData.getStateColumnWidth() > 0){
            signalTable.stateColumn.setPrefWidth(dashboardData.getStateColumnWidth());
        } else {
        	dashboardData.setStateColumnWidth((int)signalTable.stateColumn.getPrefWidth());
        }
        if (dashboardData.getEditColumnWidth() > 0){
            signalTable.editColumn.setPrefWidth(dashboardData.getEditColumnWidth());
        } else {
        	dashboardData.setEditColumnWidth((int)signalTable.editColumn.getPrefWidth());
        }
        
        setOnShowing(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                if (dashboardData != null){
                    dashboardData.setDashboard(getThisDashboard());
                    if (dashboardData.isConnected()){
                        btnConnect.setSelected(true);
                        btnConnect.setText("disconnect");
                        signalTable.connect();
                    }
                }
            }
        });

        setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                prepareCloseOperation();
            }     
        });

        spnHierarchyLevel.getValueFactory().setValue(dashboardData.getHiddenHierarchyLevel());
        clipIdentifiers(spnHierarchyLevel.getValue());
        
        showConnectButton(dashboardData.isShowConnectButton());
        showSaveDashboardButton(dashboardData.isShowSaveDashboardButton());
        showCloseButton(dashboardData.isShowConnectButton());
        showHierarchyLevelControls(dashboardData.isShowHierarchyLevelControls());
        controlInfoText();
    }

    public Dashboard(String signalTableName) throws IOException, ClassNotFoundException, SignalNotRegisteredException {
        this(DashboardData.load(signalTableName), null);
    }

    private void initComponents() {
        setTitle("Dashboard");
        contentPanel          = new VBox();
        buttonControlPanel    = new HBox();
        signalTable           = new SignalTable();
        bottomControlPanel    = new HBox();
        infoTextPanel         = new HBox();
        signalTable.adjustDefaultStateCellSize(); 
        
        btnConnect = new ToggleButton();
        btnConnect.setText("connect");
        btnConnect.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleOnActionBtnConnect(event);
            }
        });        

        lbHideHierarchyLevel = new Label();
        lbHideHierarchyLevel.setText("  hide hierarchy level of identifier");
        lbHideHierarchyLevel.setAlignment(Pos.CENTER);
        
        spnHierarchyLevel = new Spinner();
        spnHierarchyLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9));
        spnHierarchyLevel.setPrefWidth(Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault()).computeStringWidth("XXXX") + 20);
        spnHierarchyLevel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                                @Override
                                                public void handle(MouseEvent event) {
                                                    handleOnMouseClickedEventSpnHierarchyLevel(event);
                                                }
                                            });

        buttonControlPanel.setPadding(new Insets(5,5,5,5));
        buttonControlPanel.setSpacing(5.0);
        buttonControlPanel.getChildren().addAll(btnConnect, lbHideHierarchyLevel, spnHierarchyLevel);

        btnSaveDashboard = new Button();
        btnSaveDashboard.setText("save");
        btnSaveDashboard.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleOnActionBtnSave(event);
            }
        });        

//        btnClose = new Button();
//        btnClose.setText("close");
//        btnClose.setOnAction(new EventHandler<ActionEvent>() {    
//            @Override
//            public void handle(ActionEvent event) {
//                handleOnActionBtnClose(event);
//            }
//        });        
        bottomControlPanel.setPadding(new Insets(5,5,5,5));
        bottomControlPanel.setSpacing(5.0);
        bottomControlPanel.getChildren().add(btnSaveDashboard);
//        bottomControlPanel.getChildren().add(btnClose);
        
        lbInfoText = new Label();
        infoTextPanel.setPadding(new Insets(5,5,5,5));
        infoTextPanel.getChildren().addAll(lbInfoText);

        contentPanel.setPrefWidth(signalTable.getWidth());
        contentPanel.setPrefHeight(buttonControlPanel.getHeight() + signalTable.getHeight() + bottomControlPanel.getHeight() + infoTextPanel.getHeight());
        contentPanel.getChildren().addAll(buttonControlPanel, signalTable, bottomControlPanel, infoTextPanel);
        scene = new Scene(contentPanel,contentPanel.getPrefWidth(), contentPanel.getPrefHeight());
    }
    
    private Dashboard getThisDashboard(){
        return this;
    }
    
    private void handleOnActionBtnConnect(ActionEvent evt) {
        if (btnConnect.isSelected()){
            btnConnect.setText("disconnect");
            signalTable.connect();
        }
        else{
            btnConnect.setText("connect");
            signalTable.disconnect();
        }
        controlInfoText();
    }

//    private void handleOnActionBtnClose(ActionEvent evt) {
//        close();
//    }

    private void handleOnActionBtnSave(ActionEvent evt) {
        store();
        controlInfoText();
    }

    private void handleOnMouseClickedEventSpnHierarchyLevel(MouseEvent event) {
        clipIdentifiers(spnHierarchyLevel.getValue());
    }

    private void controlInfoText(){
        if (failedToSaveDashboardSaved){
            lbInfoText.setText("failed to save dashboard.");
            lbInfoText.setStyle("-fx-text-fill: red;");                        
            failedToSaveDashboardSaved = false;
        } else if (dashboardSaved){
            lbInfoText.setText("dashboard saved");
            lbInfoText.setStyle("-fx-text-fill: black;");                        
            dashboardSaved = false;
        } else if (someSignalsNotFound){
            lbInfoText.setText("some signals removed or changed. Recreate dashboard");
            lbInfoText.setStyle("-fx-text-fill: red;");                        
            dashboardSaved = false;
        }
        else{
            lbInfoText.setText("");
            lbInfoText.setStyle("-fx-text-fill: black;");                                    
        }
    }
        
    protected void clipIdentifiers(int hierarchyLevel){
        signalTable.getItems().forEach(s -> ((SignalListItem)s).clipVisibleIdentifier(hierarchyLevel));
    }
    
    protected void prepareCloseOperation(){
        if (atLeastOnePropertyChanged()){
            boolean save = askUserForSavingChanges();
            if (save){
                try{
                    store();
                }
                catch(Exception exc){
                    Log.error("Error ", exc);
                }
            }
        }
        dashboardData.setDashboard(null);
        if (dashboardLauncher != null){
            Event.fireEvent(dashboardLauncher, new DashboardClosedEvent(dashboardData));
        }
        if (signalTable.isConnected()){
            signalTable.disconnect();
        }                
    }
                
    public void showConnectButton(boolean show){
        btnConnect.setVisible(show);
    }

    public void showSaveDashboardButton(boolean show){
        btnSaveDashboard.setVisible(show);
    }
    
    public void showHierarchyLevelControls(boolean show){
        spnHierarchyLevel.setVisible(show);
        lbHideHierarchyLevel.setVisible(show);
    }
    
    public void showCloseButton(boolean show){
//        btnClose.setVisible(show);
    } 
    
    @Override
    public void close(){
        prepareCloseOperation();
        super.close();
    }
    
    public void store(){
        try{
            if (dashboardData != null){
                dashboardData.setScreenPosition(new Point2D(getX(),getY()));
                dashboardData.setDialogSize(new Dimension2D(getWidth(), getHeight()));
                dashboardData.setIdentifierColumnWidth((int)signalTable.identifierColumn.getWidth());
                dashboardData.setStateColumnWidth((int)signalTable.stateColumn.getWidth());
                dashboardData.setEditColumnWidth((int)signalTable.editColumn.getWidth());
                dashboardData.setConnected(signalTable.isConnected());
                dashboardData.setHiddenHierarchyLevel((Integer)spnHierarchyLevel.getValue());
                dashboardData.setShowConnectButton(btnConnect.isVisible());
                dashboardData.setShowSaveDashboardButton(btnSaveDashboard.isVisible());
  //              dashboardData.setShowCloseButton(btnClose.isVisible());
                //store items in the order of occurence inside the table view
                dashboardData.clear();
                signalTable.getItems().forEach(i -> dashboardData.add(i));
                if (dashboardLauncher == null){
                    //if not launched by desktop launcher
                    //save all properties to disk
                    //otherwise it is done by the launcher
                    dashboardData.store();
                }
                dashboardSaved = true;
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            failedToSaveDashboardSaved = true;
            
        }

    }
    
    protected boolean atLeastOnePropertyChanged(){
        boolean changed = false;
            if (dashboardData != null){
                //changed  = !dashboardData.getScreenPosition().equals(new Point2D(getX(),getY()));
                changed = !dashboardData.getDialogSize().equals(new Dimension2D(getWidth(), getHeight()));
                changed |= dashboardData.getIdentifierColumnWidth() != (int)signalTable.identifierColumn.getWidth();
                changed |= dashboardData.getStateColumnWidth()      != (int)signalTable.stateColumn.getWidth();
                changed |= dashboardData.getEditColumnWidth()       != (int)signalTable.editColumn.getWidth();
                changed |= dashboardData.getHiddenHierarchyLevel()  != spnHierarchyLevel.getValue();
            }        
        return changed;
    }

    protected boolean askUserForSavingChanges(){
        final String SAVE     = "Save";
        final String DISCARD  = "Discard";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Confirmation");
        alert.setHeaderText("you are about to drop changes to dashboard '" + dashboardData.getName() + "'");
        alert.setContentText("do you want to save changes ?");
        String[] options = new String[]{SAVE, DISCARD};

        List<ButtonType> buttons = new ArrayList<>();
        for (String option : options) {
            buttons.add(new ButtonType(option));
        }
        alert.getButtonTypes().setAll(buttons);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getText().equals(SAVE);
    }
    
    static class DashboardClosedEvent extends Event{
        protected DashboardData dashboardData;
        
        public static final EventType<DashboardClosedEvent> DASHBOARD_CLOSED = new EventType<>(Event.ANY, "DASHBOARD_CLOSED");

        public DashboardClosedEvent(DashboardData dashboardData) {
            super(DASHBOARD_CLOSED);
            this.dashboardData = dashboardData;
        }   

        public DashboardData getDashboardData() {
            return dashboardData;
        }
    }
}
