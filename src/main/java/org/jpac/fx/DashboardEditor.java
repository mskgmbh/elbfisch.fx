/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DashboardEditor.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jpac.Signal;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.SignalRegistry;
/**
 *
 * @author berndschuster
 */
public class DashboardEditor extends Stage {
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 

    private static final int SLIDERWIDTH                    = 20;
    private static final int TABLETITLEBARHEIGHT            = 60;//estimate
    private static final int CONTROLPANELHEIGHT             = 40;//estimate 
    private static final int DEFAULTNUMBEROFTABLEENTRIES    = 15;
    private static final int MAXNUMBEROFVISIBLETABLEENTRIES = 30;
    
    private static final String STACKTRACEQUALIFIER         = ":StackTrace";
    
    static final String WILDCARD = "\\x2A";
    enum CharacterClasses {ALPHANUMERIC("\\p{Alnum}*"),
                           DOT("\\x2E"),
                           BACKSLASH("\\"),
                           ANY(".*");
         String value;
         CharacterClasses(String value){
             this.value = value;
         }
         
         public String getValue(){
             return value;
         }
    };

    private VBox                 contentPanel;
    private Button               btnCreateDashboard;
    private Button               btnSelectDeselect;
    private CheckBox             cbWithStacktraces;
    private HBox                 creationControlPanel;
    private HBox                 infoTextPanel;
    private Label                label1 ;
    private Label                lbInfoText;
    private HBox                 selectionControlPanel;
    private SignalSelectionTable tabSelectSignals;
    private TextField            tfDashboardName;
    private TextField            tfSearchPattern;  
    private Scene                scene;
    
    private SignalRegistry       signalRegistry;
    private boolean              btnSelectDeselectToggledToSelected;
    private boolean              dashboardNameValid;
    private boolean              dashboardAlreadyExists;
    private boolean              atLeastOneSignalSelected;

    private DashboardLauncher    dashboardLauncher;
    
    /**
     * Creates new form DashboardEditor
     */
    public DashboardEditor(DashboardLauncher dashboardLauncher) {
        initComponents();
        this.initStyle(StageStyle.DECORATED);
        this.dashboardLauncher                  = dashboardLauncher;
        this.signalRegistry                     = SignalRegistry.getInstance();
        this.btnSelectDeselectToggledToSelected = true;
        this.dashboardNameValid                 = false;
        this.dashboardAlreadyExists             = false;

        btnSelectDeselect.setDisable(true);
        btnCreateDashboard.setDisable(true);
        
        controlInfoText();  
    }

