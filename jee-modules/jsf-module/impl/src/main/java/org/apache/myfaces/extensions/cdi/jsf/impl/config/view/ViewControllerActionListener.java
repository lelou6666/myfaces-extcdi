/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.cdi.jsf.impl.config.view;

import org.apache.myfaces.extensions.cdi.core.api.Deactivatable;
import org.apache.myfaces.extensions.cdi.core.impl.util.ClassDeactivation;

import javax.faces.event.ActionListener;
import javax.faces.event.ActionEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.context.FacesContext;

/**
 * @author Gerhard Petracek
 */
public class ViewControllerActionListener implements ActionListener, Deactivatable
{
    private final ActionListener wrapped;

    private final boolean deactivated;

    public ViewControllerActionListener(ActionListener wrapped)
    {
        this.wrapped = wrapped;
        this.deactivated = !isActivated();
    }

    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        if(this.deactivated)
        {
            return;
        }
        
        ViewConfigEntry viewConfigEntry =
                ViewConfigCache.getViewDefinition(FacesContext.getCurrentInstance().getViewRoot().getViewId());

        if(viewConfigEntry != null)
        {
            viewConfigEntry.invokePrePageActionMethods();
        }

        this.wrapped.processAction(actionEvent);
    }

    public boolean isActivated()
    {
        return ClassDeactivation.isClassActivated(getClass());
    }
}
