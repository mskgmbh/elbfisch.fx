/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DashboardData.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.geometry.Dimension2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class DashboardData extends ArrayList<SignalListItem> implements Serializable, Cloneable, Comparable{
    static  Logger Log = LoggerFactory.getLogger("jpac.fx"); 
    
    public   static String FILEDIRECTORY         = "./cfg/dashboards/";
    public   static String FILEEXTENSION         = ".dbd";
    private  static String ATLEASTONENONWORDCHAR = ".*[^\\w\\x2E]+.*";
    private String      name;
    private Double      screenPositionX;
    private Double      screenPositionY;
    private Double      dialogWidth;
    private Double      dialogHeight;
    private boolean     selected;                   
    private boolean     connected;
    private int         hiddenHierarchyLevel;
    private int         identifierColumnWidth;
    private int         stateColumnWidth;
    private int         editColumnWidth;
    private boolean     showConnectButton           = true;
    private boolean     showSaveDashboardButton     = true;
    private boolean     showSaveAsDashboardControls = true;
    private boolean     showHierarchyLevelControls  = true;
    private boolean     showCloseButton             = true;
    
    protected transient Dashboard   dashboard;
    /**
     * loads an existing signal list named "name"
     * @param name name of the existing signal list
     */
    public static DashboardData load(String name) throws IOException, ClassNotFoundException{
        DashboardData dashboardData = null;
        try{
            ObjectInputStream oi = new ObjectInputStream(new FileInputStream(FILEDIRECTORY + name + FILEEXTENSION));
            dashboardData = (DashboardData)oi.readObject();
            oi.close();        
        }
        catch(Exception exc){
            Log.error("failed to load dashboard {}. Removed it", name);
            //remove corrupt dashboard
            new File(FILEDIRECTORY + name + FILEEXTENSION).delete();
        }
        return dashboardData;
    }
    
    /**
     * stores signal list to a file named "name"
     * @param name name of the existing signal list
     */
    public void store(String name) throws FileNotFoundException, IOException{
        this.name = name;
        File pathFile = new File(FILEDIRECTORY);
        if (!pathFile.exists()){
            pathFile.mkdirs();
        }
        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILEDIRECTORY + name + FILEEXTENSION) );
        oo.writeObject(this);
        oo.close();        
    }
    
    /**
     * stores signal list to a file named by the signal lists name
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void store() throws FileNotFoundException, IOException{
        store(name);
    }
    
    /**
     * used to set the name of the signal list
     * @param name 
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * used to delete the file representing this signal list
     */
    public void delete(){
        File file = new File(FILEDIRECTORY + name + FILEEXTENSION);
        if (file.exists()){
            file.delete();
        }
    }
    
    public static ArrayList<DashboardData> getAvailableDashboards() throws IOException, ClassNotFoundException{
        ArrayList<DashboardData> availableDashboards = new ArrayList<>();
        File file = new File(FILEDIRECTORY);
        File[] files = file.listFiles();
        if (files != null && files.length > 0){
            for (File f: files){
                if (f.getName().endsWith(FILEEXTENSION)){
                    String dashboardName = f.getName().replaceFirst(FILEEXTENSION, "");
                    DashboardData dbd = DashboardData.load(dashboardName);
                    if (dbd != null){
                        availableDashboards.add(dbd);
                    }
                }
            }
        }
        return availableDashboards;
    }
    
    public static boolean isValidName(String name){
        return !name.matches(ATLEASTONENONWORDCHAR);
    }
    
    public static boolean alreadyExists(String name){
        String filePath = FILEDIRECTORY + name + FILEEXTENSION;
        boolean exists  = new File(filePath).exists();
        return exists;
    }

    public String getName(){
        return this.name;
    }
    /**
     * @return the screenPosition
     */
    public Point2D getScreenPosition() {
        Point2D point = null;
        if (screenPositionX != null && screenPositionY != null){
            point = new Point2D(screenPositionX,screenPositionY);
        }
        return point;
    }

    /**
     * @param screenPosition the screenPosition to set
     */
    public void setScreenPosition(Point2D screenPosition) {
        this.screenPositionX = screenPosition.getX();
        this.screenPositionY = screenPosition.getY();
    }

    /**
     * @return the dialogSize
     */
    public Dimension2D getDialogSize() {
        Dimension2D dim = null;
        if (dialogWidth != null && dialogHeight != null){
            dim = new Dimension2D(dialogWidth, dialogHeight);
        }
        return dim;
    }

    /**
     * @param dialogSize the dialogSize to set
     */
    public void setDialogSize(Dimension2D dialogSize) {
        this.dialogWidth  = dialogSize.getWidth();
        this.dialogHeight = dialogSize.getHeight();
    }    
     /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + name + ") " + super.toString();
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the hiddenHierarchyLevel
     */
    public int getHiddenHierarchyLevel() {
        return hiddenHierarchyLevel;
    }

    /**
     * @param hiddenHierarchyLevel the hiddenHierarchyLevel to set
     */
    public void setHiddenHierarchyLevel(int hiddenHierarchyLevel) {
        this.hiddenHierarchyLevel = hiddenHierarchyLevel;
    }

    /**
     * @return the stateColumnWidth
     */
    public int getStateColumnWidth() {
        return stateColumnWidth;
    }

    /**
     * @param stateColumnWidth the stateColumnWidth to set
     */
    public void setStateColumnWidth(int stateColumnWidth) {
        this.stateColumnWidth = stateColumnWidth;
    }

    /**
     * @return the editColumnWidth
     */
    public int getEditColumnWidth() {
        return editColumnWidth;
    }

    /**
     * @param editColumnWidth the editColumnWidth to set
     */
    public void setEditColumnWidth(int editColumnWidth) {
        this.editColumnWidth = editColumnWidth;
    }

    /**
     * @return the identifierColumnWidth
     */
    public int getIdentifierColumnWidth() {
        return identifierColumnWidth;
    }

    /**
     * @param identifierColumnWidth the identifierColumnWidth to set
     */
    public void setIdentifierColumnWidth(int identifierColumnWidth) {
        this.identifierColumnWidth = identifierColumnWidth;
    }

    /**
     * @return the showConnectButton
     */
    public boolean isShowConnectButton() {
        return showConnectButton;
    }

    /**
     * @param showConnectButton the showConnectButton to set
     */
    public void setShowConnectButton(boolean showConnectButton) {
        this.showConnectButton = showConnectButton;
    }

    /**
     * @return the showSaveDashboardButton
     */
    public boolean isShowSaveDashboardButton() {
        return showSaveDashboardButton;
    }

    /**
     * @param showSaveDashboardButton the showSaveDashboardButton to set
     */
    public void setShowSaveDashboardButton(boolean showSaveDashboardButton) {
        this.showSaveDashboardButton = showSaveDashboardButton;
    }

    /**
     * @return the showSaveAsDashboardButton
     */
    public boolean isShowSaveAsDashboardControls() {
        return showSaveAsDashboardControls;
    }

    /**
     * @param showSaveAsDashboardControls the showSaveAsDashboardButton to set
     */
    public void setShowSaveAsDashboardControls(boolean showSaveAsDashboardControls) {
        this.showSaveAsDashboardControls = showSaveAsDashboardControls;
    }

    /**
     * @return the showCloseButton
     */
    public boolean isShowCloseButton() {
        return showCloseButton;
    }

    /**
     * @param showCloseButton the showCloseButton to set
     */
    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
    }

    /**
     * @return the showHierarchyLevelControls
     */
    public boolean isShowHierarchyLevelControls() {
        return showHierarchyLevelControls;
    }

    /**
     * @param showHierarchyLevelControls the showHierarchyLevelControls to set
     */
    public void setShowHierarchyLevelControls(boolean showHierarchyLevelControls) {
        this.showHierarchyLevelControls = showHierarchyLevelControls;
    }

    /**
     * @return the displayed
     */
    public boolean isDisplayed() {
        return dashboard != null;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public Dashboard getDashboard() {
        return this.dashboard;
    }

    @Override
    public int compareTo(Object o) {
        return name.compareTo(((DashboardData)o).getName());
    }
}