    private void initComponents() {
        tfSearchPattern       = new TextField();
        selectionControlPanel = new HBox();
        cbWithStacktraces     = new CheckBox();
        btnSelectDeselect     = new Button();
        tabSelectSignals      = new SignalSelectionTable();
        creationControlPanel  = new HBox();
        btnCreateDashboard    = new Button();
        label1                = new Label();
        tfDashboardName       = new TextField();
        infoTextPanel         = new HBox();
        lbInfoText            = new Label();

        tfSearchPattern.setEditable(true);
        tfSearchPattern.setPrefSize(250,20);
        tfSearchPattern.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    tfSearchPatternActionPerformed(event);             
                }
            }
        );
        
        tfSearchPattern.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
            	controlDashboardName();          
            }
        }
    );

        cbWithStacktraces.setText("with stack traces");
        cbWithStacktraces.setPrefWidth(130.0);
        cbWithStacktraces.setSelected(false);
        cbWithStacktraces.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cbWithStacktracesActionPerformed(event);
            }
        });
                        
        btnSelectDeselect.setText("select all");
        btnSelectDeselect.setPrefSize(116.0, 29.0);
        btnSelectDeselect.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    btnSelectDeselectActionPerformed(event);
                }
            }
        );
        
        selectionControlPanel.setPadding(new Insets(5,5,5,5));
        selectionControlPanel.setSpacing(5.0);
        selectionControlPanel.setAlignment(Pos.CENTER_LEFT);
        selectionControlPanel.getChildren().addAll(tfSearchPattern, cbWithStacktraces, btnSelectDeselect);

        btnCreateDashboard.setText("create dashboard");
        btnCreateDashboard.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    btnCreateDashboardActionPerformed(event);
                }
            }
        );

        label1.setText("as");
        
        tabSelectSignals.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SignalSelectionData>() {
			@Override
			public void onChanged(Change<? extends SignalSelectionData> c) {
                atLeastOneSignalSelected = !tabSelectSignals.getSelectionModel().isEmpty();
                controlCreateDashboardButton();
                controlInfoText();                
			}
        });
        

        tfDashboardName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                tfDashboardNameChanged();
			}
        });
                                        
        creationControlPanel.setPadding(new Insets(5,5,5,5));
        creationControlPanel.setSpacing(5.0);        
        creationControlPanel.getChildren().addAll(btnCreateDashboard, label1, tfDashboardName);

        lbInfoText.setAlignment(Pos.BASELINE_LEFT);
        lbInfoText.setText("This is a long and boring text .......");
        lbInfoText.setPrefSize(creationControlPanel.getMinWidth(), 35.0);
        
        infoTextPanel.setPrefSize(400.0, 25.0);
        infoTextPanel.setPadding(new Insets(5,5,5,5));
        infoTextPanel.setSpacing(5.0);        
        infoTextPanel.getChildren().addAll(lbInfoText);

        contentPanel = new VBox();
        contentPanel.setPrefWidth(selectionControlPanel.getPrefWidth());

        contentPanel.getChildren().addAll(selectionControlPanel, tabSelectSignals, creationControlPanel, infoTextPanel);
        scene = new Scene(contentPanel,contentPanel.getPrefWidth(), contentPanel.getPrefHeight());
        setScene(scene);
    }
    
    

    private void tfSearchPatternActionPerformed(Event evt) {
        handleSearchPatternActionPerformed(evt);
    }
    
    private void btnSelectDeselectActionPerformed(Event eventt) {
        if (btnSelectDeselectToggledToSelected){
            selectAllListedSignals(true);
            btnSelectDeselect.setText("deselect all");
            btnSelectDeselectToggledToSelected = false;
        }
        else{
            selectAllListedSignals(false);
            btnSelectDeselect.setText("select all");            
            btnSelectDeselectToggledToSelected = true;
        }
    }
  
    private void cbWithStacktracesActionPerformed(Event event) {
    	handleSearchPatternActionPerformed(event);
    }

    private void btnCreateDashboardActionPerformed(Event event) {
        if (!DashboardData.alreadyExists(tfDashboardName.getText())){
            DashboardData dashboardData = new DashboardData();
            tabSelectSignals.getSelectionModel().getSelectedItems().forEach(i -> dashboardData.add(new SignalListItem(i.getQualifiedIdentifier())));
            dashboardData.setName(tfDashboardName.getText());
            try{
                Dashboard dashboard = new Dashboard(dashboardData, dashboardLauncher);
                dashboard.show();
                if (dashboardLauncher != null){
                    Event.fireEvent(dashboardLauncher, new DashboardCreationEvent(dashboardData));
                }
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
        else{
            tfDashboardName.setStyle("-fx-text-fill: red;");                                
            dashboardNameValid = false;            
        }
        controlCreateDashboardButton();
        controlInfoText();
    }

    private void tfDashboardNameChanged() {
        if (!tfDashboardName.getText().isEmpty()){
            if (DashboardData.isValidName(tfDashboardName.getText())){
                if (!DashboardData.alreadyExists(tfDashboardName.getText())){
                    tfDashboardName.setStyle("-fx-text-fill: black;");
                    btnCreateDashboard.setDisable(tabSelectSignals.getItems().size() == 0);
                    dashboardNameValid     = true;
                    dashboardAlreadyExists = false;
                }
                else{
                    tfDashboardName.setStyle("-fx-text-fill: red;");
                    dashboardNameValid     = true;
                    dashboardAlreadyExists = true;
                }
            }
            else{
                tfDashboardName.setStyle("-fx-text-fill: red;");
                dashboardNameValid     = false;
            }
        }
        else{
            tfDashboardName.setStyle("-fx-text-fill: black;");
            dashboardNameValid     = false;            
        }
        controlCreateDashboardButton();
        controlInfoText();
    }

    private void controlCreateDashboardButton(){
        String dashboardName  = tfDashboardName.getText();
        boolean buttonEnabled = !dashboardName.isEmpty() && dashboardNameValid && isAtLeastOneSignalSelected();
        btnCreateDashboard.setDisable(!buttonEnabled);
    }
    
    private void controlInfoText(){
        if (tabSelectSignals.getItems().size() == 0){
            lbInfoText.setText("select signals, use '*' as wildcard, append dots ('.') to adjust levels");
            lbInfoText.setStyle("-fx-text-fill: black;");            
        } 
        else{
            if (!tfDashboardName.getText().isEmpty()){
                if (!dashboardNameValid){
                    lbInfoText.setText("invalid name for dashboard");
                    lbInfoText.setStyle("-fx-text-fill: red;");            
                } else if (dashboardAlreadyExists){
                    lbInfoText.setText("dashboard already exists");
                    lbInfoText.setStyle("-fx-text-fill: red;");            
                } else if (!isAtLeastOneSignalSelected()){
                    lbInfoText.setText("at least one signal must be selected");
                    lbInfoText.setStyle("-fx-text-fill: red;");            
                }
                else{
                    lbInfoText.setText("create dashboard ...");
                    lbInfoText.setStyle("-fx-text-fill: black;");            
                }
            }
            else{
                lbInfoText.setText("select name for the dashboard");
                lbInfoText.setStyle("-fx-text-fill: black;");            
            }
        }
    }
    
    private void controlDashboardName() {
        String ldbn;    
    	String dbn = "";
    	int    i   = 1;
    	String searchString = tfSearchPattern.getText();
    	if (!searchString.equals("")){
    		if (searchString.contentEquals("*")) {
    			dbn = "all";
    		} else {
    			dbn  = searchString.replaceAll(WILDCARD,"");
    			ldbn = dbn;
        		while(DashboardData.alreadyExists(ldbn)) {
    				ldbn = dbn + i++;
    			}
    			dbn = ldbn;
    		}
    	}
    	tfDashboardName.setText(dbn);
    }
    
    int level;
    private void handleSearchPatternActionPerformed(Event event){
        String  searchString;
        String  regexString;
        Pattern pattern;
        if (!tfSearchPattern.getText().equals("")){
            searchString = tfSearchPattern.getText();
            level        = computeLevel(searchString);
            if (level > 0) {
            	int stripIndex = searchString.length() > level ? searchString.length() - level : 0;
            	searchString = searchString.substring(0, stripIndex);//strip level indicators (trailing dots)
            }
            //prepare regular expression
            //replace dots (.), because they are interpreted as meta characters  
            regexString  = searchString.replaceAll(CharacterClasses.DOT.getValue(),CharacterClasses.BACKSLASH.getValue() + CharacterClasses.DOT.getValue());
            //replace wild card character by the corresponding regular expression
            regexString  = regexString.replaceAll(WILDCARD, CharacterClasses.ANY.getValue());
            //given string might occur elsewhere inside an signal identifier
            if (!regexString.startsWith(CharacterClasses.ANY.getValue())){
                regexString = CharacterClasses.ANY.getValue() + regexString;
            }
            if (!regexString.endsWith(CharacterClasses.ANY.getValue())){
                regexString = regexString +  CharacterClasses.ANY.getValue();
            }
            pattern = Pattern.compile(regexString);
            //clear table of selected signals
            tabSelectSignals.getItems().clear();
            //fill in matching signals
            List<Signal> listOfSignals = new ArrayList<>(signalRegistry.getSignals().values());
            Collections.sort( listOfSignals, new Comparator<Signal>(){
                @Override
                public int compare(Signal o1, Signal o2) {
                        return o1.getQualifiedIdentifier().compareTo(o2.getQualifiedIdentifier());
                    };
            });
            
            List<Signal> listOfMatchingSignals = listOfSignals.stream().filter(s -> pattern.matcher(s.getQualifiedIdentifier()).matches() && 
                    	!s.getQualifiedIdentifier().contains(Signal.PROXYQUALIFIER) &&
                    	!(!cbWithStacktraces.isSelected() && s.getQualifiedIdentifier().contains(STACKTRACEQUALIFIER))).collect(Collectors.toList());            
            //filter all signals which are deeper in hierarchy than the desired level
            if (level > 0) {
            	int minNumberOfDots = Integer.MAX_VALUE;
            	int dots            = 0;
            	for(Signal s: listOfMatchingSignals) {
            		dots = countDots(s.getQualifiedIdentifier());
            		if (dots < minNumberOfDots) minNumberOfDots = dots;
            	}
            	level = minNumberOfDots + level - 1;
                listOfMatchingSignals = listOfMatchingSignals.stream().filter(s -> countDots(s.getQualifiedIdentifier()) <= level).collect(Collectors.toList());
            }
            listOfMatchingSignals.forEach(s -> tabSelectSignals.addEntry(s));
            
            //reset buttons
            btnSelectDeselect.setDisable(tabSelectSignals.getItems().size() == 0);                                
            btnSelectDeselectToggledToSelected = false;
            btnSelectDeselect.setText("deselect all");
            selectAllListedSignals(true);
            adaptDialogHeight(tabSelectSignals.getItems().size());            
            controlCreateDashboardButton();            
            controlInfoText();
            this.sizeToScene();
        }
    }
    
    private int computeLevel(String searchString) {
		int level = 0;
		if (searchString.length() > 0) {
			int index = searchString.length() - 1;
			while(index > 0 && searchString.charAt(index--) == '.') level++;
		}
		return level;
	}
    
    private int countDots(String identifier) {
    	int dots = 0;
    	for(int i = 0; i < identifier.length(); i++) {
    		if (identifier.charAt(i) == '.') dots++;
    	}
    	return dots;
    }
    
    private boolean isAtLeastOneSignalSelected(){
        return atLeastOneSignalSelected;
    }

    private void adaptDialogHeight(int numberOfTableItems){
        int itemsCnt = numberOfTableItems > MAXNUMBEROFVISIBLETABLEENTRIES ? MAXNUMBEROFVISIBLETABLEENTRIES : numberOfTableItems;
        itemsCnt     = itemsCnt           < DEFAULTNUMBEROFTABLEENTRIES ? DEFAULTNUMBEROFTABLEENTRIES       : itemsCnt;
        tabSelectSignals.setPrefHeight(itemsCnt * tabSelectSignals.getPreferredCellHeight());
    }
    
    public void selectAllListedSignals(boolean select){
        if (select){
            tabSelectSignals.getSelectionModel().selectAll();
        }
        else{
            tabSelectSignals.getSelectionModel().clearSelection();
        }
        controlCreateDashboardButton();
        controlInfoText();
    } 
    
    static class DashboardCreationEvent extends Event{
		private static final long serialVersionUID = -3382204566827532407L;

		protected DashboardData dashboardData;
        
        public static final EventType<DashboardCreationEvent> DASHBOARD_CREATED = new EventType<>(Event.ANY, "DASHBOARD_CREATED");

        public DashboardCreationEvent(DashboardData dashboardData) {
            super(DASHBOARD_CREATED);
            this.dashboardData = dashboardData;
        }   

        public DashboardData getDashboardData() {
            return dashboardData;
        }
    }
}
