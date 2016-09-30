// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.druid.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Druid Queries the application knows about.
 */
public interface QueryType {

    /**
     * Get the JSON version of the query type.
     *
     * @return the json representation of this query type
     */
    @JsonValue
    String toJson();
}
