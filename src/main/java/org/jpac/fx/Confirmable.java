/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Confirmable.java
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

/**
 *
 * @author berndschuster
 */
public interface Confirmable {
    /**
     * used to confirm or decline the alteration of a Confirmable
     * @param confirm : true: confirm alteration, false: decline it
     */
    public void confirm(boolean confirm);
    
    /**
     * sets a Confirmable as to be confirmed 
     * @param toBeConfirmed 
     */
    public void setToBeConfirmed(boolean toBeConfirmed);
    
    /**
     * used to check, if a Confirmable is to be confirmed
     * @return 
     */
    public boolean isToBeConfirmed();
    
    /**
     * used to check, if the Confirmable is able to be confirmed. Might return false
     * in cases, where the user input is invalid
     * @return true: Confirmable can be confirmed, false: Confirmable does provide valid input data right now
     */
    public boolean isConfirmable();
    
    public ProcessEvent confirmed();
}
