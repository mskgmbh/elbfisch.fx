/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : UserRegistry.java
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

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import org.jpac.Observable;
import org.jpac.Observer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 *
 * @author berndschuster
 */
public class UserRegistry extends Observable{
    static Logger Log = LoggerFactory.getLogger("jpac.auth");    
    
    static private UserRegistry instance;
    
    private Users              users;
    private User               loggedInUser;
    
    private File               usersFile; 
    URL                        usersFileUrl;
    
    protected UserRegistry(){
        boolean usersLoaded = false;
        try{
            usersFileUrl = ClassLoader.getSystemResource("org.jpac.Users.xml");
            if (usersFileUrl != null){
                usersFile = new File(URLDecoder.decode(usersFileUrl.getFile(), "UTF-8"));
                usersLoaded = load();
            }
            else{
                //no users file found in class path. Set default
                usersFile = new File("./cfg/org.jpac.Users.xml");
            }   
        }
        catch(Exception exc){
            Log.error("Error while loading users list: ", exc);
        }
        if (!usersLoaded){
            //instantiate empty users list
            users        = new Users();
        }
    }
    
    /*
     * used to get the instance of the user registry (singleton)
     */
    static public UserRegistry getInstance(){
        if (instance == null){
            instance = new UserRegistry();
        }
        return instance;
    } 
    
    /**
     * used to get the User with the specified id
     * @param id
     * @return 
     */
    public User getUser(String id){
        return users.getList().get(id);
    }

    /**
     * used to add a user to the registry
     * @param user 
     */
    public void addUser(User user) throws UserAlreadyRegisteredException{
        if (users.getList().containsKey(user.getId())){
            throw new UserAlreadyRegisteredException(user);
        }
        users.getList().put(user.getId(), user);
        if (Log.isInfoEnabled()) Log.info("User added: " + user);
    }
    
    /**
     * used to login as the given user
     * @param user: the user to login
     * @return true: login was successfull, false: login failed
     */
    public boolean login(User user){
        boolean success = false;
        if (users.getList().containsValue(user)){
            if (Log.isInfoEnabled()) Log.info("User logged in: " + user);
            loggedInUser = user;
            setChanged();
            notifyObservers(loggedInUser.getPrivilege());
            success = true;
        }
        else{
            if (Log.isInfoEnabled()) Log.info("User failed to log in: " + user);
        }
        return success;
    }

    /**
     * used to login with a given id
     * @param id: the id of the user to login
     * @return true: login was successfull, false: login failed
     */
    public boolean login(String id){
        boolean sucess = false;
        loggedInUser = users.getList().get(id);
        if (loggedInUser != null){
            if (Log.isInfoEnabled()) Log.info("User logged in: " + loggedInUser);
            setChanged();
            notifyObservers(loggedInUser.getPrivilege());
            sucess = true;
        }
        else{
            if (Log.isInfoEnabled()) Log.info("User failed to log in. Id = " + id);            
        }
        return sucess;
    }
    
    /*
     * used to logoff
     */
    public void logoff(){
        if (Log.isInfoEnabled()) Log.info("User logged off: " + loggedInUser);
        loggedInUser = null;
        setChanged();
        notifyObservers(Privilege.NONE);
    }
    
    /**
     * used to get the user currently logged in
     * @return User, or null if logged off
     */
    public User getLoggedInUser(){
        return loggedInUser;
    }
    
    /**
     * returns the current privilege
     * @return 
     */
    public Privilege getCurrentPrivilege(){
        return loggedInUser == null ? Privilege.NONE : loggedInUser.getPrivilege();
    }
    
    @Override
    public void addObserver(Observer<?> observer){
        super.addObserver(observer);
        setChanged();
        notifyObservers(getCurrentPrivilege());
    }
    
    private boolean load(){
        boolean success = false;
        try{
            JAXBContext  context      = JAXBContext.newInstance(Users.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            users                     = (Users) unmarshaller.unmarshal(usersFile);
            success                   = true;
        }
        catch(Exception exc){
            Log.error("Failed to load org.jpac.Users.xml: ", exc);
        }
        return success;
    }

    public void store(){
        try{
            JAXBContext  context      = JAXBContext.newInstance(Users.class);
            Marshaller   marshaller   = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);                
            marshaller.marshal(users, usersFile);
            if (Log.isInfoEnabled()) Log.info("users list stored to " + usersFile.getAbsolutePath());
        }
        catch(Exception exc){
            Log.error("Failed to store users list to org.jpac.Users.xml: ", exc);
        }
    }
}
