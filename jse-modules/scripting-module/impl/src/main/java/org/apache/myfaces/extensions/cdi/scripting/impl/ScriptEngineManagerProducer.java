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
package org.apache.myfaces.extensions.cdi.scripting.impl;

import org.apache.myfaces.extensions.cdi.scripting.api.ScriptLanguage;
import org.apache.myfaces.extensions.cdi.scripting.api.LanguageManager;
import org.apache.myfaces.extensions.cdi.scripting.api.ScriptExecutor;
import org.apache.myfaces.extensions.cdi.scripting.api.ScriptBuilder;
import org.apache.myfaces.extensions.cdi.scripting.api.language.Language;
import static org.apache.myfaces.extensions.cdi.scripting.api.ScriptingModuleBeanNames.*;
import static org.apache.myfaces.extensions.cdi.scripting.impl.util.ExceptionUtils.unknownScriptingLanguage;
import static org.apache.myfaces.extensions.cdi.scripting.impl.util.ExceptionUtils.overrideBuilderState;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.Produces;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Gerhard Petracek
 */
public class ScriptEngineManagerProducer
{
    protected ScriptEngineManagerProducer()
    {
    }

    //this syntax is neede for using it via the expression language
    @Produces
    @Dependent
    @Named(SCRIPT_EXECUTOR_ALIAS)
    public Map<String, Object> createScriptExecutorBeanForAlias()
    {
        return createScriptExecutorBean();
    }

    @Produces
    @Dependent
    @Named(SCRIPT_EXECUTOR)
    //TODO add support for args
    public Map<String, Object> createScriptExecutorBean()
    {
        return new HashMap<String, Object>() {
            private static final long serialVersionUID = 9030764968435532235L;

            @Override
            public Object get(Object key)
            {
                if(key == null)
                {
                    return null;
                }

                String language;

                if(key instanceof String)
                {
                    language = (String)key;
                }
                else
                {
                    language = key.toString();
                }

                language = expandLanguageName(language);

                return createScriptExecutorForLanguage(language);
            }
        };
    }

    private Map<String, Object> createScriptExecutorForLanguage(final String language)
    {
        return new HashMap<String, Object>() {
            private static final long serialVersionUID = 6583167904001037920L;

            @Override
            public Object get(Object key)
            {
                String script;

                if(key instanceof String)
                {
                    script = (String)key;
                }
                else
                {
                    script = key.toString();
                }

                return evalScript(language, script);
            }
        };
    }

    private interface PlaceHolderLanguage extends Language{}

    @Produces
    @ScriptLanguage(PlaceHolderLanguage.class)
    @Deprecated
    public ScriptExecutor createScriptExecutor(InjectionPoint injectionPoint, LanguageManager languageManager)
    {
        ScriptEngine scriptEngine = createScriptEngineByLanguageName(injectionPoint, languageManager);

        return createScriptExecutor(scriptEngine);
    }

