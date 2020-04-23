/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Confirmed.java
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

import org.jpac.ProcessEvent;
import org.jpac.ProcessException;

/**
 *
 * @author berndschuster
 * ProcessEvent fired in case of an user action on a Confirmable is confirmed
 * get an instance by <Confirmable>.confirmed();
 */
public class Confirmed extends ProcessEvent{
    private boolean confirmed;
    
    @Override
    public void reset(){
        super.reset();
        //eventually pending confirmed state is reset on call of await()
        confirmed = false;
    }
    
    @Override
    public boolean fire() throws ProcessException {
        //catch transition
        boolean _fired = confirmed;
        confirmed = false;
        return _fired;
    }

    /**
     * @return the confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }   
}
