package com.yahoo.bard.webservice.druid.model;

import com.yahoo.bard.webservice.util.EnumUtils;

/**
 * The Druid Query types provided by Fili out of the box.
 */
public enum DefaultQueryType implements QueryType {
    GROUP_BY,
    TOP_N,
    TIMESERIES,
    TIME_BOUNDARY,
    SEGMENT_METADATA,
    SEARCH,
    LOOKBACK;

    private final String jsonName;

    /**
     * Constructor.
     */
    DefaultQueryType() {
        this.jsonName = EnumUtils.enumJsonName(this);
    }

    /**
     * Get the JSON version of this.
     *
     * @return the json representation of this enum
     */
    @Override
    public String toJson() {
        return jsonName;
    }
}