    private ScriptExecutor createScriptExecutor(final ScriptEngine scriptEngine)
    {
        return new ScriptExecutor()
        {
            public Object eval(String script)
            {
                return eval(script, Object.class);
            }

            public Object eval(String script, Map<String, Object> arguments)
            {
                return eval(script, arguments, Object.class);
            }

            public Object eval(String script, Bindings bindings)
            {
                return eval(script, bindings, Object.class);
            }

            public <T> T eval(String script, Class<T> returnType)
            {
                try
                {
                    return (T)scriptEngine.eval(script);
                }
                catch (ScriptException e)
                {
                    throw new RuntimeException(e);
                }
            }

            public <T> T eval(String script, Map<String, Object> arguments, Class<T> returnType)
            {
                try
                {
                    Bindings bindings = new SimpleBindings(arguments);
                    return (T)scriptEngine.eval(script, bindings);
                }
                catch (ScriptException e)
                {
                    throw new RuntimeException(e);
                }
            }

            public <T> T eval(String script, Bindings bindings, Class<T> returnType)
            {
                try
                {
                    return (T)scriptEngine.eval(script, bindings);
                }
                catch (ScriptException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Produces
    @ScriptLanguage(PlaceHolderLanguage.class)
    @Deprecated
    public ScriptBuilder createScriptBuilder(InjectionPoint injectionPoint, LanguageManager languageManager)
    {
        ScriptEngine scriptEngine = createScriptEngineByLanguageName(injectionPoint, languageManager);

        return createScriptBuilder(scriptEngine);
    }

    private ScriptBuilder createScriptBuilder(final ScriptEngine scriptEngine)
    {
        return new ScriptBuilder()
        {
            private Map<String, Object> arguments;
            private String script;
            private Bindings bindings;

            public ScriptBuilder script(String script)
            {
                this.script = script;
                return this;
            }

            public ScriptBuilder namedArgument(String name, Object value)
            {
                if(this.bindings != null)
                {
                    throw overrideBuilderState("(named) argument/s");
                }

                if(this.arguments == null)
                {
                    this.arguments = new HashMap<String, Object>();
                }
                this.arguments.put(name, value);
                return this;
            }

            public ScriptBuilder bindings(Bindings bindings)
            {
                if(this.arguments != null)
                {
                    throw overrideBuilderState("bindings");
                }

                this.bindings = bindings;
                return this;
            }

            public Object eval()
            {
                return eval(Object.class);
            }

            public <T> T eval(Class<T> returnType)
            {
                try
                {
                    if(this.bindings == null && this.arguments == null)
                    {
                        return (T)scriptEngine.eval(this.script);
                    }

                    Bindings scriptBindings = this.bindings;

                    if(scriptBindings == null)
                    {
                        scriptBindings = new SimpleBindings(this.arguments);
                    }

                    return (T)scriptEngine.eval(this.script, scriptBindings);
                }
                catch (ScriptException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Produces
    @ScriptLanguage(PlaceHolderLanguage.class)
    public ScriptEngine createScriptEngineByLanguageName(InjectionPoint injectionPoint, LanguageManager languageManager)
    {
        Class<? extends Language> language =
                injectionPoint.getAnnotated().getAnnotation(ScriptLanguage.class).value();

        String languageName = languageManager.getLanguageName(language);

        return checkedScriptEngine(getScriptEngineManager().getEngineByName(languageName), languageName);
    }

    /*
    @Produces
    @ScriptExtension("")
    public ScriptEngine createScriptEngineByExtension(InjectionPoint injectionPoint,
                                                      ScriptEngineManager scriptEngineManager)
    {
        String extension = injectionPoint.getAnnotated().getAnnotation(ScriptExtension.class).value();

        return checkedScriptEngine(scriptEngineManager.getEngineByExtension(extension), extension);
    }

    @Produces
    @ScriptMimeType("")
    public ScriptEngine createScriptEngineByMimeType(InjectionPoint injectionPoint,
                                                     ScriptEngineManager scriptEngineManager)
    {
        String mimeType = injectionPoint.getAnnotated().getAnnotation(ScriptMimeType.class).value();

        return checkedScriptEngine(scriptEngineManager.getEngineByMimeType(mimeType), mimeType);
    }
    */

    private ScriptEngine checkedScriptEngine(ScriptEngine scriptEngine, String type)
    {
        if (scriptEngine != null)
        {
            return scriptEngine;
        }
        throw unknownScriptingLanguage(type);
    }

    private Object evalScript(String language, String script)
    {
        try
        {
            return getScriptEngineManager().getEngineByName(language).eval(script);
        }
        catch (ScriptException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String expandLanguageName(String language)
    {
        //TODO
        return language;
    }

    private ScriptEngineManager getScriptEngineManager()
    {
        return ScriptEngineManagerProvider.getScriptEngineManagerInstance();
    }
}