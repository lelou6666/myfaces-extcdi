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
package org.apache.myfaces.extensions.cdi.jsf.impl.navigation;

import static org.apache.myfaces.extensions.cdi.core.api.util.ClassUtils.tryToLoadClassForName;
import org.apache.myfaces.extensions.cdi.core.api.config.view.ViewConfig;
import org.apache.myfaces.extensions.cdi.core.api.security.AccessDeniedException;
import org.apache.myfaces.extensions.cdi.core.api.provider.BeanManagerProvider;
import static org.apache.myfaces.extensions.cdi.core.impl.utils.SecurityUtils.invokeVoters;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.NavigationMode;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PreViewConfigNavigateEvent;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.ViewConfigCache;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.ViewConfigEntry;
import static org.apache.myfaces.extensions.cdi.jsf.impl.util.SecurityUtils.tryToHandleSecurityViolation;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.enterprise.inject.spi.BeanManager;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Gerhard Petracek
 */
public class ViewConfigAwareNavigationHandler extends NavigationHandler
{
    private Set<String> otherOutcomes = new CopyOnWriteArraySet<String>();
    private Map<String, ViewConfigEntry> viewConfigs = new ConcurrentHashMap<String, ViewConfigEntry>();

    private NavigationHandler navigationHandler;

    private BeanManager beanManager;

    public ViewConfigAwareNavigationHandler(NavigationHandler navigationHandler)
    {
        this.navigationHandler = navigationHandler;
    }

    @Override
    public void handleNavigation(FacesContext facesContext, String fromAction, String outcome)
    {
        if(outcome != null && outcome.contains("."))
        {
            String originalOutcome = outcome;
            String oldViewId = facesContext.getViewRoot().getViewId();

            if(!this.otherOutcomes.contains(outcome))
            {
                if(outcome.startsWith("class "))
                {
                    outcome = outcome.substring(6);
                }
                ViewConfigEntry entry = this.viewConfigs.get(outcome);

                if(entry == null)
                {
                    Object loadedClass = tryToLoadClassForName(outcome);

                    if(loadedClass == null)
                    {
                        this.otherOutcomes.add(originalOutcome);
                    }
                    else if(loadedClass instanceof Class && ViewConfig.class.isAssignableFrom((Class)loadedClass))
                    {
                        //noinspection unchecked
                        entry = ViewConfigCache.getViewDefinition((Class<? extends ViewConfig>)loadedClass);
                    }
                }

                if(entry != null)
                {
                    //security
                    try
                    {
                        invokeVoters(null, entry.getAccessDecisionVoters(), entry.getErrorView());
                    }
                    catch (AccessDeniedException accessDeniedException)
                    {
                        tryToHandleSecurityViolation(accessDeniedException);
                        return;
                    }

                    this.viewConfigs.put(outcome, entry);

                    PreViewConfigNavigateEvent navigateEvent = firePreViewConfigNavigateEvent(oldViewId, entry);

                    entry = tryToUpdateEntry(entry, navigateEvent);

                    if(entry != null) //entry might be null after the update
                    {
                        processViewDefinitionEntry(facesContext, entry);

                        //just to invoke all other nav handlers if they have to perform special tasks...
                        this.navigationHandler.handleNavigation(facesContext, null, null);
                        return;
                    }
                }
            }
        }

        this.navigationHandler.handleNavigation(facesContext, fromAction, outcome);
    }

    private ViewConfigEntry tryToUpdateEntry(ViewConfigEntry viewConfigEntry, PreViewConfigNavigateEvent navigateEvent)
    {
        if(navigateEvent == null)
        {
            return viewConfigEntry;
        }

        if(navigateEvent.getToView() == null)
        {
            return null;
        }

        if(navigateEvent.getToView().equals(viewConfigEntry.getViewDefinitionClass()))
        {
            return viewConfigEntry;
        }

        return ViewConfigCache.getViewDefinition(navigateEvent.getToView());
    }

    private PreViewConfigNavigateEvent firePreViewConfigNavigateEvent(
            String oldViewId, ViewConfigEntry newViewConfigEntry)
    {
        ViewConfigEntry oldViewConfigEntry = ViewConfigCache.getViewDefinition(oldViewId);

        if(oldViewConfigEntry != null)
        {
            initBeanManager();
            PreViewConfigNavigateEvent navigateEvent = new PreViewConfigNavigateEvent(
                    oldViewConfigEntry.getViewDefinitionClass(), newViewConfigEntry.getViewDefinitionClass());

            this.beanManager.fireEvent(navigateEvent);
            return navigateEvent;
        }
        return null;
    }

    private void initBeanManager()
    {
        if(this.beanManager == null)
        {
            this.beanManager = BeanManagerProvider.getInstance().getBeanManager();
        }
    }

    private void processViewDefinitionEntry(FacesContext facesContext, ViewConfigEntry entry)
    {
        String targetViewId = entry.getViewId();

        NavigationMode currentNavigationMode = entry.getNavigationMode();

        if(NavigationMode.DEFAULT.equals(currentNavigationMode))
        {
            //TODO use value of the config
            currentNavigationMode = NavigationMode.FORWARD;
        }

        if(NavigationMode.REDIRECT.equals(currentNavigationMode))
        {
            ExternalContext externalContext = facesContext.getExternalContext();
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            String redirectPath = viewHandler.getActionURL(facesContext, targetViewId);

            try
            {
                externalContext.redirect(externalContext.encodeActionURL(redirectPath));
            }
            catch (IOException e)
            {
                throw new FacesException(e.getMessage(), e);
            }
        }
        else
        {
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            UIViewRoot viewRoot = viewHandler.createView(facesContext, targetViewId);
            facesContext.setViewRoot(viewRoot);
            facesContext.renderResponse();
        }
    }
}