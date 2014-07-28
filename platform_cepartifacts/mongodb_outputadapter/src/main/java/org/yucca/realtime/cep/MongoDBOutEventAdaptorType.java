/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yucca.realtime.cep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.yucca.realtime.cep.internal.util.MongoDBOutEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public final class MongoDBOutEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(MongoDBOutEventAdaptorType.class);
    private ResourceBundle resourceBundle;

    @Override
    protected String getName() {
        return MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_TYPE_TESTOUT;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.yucca.realtime.cep.i18n.Resources", Locale.getDefault());

    }

    @Override
    protected List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        Property property1 = new Property(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD1);
        property1.setDisplayName(
                resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD1));
        property1.setRequired(true);
        property1.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD1_HINT));
        propertyList.add(property1);

        return propertyList;
    }

    @Override
    protected List<Property> getOutputMessageProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object o, OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
            int tenantId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
