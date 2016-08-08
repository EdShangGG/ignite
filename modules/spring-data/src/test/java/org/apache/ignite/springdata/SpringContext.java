/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.springdata;

import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ignite.springdata.config.EnableIgniteRepositories;

@Configuration
@EnableIgniteRepositories
public class SpringContext {

    @Bean Service getService() {
        return new Service();
    }

    @Bean IgniteSpringBean igniteSpringBean() {
        IgniteSpringBean igniteSpringBean = new IgniteSpringBean();

        igniteSpringBean.setConfiguration(igniteConfiguration());

        return igniteSpringBean;
    }

    @Bean IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration configuration = new IgniteConfiguration();

        CacheConfiguration ccfg = new CacheConfiguration();
        ccfg.setName("cache");

        ccfg.setIndexedTypes(Integer.class, Person.class);

        configuration.setCacheConfiguration(ccfg);
        return configuration;
    }
}