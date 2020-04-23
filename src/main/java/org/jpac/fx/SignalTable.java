/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignalTable.java
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.jpac.SignalNotRegisteredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class SignalTable extends TableView<SignalListItem> implements Connectable{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 
    
    public  final static int        IDENTIFIERCOLUMN     = 0;
    public  final static int        STATECOLUMN          = 1;
    //public  final static int UNITCOLUMN       = 2;
    public  final static int        EDITCOLUMN           = 2;
    private final static DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    
    protected TableColumn<SignalListItem, String>  identifierColumn;
    protected TableColumn<SignalListItem, Control> stateColumn;
    protected TableColumn<SignalListItem, Control> editColumn;
    
    private String         identifier;
    private boolean        connected;
    private DashboardData  dashboardData;
    private StackPane      draggedRowPane;
    private boolean        someSignalNotFound;
    
    public SignalTable(){
        super();
        
        this.identifierColumn   = new TableColumn<>("Signal");
        this.stateColumn        = new TableColumn<>("State");
        this.editColumn         = new TableColumn<>("Edit");
        this.someSignalNotFound = false;
        
        identifierColumn.setPrefWidth(50);
        identifierColumn.setResizable(true);
        identifierColumn.setSortable(false);
        identifierColumn.setCellValueFactory(new SignalIdentifierFactory());
        identifierColumn.setId("identifierColumn");
        stateColumn.setResizable(true);
        stateColumn.setSortable(false);
        stateColumn.setCellValueFactory(new SignalStateFactory());
        stateColumn.setId("stateColumn");
        editColumn.setResizable(true);
        editColumn.setSortable(false);        
        editColumn.setCellValueFactory(new SignalEditFactory());
        editColumn.setId("editColumn");

        identifierColumn.setCellValueFactory(new PropertyValueFactory<>("visibleIdentifier"));
        
        stateColumn.setCellFactory(new Callback<TableColumn<SignalListItem, Control>, TableCell<SignalListItem, Control>>() {
            @Override
            public TableCell<SignalListItem, Control> call(TableColumn<SignalListItem, Control> param) {
                return new SignalStateCell(connected);
            }
        });

        editColumn.setCellFactory(new Callback<TableColumn<SignalListItem, Control>, TableCell<SignalListItem, Control>>() {
            @Override
            public TableCell<SignalListItem, Control> call(TableColumn<SignalListItem, Control> param) {
                return new SignalEditCell(connected);
            }
        });

        getColumns().addAll(identifierColumn, stateColumn, editColumn);    

        setRowFactory((TableView<SignalListItem> tv) -> {
            TableRow<SignalListItem> row = new TableRow<>();
            
            row.setOnDragDetected((MouseEvent event) -> {
                if (! row.isEmpty()) {
                    try{
                        Integer index = row.getIndex();
                        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                        db.setDragView(getDraggedRowImage(getItems().get(index).getVisibleIdentifier()));
                        ClipboardContent cc = new ClipboardContent();
                        cc.put(SERIALIZED_MIME_TYPE, index);
                        db.setContent(cc);
                        event.consume();
                    }
                    catch(Exception exc){
                        Log.error("Error: ", exc);
                    }
                }
            });
            
            row.setOnDragEntered(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        getSelectionModel().clearAndSelect(row.getIndex());
                        event.consume();
                    }
                }
            });
            
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    SignalListItem draggedItem = getItems().remove(draggedIndex);

                    int dropIndex ; 

                    if (row.isEmpty()) {
                        dropIndex = getItems().size() ;
                    } else {
                        dropIndex = row.getIndex();
                    }

                    getItems().add(dropIndex, draggedItem);

                    event.setDropCompleted(true);
                    getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row ;
        });
    }        
        
    public void adjustDefaultStateCellSize(){
        FontMetrics fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault());
        
        double width  = fontMetrics.computeStringWidth("XXXXXXXXXXXX");
        identifierColumn.setResizable(true);
        stateColumn.setPrefWidth(width);
        editColumn.setPrefWidth(width);
        identifierColumn.setPrefWidth(fontMetrics.computeStringWidth("Main.AnyModule.AnySubModule.Signal"));
    }

    public double getDefaultWidth(){
        return identifierColumn.getPrefWidth() + stateColumn.getPrefWidth() + editColumn.getPrefWidth();
    }
    
    @Override
    public String getQualifiedIdentifier() throws QualifiedIdentifierException {
        return HmiUtitilities.getQualifiedIdentifier(this);
    }

    @Override
    public void connect() {
        connectContainedConnnectables(true);
        connected = true;
    }

    @Override
    public void disconnect() {
        connectContainedConnnectables(false);
        connected = false;
    }
    
    protected double getPreferredCellHeight(){
        double cellHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(Font.getDefault()).getLineHeight() + 10;
        return cellHeight;
    }
    
    protected void connectContainedConnnectables(boolean connect){
        for (Node row: lookupAll(".table-row-cell")){
            for (Node cell: row.lookupAll(".table-cell")){
                Node node = ((TableCell) cell).getGraphic();
                if (node instanceof Connectable){
                    if (connect){
                        ((Connectable)node).connect();
                    }
                    else{
                        ((Connectable)node).disconnect();                    
                    }
                }
            }
        }
    }
    
    protected Image getDraggedRowImage(String text) {
        if (draggedRowPane == null){
            draggedRowPane = new StackPane();
            Rectangle rect = new Rectangle();
            draggedRowPane.setAlignment(Pos.CENTER_LEFT);
            draggedRowPane.setPadding(new Insets(5,2,5,2));
            rect.setWidth(identifierColumn.getPrefWidth());
            rect.setHeight(getPreferredCellHeight());
            rect.setFill(Color.STEELBLUE);
            Text txt = new Text(text);
            txt.setId("txt");
            txt.setFont(Font.getDefault());
            txt.setFill(Color.WHITE);
            draggedRowPane.getChildren().addAll(rect, txt);
        }
        else{
            Text txt = (Text)draggedRowPane.getChildren().get(1);
            txt.setText(text);
        }
        Image img = draggedRowPane.snapshot(new SnapshotParameters(), null);        
        return img;
    }
    
    public void addEntry(SignalListItem listItem) throws SignalNotRegisteredException{
            getItems().add(listItem);
    }

    public void addEntries(DashboardData dashboardData) throws SignalNotRegisteredException{
        this.dashboardData = dashboardData;
        for(SignalListItem item: dashboardData){
            if (item.instantiateControls()){
                addEntry(item);
            }
            else{
                someSignalNotFound = true;
            }
        }
        setPrefHeight((getPreferredCellHeight() + 5) * getItems().size() + 30);
    }
    
    /**
     * @return the identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public boolean isConnected(){
        return this.connected;
    }

    /**
     * @return the dashboardData
     */
    public DashboardData getDashboardData() {
        return dashboardData;
    }

    /**
     * @param dashboardData the dashboardData to set
     */
    public void setDashboardData(DashboardData dashboardData) {
        this.dashboardData = dashboardData;
    }    
    
    public boolean isSomeSignalsNotFound(){
        return someSignalNotFound;
    }
        
    class SignalIdentifierFactory implements Callback<TableColumn.CellDataFeatures<SignalListItem, String>, ObservableValue<String>> {
        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<SignalListItem, String> data) {
            return new ReadOnlyObjectWrapper<>(data.getValue().getSignalIdentifier());
        }
    }

    class SignalStateFactory implements Callback<TableColumn.CellDataFeatures<SignalListItem, Control>, ObservableValue<Control>> {
        @Override
        public ObservableValue<Control> call(TableColumn.CellDataFeatures<SignalListItem, Control> data) {
            return new ReadOnlyObjectWrapper<>(data.getValue().getStateControl());
        }
    }

    class SignalEditFactory implements Callback<TableColumn.CellDataFeatures<SignalListItem, Control>, ObservableValue<Control>> {
        @Override
        public ObservableValue<Control> call(TableColumn.CellDataFeatures<SignalListItem, Control> data) {
            return new ReadOnlyObjectWrapper<>(data.getValue().getEditControl());
        }
    }

    class SignalStateCell extends TableCell<SignalListItem, Control> {
        Control control = null;
        boolean connectInstantly;
        
        public SignalStateCell(boolean connectInstantly){
            super();
            this.connectInstantly = connectInstantly;
        }
                                                
        @Override
        protected void updateItem(Control item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty){
                setText(null);
                setGraphic(null);
            }
            else{
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);                        
                setPadding(Insets.EMPTY);
                control = item;
                control.setMinHeight(5);
                control.setPrefHeight(getPreferredCellHeight());
                control.prefWidthProperty().bind(this.widthProperty());
                if (connectInstantly && !((Connectable)control).isConnected()){
                    ((Connectable)control).connect();
                }
                setGraphic(control);                    
            }
        }
    }

    class SignalEditCell extends TableCell<SignalListItem, Control> {
        Control control = null;
        boolean connectInstantly;
        
        public SignalEditCell(boolean connectInstantly){
            super();
            this.connectInstantly = connectInstantly;
        }
                        
        @Override
        protected void updateItem(Control item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty){
                setText(null);
                setGraphic(null);
            }
            else{
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);                        
                setPadding(Insets.EMPTY);
                control = item;
                control.setMinHeight(5);
                control.setPrefHeight(getPreferredCellHeight());
                control.prefWidthProperty().bind(this.widthProperty());
                if (connectInstantly && !((Connectable)control).isConnected()){
                    ((Connectable)control).connect();
                }
                setGraphic(control);                    
            }
        }
    }
}
