/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.finra.herd.dao.IndexSearchDao;
import org.finra.herd.model.api.xml.IndexSearchFilter;
import org.finra.herd.model.api.xml.IndexSearchRequest;
import org.finra.herd.model.api.xml.IndexSearchResponse;
import org.finra.herd.model.api.xml.IndexSearchResultTypeKey;
import org.finra.herd.model.api.xml.TagKey;
import org.finra.herd.service.FacetFieldValidationService;
import org.finra.herd.service.IndexSearchService;
import org.finra.herd.service.SearchableService;

/**
 * IndexSearchServiceImpl is the implementation of the IndexSearchService and includes an indexSearch method that will handle search requests against a search
 * index.
 */
@Service
public class IndexSearchServiceImpl implements IndexSearchService, SearchableService, FacetFieldValidationService
{
    /**
     * Constant to hold the display name option for the indexSearch
     */
    private static final String DISPLAY_NAME_FIELD = "displayname";

    /**
     * The minimum allowable length of a search term
     */
    private static final int SEARCH_TERM_MINIMUM_ALLOWABLE_LENGTH = 3;

    /**
     * Constant to hold the short description option for the indexSearch
     */
    private static final String SHORT_DESCRIPTION_FIELD = "shortdescription";

    @Autowired
    private IndexSearchDao indexSearchDao;

    private static final String TAG_FACET_FIELD = "tag";

    private static final String RESULT_TYPE_FACET_FIELD = "resultType";

    @Override
    public IndexSearchResponse indexSearch(final IndexSearchRequest request, final Set<String> fields)
    {
        // Validate the search response fields
        validateSearchResponseFields(fields);

        // Validate the search term
        validateIndexSearchRequestSearchTerm(request.getSearchTerm());

        // Validate the index search filters
        validateIndexSearchFilters(request.getIndexSearchFilters());

        Set<String> facetFields = new HashSet<>();
        if (CollectionUtils.isNotEmpty(request.getFacetFields()))
        {
            facetFields.addAll(validateFacetFields(new HashSet<>(request.getFacetFields())));
        }

        //set the facets fields after validation
        request.setFacetFields(new ArrayList<>(facetFields));
        return indexSearchDao.indexSearch(request, fields);
    }

    /**
     * Private method to validate the index search request search term.
     *
     * @param indexSearchTerm the index search term string
     */
    private void validateIndexSearchRequestSearchTerm(final String indexSearchTerm)
    {
        // A search term must be provided
        Assert.notNull(indexSearchTerm, "A search term must be specified.");

        // The following characters will be react like spaces during the search: '-' and '_'
        // Confirm that the search term is long enough
        Assert.isTrue(indexSearchTerm.replace('-', ' ').replace('_', ' ').trim().length() >= SEARCH_TERM_MINIMUM_ALLOWABLE_LENGTH,
            "The search term length must be at least " + SEARCH_TERM_MINIMUM_ALLOWABLE_LENGTH + " characters.");
    }

    /**
     * Validates the specified index search filters.
     *
     * @param indexSearchFilters the index search filters
     */
    private void validateIndexSearchFilters(List<IndexSearchFilter> indexSearchFilters)
    {
        // Validate that the search filters list is not empty
        Assert.notEmpty(indexSearchFilters, "At least one index search filter must be specified.");

        for (IndexSearchFilter searchFilter : indexSearchFilters)
        {
            // Silently skip a search filter which is null
            if (null != searchFilter)
            {
                // Validate that each search filter has at least one index search key
                Assert.notEmpty(searchFilter.getIndexSearchKeys(), "At least one index search key must be specified.");

                // Guard against a single null element in the index search keys list
                if (null != searchFilter.getIndexSearchKeys().get(0))
                {
                    // Get the instance type of the key in the search filter, match all other keys with this
                    Class<?> expectedInstanceType =
                        searchFilter.getIndexSearchKeys().get(0).getIndexSearchResultTypeKey() != null ? IndexSearchResultTypeKey.class : TagKey.class;

                    searchFilter.getIndexSearchKeys().forEach(indexSearchKey ->
                    {
                        // Validate that each search key has either an index search result type key or a tag key
                        Assert.isTrue((null != indexSearchKey.getIndexSearchResultTypeKey()) ^ (null != indexSearchKey.getTagKey()),
                            "Exactly one instance of index search result type key or tag key must be specified.");

                        Class<?> actualInstanceType = indexSearchKey.getIndexSearchResultTypeKey() != null ? IndexSearchResultTypeKey.class : TagKey.class;

                        // Validate that search keys within the same filter have either index search result type keys or tag keys
                        Assert.isTrue(expectedInstanceType.equals(actualInstanceType),
                            "Index search keys should be a homogeneous list of either index search result type keys or tag keys");
                    });
                }
            }
        }
    }


    @Override
    public Set<String> getValidSearchResponseFields()
    {
        return ImmutableSet.of(SHORT_DESCRIPTION_FIELD, DISPLAY_NAME_FIELD);
    }

    @Override
    public Set<String> getValidFacetFields()
    {
        return ImmutableSet.of(TAG_FACET_FIELD, RESULT_TYPE_FACET_FIELD);
    }
}
