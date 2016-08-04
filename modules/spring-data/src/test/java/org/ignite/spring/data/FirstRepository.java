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

package org.ignite.spring.data;

import java.util.List;
import org.springframework.data.ignite.repository.IgniteRepository;
import org.springframework.data.ignite.repository.config.Query;
import org.springframework.data.ignite.repository.config.RepositoryConfig;

@RepositoryConfig(cacheName = "cache")
public interface FirstRepository extends IgniteRepository<Person, Integer> {

    public List<Person> findByFirstName(String val);

    public List<Person> findByFirstNameContaining(String val);

    public List<Person> findTopByFirstNameContaining(String val);

    public List<Person> findFirst10ByFirstNameLike(String val);

    @Query("firstName = ?")
    public List<Person> byVal(String val);
}