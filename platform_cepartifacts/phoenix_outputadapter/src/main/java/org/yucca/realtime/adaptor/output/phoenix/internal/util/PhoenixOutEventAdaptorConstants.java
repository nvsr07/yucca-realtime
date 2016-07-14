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

package org.yucca.realtime.adaptor.output.phoenix.internal.util;


public final class PhoenixOutEventAdaptorConstants {

    private PhoenixOutEventAdaptorConstants() {
    }

    public static final String EVENT_ADAPTOR_TYPE_PHOENIX = "phoenix";
    public static final String EVENT_ADAPTOR_CONF_FIELD_MONGODBURL = "mongodb.url";
    public static final String EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT = "mongodb.port";

    public static final String EVENT_MESSAGE_CONF_FIELD_MONGODBUSERNAME = "mongodb.username";
    public static final String EVENT_MESSAGE_CONF_FIELD_MONGODBPASSWORD = "mongodb.password";

    
    public static final String EVENT_MESSAGE_CONF_FIELD_PHOENIXTABLE = "phoenix.table";
    
}
