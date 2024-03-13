/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Led.java
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

/**
 *
 * @author berndschuster
 */
public interface Connectable {
    /**
     * 
     * @return the fully qualified identifier of the signal the Connectable is connected to
     * @throws QualifiedIdentifierException 
     */
    public String getQualifiedIdentifier() throws QualifiedIdentifierException;
    /**
     * 
     * @return the identifier of the signal, the Connectable is connected to 
     */
    public String getIdentifier();
    /**
     * used to specify the identifier of the signal, the Connectable is to be connected to
     * Notation: the identifier might be preceded by a number of dots, to specify a relative identifier which
     *           is automatically completed by the identifiers of Connectables which directly or indirectly 
     *           contain this Connectable.
     *           Example: Assume an org.jpac.hmi.ControlPanel, whose signalIdentifier is "Robot.Base.TorqueController" which embeds
     *                    the Connectable, for instance a DecimalOutputField, whose signalIdentifer is ".mustValue" (note the preceding dot). 
     *                    This Connectable would be connected to the signal "Robot.Base.TorqueController.mustValue".
     *                    Another Connectable contained in the same jpac.hmi.ControlPanel, whose signalIdentifier is specified as "..AngleOfRotation" (not the 2 preceding dots)
     *                    would be connected to the signal "Robot.Base.AngleOfRotation".
     * @param identifier 
     */
    public void setIdentifier(String identifier);
    public void connect();
    public void disconnect();
    public boolean connected();
}
