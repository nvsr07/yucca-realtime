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

package org.yucca.realtime.adaptor.output.mongodb.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.yucca.realtime.adaptor.output.mongodb.MongoDBOutEventAdaptorFactory;


/**
 * @scr.component name="output.mongodbOutEventAdaptorService.component" immediate="true"
 */


public class MongoDBOutEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(MongoDBOutEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorFactory testOutEventAdaptorFactory = new MongoDBOutEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(), testOutEventAdaptorFactory, null);
            log.info("Successfully deployed the Mongo output event adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the Mongo output event adaptor service ", e);
        }
    }

}
