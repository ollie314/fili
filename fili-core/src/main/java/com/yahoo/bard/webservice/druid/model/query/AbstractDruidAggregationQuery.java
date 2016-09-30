// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.druid.model.query;

import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.druid.model.DefaultQueryType;
import com.yahoo.bard.webservice.druid.model.QueryType;
import com.yahoo.bard.webservice.druid.model.aggregation.Aggregation;
import com.yahoo.bard.webservice.druid.model.aggregation.LongSumAggregation;
import com.yahoo.bard.webservice.druid.model.aggregation.SketchAggregation;
import com.yahoo.bard.webservice.druid.model.datasource.DataSource;
import com.yahoo.bard.webservice.druid.model.filter.Filter;
import com.yahoo.bard.webservice.druid.model.orderby.LimitSpec;
import com.yahoo.bard.webservice.druid.model.postaggregation.PostAggregation;
import com.yahoo.bard.webservice.util.IntervalUtils;
import com.yahoo.bard.webservice.util.Utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for druid aggregation queries.
 *
 * @param <Q> Type of AbstractDruidAggregationQuery this one extends. This allows the queries to nest their own type.
 */
public abstract class AbstractDruidAggregationQuery<Q extends AbstractDruidAggregationQuery<? super Q>>
        extends AbstractDruidFactQuery<Q> implements DruidAggregationQuery<Q> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDruidAggregationQuery.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected final Collection<Dimension> dimensions;

    // At least one is needed
    protected final Collection<Aggregation> aggregations;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected final Collection<PostAggregation> postAggregations;

    /**
     * Constructor.
     *
     * @param queryType  The type of this query
     * @param dataSource  The datasource
     * @param granularity  The granularity
     * @param dimensions  The dimensions
     * @param filter  The filter
     * @param aggregations  The aggregations
     * @param postAggregations  The post-aggregations
     * @param intervals  The intervals
     * @param context  The context
     * @param doFork  true to fork a new context and bump up the query id, or false to create an exact copy of the
     * context.
     */
    protected AbstractDruidAggregationQuery(
            QueryType queryType,
            DataSource dataSource,
            Granularity granularity,
            Collection<Dimension> dimensions,
            Filter filter,
            Collection<Aggregation> aggregations,
            Collection<PostAggregation> postAggregations,
            Collection<Interval> intervals,
            QueryContext context,
            boolean doFork
    ) {
        super(queryType, dataSource, granularity, filter, intervals, context, doFork);
        this.dimensions = dimensions != null ? Collections.unmodifiableCollection(dimensions) : null;
        this.aggregations = aggregations != null ? new LinkedHashSet<>(aggregations) : null;
        this.postAggregations = postAggregations != null ? new LinkedHashSet<>(postAggregations) : null;
    }

    @Override
    public Collection<Dimension> getDimensions() {
        return dimensions;
    }

    @Override
    public Set<Aggregation> getAggregations() {
        return new LinkedHashSet<>(aggregations);
    }

    @Override
    public Collection<PostAggregation> getPostAggregations() {
        return new LinkedHashSet<>(postAggregations);
    }

    @Override
    public long computeWeight() {
        long sketchWeight = computeSketchWeight();
        if (sketchWeight == 0) {
            return 0;
        }

        long weight = Math.multiplyExact(
                computeCardinalityWeight(),
                Math.multiplyExact(sketchWeight, computePeriodWeight())
        );
        LOG.debug("worst case weight = {}", weight);

        return weight;
    }

    /**
     * Compute a weight based on the sketches in the query.
     * <p>
     * By default, the sketch weight is the number of sketch aggregations in the query.
     *
     * @return A weight based on the sketches in the query
     */
    protected long computeSketchWeight() {
        return Utils.getSubsetByType(getAggregations(), SketchAggregation.class).size();
    }

    /**
     * Computes a weight based on the period of the query.
     * <p>
     * By default, a query's period is the number of intervals in the query, after the intervals have been simplified
     * and partitioned by grain.
     *
     * @return A weight based on the period of the query
     */
    protected long computePeriodWeight() {
        return IntervalUtils.countSlicedIntervals(getIntervals(), getGranularity());
    }

    /**
     * Computes a weight based on the cardinality of the dimensions of a query.
     * <p>
     * By default, the weight is computed by multiplying together the cardinality of every non-empty dimension.
     *
     * @return A weight based on the cardinality of a dimension
     */
    protected long computeCardinalityWeight() {
        return getDimensions().stream()
                .mapToLong(Dimension::getCardinality)
                .filter(cardinality -> cardinality > 0)
                .reduce(1, Math::multiplyExact);
    }
}
