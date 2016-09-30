// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.druid.model.query;

import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.druid.model.aggregation.Aggregation;
import com.yahoo.bard.webservice.druid.model.datasource.DataSource;
import com.yahoo.bard.webservice.druid.model.postaggregation.PostAggregation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common interface for Druid Query classes.
 *
 * @param <Q> Type of DruidAggregationQuery this one extends. This allows the queries to nest their own type.
 */
public interface DruidAggregationQuery<Q extends DruidAggregationQuery<? super Q>> extends DruidFactQuery<Q> {

    /**
     * Retrieves a set of druid metric names associated with the query.
     *
     * @return set of druid metric names
     */
    @JsonIgnore
    default Set<String> getDependentFieldNames() {
        return getInnermostQuery().getAggregations().stream()
                .map(Aggregation::getFieldName)
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Get all the dimensions from Filtered Aggregations of a filtered metric.
     *
     * @return Set of dimensions which are part of Filtered Aggregations in a filtered logical metric
     */
    @JsonIgnore
    default Set<Dimension> getMetricDimensions() {
        return getInnermostQuery().getAggregations().stream()
                .map(Aggregation::getDependentDimensions)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the dimensions of the query if any.
     *
     * @return the query dimensions
     */
    Collection<Dimension> getDimensions();

    /**
     * Returns the aggregations of the query.
     *
     * @return the query aggregations
     */
    Set<Aggregation> getAggregations();

    /**
     * Returns the post-aggregations of the query.
     *
     * @return the query post-aggregations
     */
    Collection<PostAggregation> getPostAggregations();

    /**
     * Returns a copy of this query with the specified aggregations.
     *
     * @param aggregations  the new aggregations
     *
     * @return the query copy
     */
    Q withAggregations(Collection<Aggregation> aggregations);

    /**
     * Returns a copy of this query with the specified post aggregations.
     *
     * @param postAggregations  the new post aggregations
     *
     * @return the query copy
     */
    Q withPostAggregations(Collection<PostAggregation> postAggregations);

    /**
     * Provides a rough, relative measure of how much work the backend is expected to perform to answer the query.
     * <p>
     * Higher weight values correspond to queries that are expected to take more time and resources for the backend
     * to process.
     *
     * @return A number providing a relative measure of how heavy the query is expected to be
     *
     * @throws ArithmeticException if the estimate is larger than {@link Long#MAX_VALUE}
     */
    long computeWeight();

    /**
     * Build a backend query that provides a more accurate weight for this query.
     * <p>
     * If the weight returned by `computeWeight` is sufficiently large, Fili sends a simple query to the backend that
     * computes a more accurate weight. It will use the query returned by this method to
     * compute that weight.
     *
     * @return A query that can be sent to the backend to compute a more accurate weight for the
     * query
     */
    DruidAggregationQuery<?> buildWeightEvaluationQuery();


    @Override
    @JsonIgnore
    default DruidAggregationQuery<?> getInnerQuery() {
        return (DruidAggregationQuery<?>) DruidFactQuery.super.getInnerQuery();
    }

    @Override
    @JsonIgnore
    default DruidAggregationQuery<?> getInnermostQuery() {
        return (DruidAggregationQuery<?>) DruidFactQuery.super.getInnermostQuery();
    }
}
