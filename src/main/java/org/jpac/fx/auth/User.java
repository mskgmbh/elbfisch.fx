/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : User.java
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


package org.jpac.fx.auth;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author berndschuster
 */
@XmlRootElement(name = "user")
@XmlAccessorType (XmlAccessType.FIELD)
public class User {
    static Logger Log = LoggerFactory.getLogger("jpac.auth");    
    private String    id;
    private String    name;
    private Privilege privilege;
    
    public User(){
        this.id        = null;
        this.name      = null;
        this.privilege = Privilege.NONE;        
    }
    
    public User(String id, String name, Privilege privilege){
        this.id        = id;
        this.name      = name;
        this.privilege = privilege;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the privilege
     */
    public Privilege getPrivilege() {
        return privilege;
    }  
    
    @Override
    public String toString(){
        return "User(" + id + ", '" + name + "', " + privilege + ")";
    }
    
}
