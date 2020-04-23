/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DashboardTester.java
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

package org.jpac.fx.test;

import java.io.File;
import javafx.stage.Stage;
import org.jpac.Module;
import org.jpac.SignalRegistry;
import org.jpac.fx.Dashboard;
import org.jpac.fx.DashboardData;
import org.jpac.fx.SignalListItem;

/**
 *
 * @author berndschuster
 */
public class DashboardTester extends ModuleTesterTemplate{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Module module = new SignalTableTestModule(null,"Main");
        module.start();
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try{
            Dashboard dashboard;
            DashboardData dbd = new DashboardData();
            dbd.setName("Main");
            if (new File("./cfg/dashboards/alle.dbd").exists()){
                dashboard = new Dashboard("alle");            
            }
            else{
                SignalRegistry.getInstance().getSignals().values().forEach((s) ->{
                    SignalListItem item = new SignalListItem(s.getQualifiedIdentifier());
                    dbd.add(item);
                });
                dashboard = new Dashboard(dbd, null);
            }
//            SignalListItem item = new SignalListItem("Main.toggleSignal","");
//            dbd.add(item);
//            dashboard = new Dashboard(dbd);
            dashboard.show();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }

}
