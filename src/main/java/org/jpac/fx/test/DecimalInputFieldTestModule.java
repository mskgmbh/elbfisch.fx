/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : TestModule.java
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

import org.jpac.AbstractModule;
import org.jpac.Decimal;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.ProcessException;

/**
 *
 * @author berndschuster
 */
public class DecimalInputFieldTestModule extends Module{
    protected Decimal analogSignal;
    
    public DecimalInputFieldTestModule(AbstractModule containingModule, String identifier){
        super(containingModule, identifier);
        this.analogSignal = new Decimal(this,"analogSignal");
    }

    @Override
    protected void work() throws ProcessException {
        try{
            Log.info("started");
            do{
                if (!analogSignal.isValid()){
                    analogSignal.becomesValid().await();
                    Log.info("analog signal became valid: " + analogSignal.get());
                }
                analogSignal.changes().await();
                Log.info("analog signal changed " + analogSignal.get());
            }
            while(true);
        }
        finally{
            Log.info("stopped");
        }
    }

    @Override
    protected void preCheckInterlocks() throws InputInterlockException {
        //nothing to do
    }

    @Override
    protected void postCheckInterlocks() throws OutputInterlockException {
        //nothing to do
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
