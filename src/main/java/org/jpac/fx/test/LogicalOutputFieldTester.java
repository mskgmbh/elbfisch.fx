/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerInputFieldTester.java
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
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jpac.Module;
import org.jpac.fx.ControlPane;
import org.jpac.fx.LogicalOutputField;
import org.jpac.fx.LogicalOutputField.LedColor;
import org.jpac.fx.LogicalOutputField.LedType;

/**
 *
 * @author berndschuster
 */
public class LogicalOutputFieldTester extends ModuleTesterTemplate{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Module module = new LogicalOutputFieldTestModule(null,"Main");
        module.start();
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try{
        ControlPane ctrlPane = new ControlPane();
        ctrlPane.setIdentifier("Main");
        LogicalOutputField outputField = new LogicalOutputField(".logicalSignal");
        outputField.setStyle("-fx-background-color: gray");
        outputField.setPrefSize(70, 20);
        outputField.setLedColor(LedColor.AMBER);
        outputField.setLedType(LedType.SQUARE);
        outputField.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        
        VBox root = new VBox();
        root.getChildren().add(outputField);

        ctrlPane.getChildren().add(root);

        Scene scene = new Scene(ctrlPane, 200, 200);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();

        ctrlPane.connect();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }    
}
