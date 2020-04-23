/**
 * PROJECT   : <???>
 * MODULE    : <???>.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : <???>
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 * LOG       : $Log$
 */

package org.jpac.fx.test;

import org.jpac.Module;

/**
 *
 * @author berndschuster
 */
public class ModuleTester extends ModuleTesterTemplate{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Module module = new ClickButtonTestModule(null,"Main");
        module.start();
        launch(args);
    }

}
