/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RequestButtonTester.java
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

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jpac.Module;
import org.jpac.fx.ControlPane;
import org.jpac.fx.RequestButton;
import org.jpac.fx.RequestButton.EnableRule;
/**
 *
 * @author berndschuster
 */
public class RequestButtonTester extends ModuleTesterTemplate{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Module module = new RequestButtonTestModule(null,"Main");
        module.start();
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        ControlPane ctrlPane = new ControlPane();
        ctrlPane.setIdentifier("Main");
        
        RequestButton requestBtn = new RequestButton(".buttonRequest");
        requestBtn.setText("request button");
        requestBtn.setEnableRule(EnableRule.WHILENOTACTIVE);
        requestBtn.setRequestedStyle("-fx-text-fill: blue;");
        requestBtn.setActiveStyle("-fx-base: red; -fx-text-fill: blue;");
        
        VBox root = new VBox();
        root.getChildren().add(requestBtn);

        ctrlPane.getChildren().add(root);

        Scene scene = new Scene(ctrlPane, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();

        ctrlPane.connect();
//        clickBtn.connect();
//        toggleBtn.connect();        
    }

}
