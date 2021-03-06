/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.osgi;

import org.opennms.features.topology.api.osgi.locator.OnmsServiceManagerLocator;
import org.osgi.framework.BundleContext;

public class VaadinApplicationContextImpl implements VaadinApplicationContext {
    private String sessionId;
    private String userName;
    private int uiId;

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    @Override
    public EventProxy getEventProxy(OnmsServiceManager serviceManager) {
        if (serviceManager == null) throw new IllegalArgumentException("OnmsServiceManager must not be null");
        EventRegistry eventRegistry = serviceManager.getEventRegistry();
        if (eventRegistry == null) throw new IllegalArgumentException("EventRegistry must not be null");
        return eventRegistry.getScope(this);
    }

    @Override
    public EventProxy getEventProxy(BundleContext bundleContext) {
        if (bundleContext == null) throw new IllegalArgumentException("BundleContext must not be null");
        return getEventProxy(new OnmsServiceManagerLocator().lookup(bundleContext));
    }

    public void setUiId(int uiId) {
        this.uiId = uiId;
    }

    @Override
    public int getUiId() {
        return uiId;
    }
}
