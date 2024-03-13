package org.jpac.fx.auth;

import org.jpac.Observer;

public interface UserRegistryObserver extends Observer<UserRegistry>{
    public void update(UserRegistry userRegistry, Privilege privilege);
}
