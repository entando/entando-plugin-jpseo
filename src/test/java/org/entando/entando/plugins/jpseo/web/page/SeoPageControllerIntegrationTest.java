/*
 * Copyright 2018-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.plugins.jpseo.web.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agiletec.aps.system.common.notify.NotifyManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.aps.system.services.page.Page;
import com.agiletec.aps.system.services.page.PageMetadata;
import com.agiletec.aps.system.services.page.PageTestUtil;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.system.services.pagemodel.PageModel;
import com.agiletec.aps.system.services.role.Permission;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.aps.util.ApsProperties;
import com.agiletec.aps.util.FileTextReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.entando.entando.aps.system.services.page.IPageService;
import org.entando.entando.aps.system.services.page.model.PageDto;
import org.entando.entando.plugins.jpseo.aps.system.services.mapping.FriendlyCodeVO;
import org.entando.entando.plugins.jpseo.aps.system.services.mapping.ISeoMappingManager;
import org.entando.entando.plugins.jpseo.aps.system.services.mapping.SeoMappingManager;
import org.entando.entando.web.AbstractControllerIntegrationTest;
import org.entando.entando.web.page.model.PageRequest;
import org.entando.entando.web.utils.OAuth2TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

class SeoPageControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    @Qualifier("SeoPageService")
    private IPageService pageService;
    
    @Autowired
    private IPageManager pageManager;
    
    @Autowired
    private SeoMappingManager seoMappingManager;

    private ObjectMapper mapper = new ObjectMapper();

    private static String SEO_TEST_1 = "seoTest1";
    private static String SEO_TEST_2 = "seoTest2";
    private static String SEO_TEST_3 = "seoTest3";
    private static String SEO_TEST_4 = "seoTest4";
    private static String SEO_TEST_2_FC = "seoTest2fc";


    @Test
    void testGetBuiltInSeoPage() throws Exception {
        String accessToken = this.createAccessToken();

        final ResultActions result = this.executeGetSeoPage("service", accessToken);
        result.andExpect(status().isOk());
    }
    
    @Test
    void testPostSeoPage() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            final ResultActions result = this.executePostSeoPage("1_POST_valid.json", accessToken, status().isOk());
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT));
            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_1)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("service")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("service")))
                    .andExpect(jsonPath("$.payload.seo", is(true)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.description", is("test")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.keywords", is("keyword1, keyword 2")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is("test_page_1_en")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags.size()", is(3)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].key", is("copyright")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].value", is("2020")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].value", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].useDefaultLang", is(false)))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.inheritDescriptionFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritKeywordsFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritFriendlyCodeFromDefaultLang",
                            is(true)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.description", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.keywords", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_1_it")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags.size()", is(3)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].key", is("copyright")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].value", is("metatag di prova")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.inheritFriendlyCodeFromDefaultLang",
                            is(false)));

            // result.andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.inheritDescriptionFromDefaultLang",is(false)));
            // result.andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.inheritKeywordsFromDefaultLang",is(false)));
        } finally {
            PageDto page = this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT);
            if (null != page) {
                this.pageService.removePage(SEO_TEST_1);
            }
        }
    }

    @Test
    void testPostSeoPageNoSeoFields() throws Exception {
        try {
            String accessToken = this.createAccessToken();

            final ResultActions result = this
                    .executePostSeoPage("1_POST_valid_empty_fields_1.json", accessToken, status().isOk());

            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT));
            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_1)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("service")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("service")))
                    .andExpect(jsonPath("$.payload.seo", is(false)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)));

        } finally {
            PageDto page = this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT);
            if (null != page) {
                this.pageService.removePage(SEO_TEST_1);
            }
        }
    }

    @Test
    void testPostSeoPageNullSeoFields() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            final ResultActions result = this
                    .executePostSeoPage("1_POST_valid_empty_fields_2.json", accessToken, status().isOk());
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT));
            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_1)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("service")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("service")))
                    .andExpect(jsonPath("$.payload.seo", is(false)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)));
        } finally {
            PageDto page = this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT);
            if (null != page) {
                this.pageService.removePage(SEO_TEST_1);
            }
        }
    }

    @Test
    void testPostSeoPageNullSeoData() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            final ResultActions result = this.executePostSeoPage("1_POST_valid_no_seoData.json", accessToken, status().isOk());
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_1, IPageService.STATUS_DRAFT));
            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_1)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("service")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("service")))
                    .andExpect(jsonPath("$.payload.seo", is(false)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)));
        } finally {
            this.pageManager.deletePage(SEO_TEST_1);
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_1);
        }
    }

    @Test
    void testPostSeoPageNullPointer() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            Assertions.assertNull(this.pageManager.getDraftPage(SEO_TEST_4));
            final ResultActions result = this.executePostSeoPage("4_POST_valid.json", accessToken, status().isOk());
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_4, IPageService.STATUS_DRAFT));
            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_4)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("home")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("homepage")))
                    .andExpect(jsonPath("$.payload.seo", is(false)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(1)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(1)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)));
        } finally {
            this.pageManager.deletePage(SEO_TEST_4);
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_4);
        }
    }
    
    @Test
    void testPostSeoPageDuplicateFriendlyCode() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            ResultActions result1 = this.executePostSeoPage("2_POST_valid.json", accessToken, status().isOk());
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_2, IPageService.STATUS_DRAFT));
            result1.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is("test_page_2_en")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_2_it")));
            
            ResultActions result2 = this.executePostSeoPage("3_POST_invalid.json", accessToken, status().isConflict());
            Assertions.assertNull(this.pageManager.getDraftPage(SEO_TEST_3));
            result2.andExpect(jsonPath("$.errors.size()", is(1)));
        } finally {
            this.pageManager.deletePage(SEO_TEST_2);
            this.pageManager.deletePage(SEO_TEST_3);
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_2);
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_3);
        }
    }
    
    @Test
    void testPutSeoPage() throws Exception {
        try {
            String accessToken = this.createAccessToken();
            final ResultActions result = this
                    .executePostSeoPage("2_POST_valid.json", accessToken, status().isOk());
            this.waitNotifyingThread();

            result.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("service")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("service")))
                    .andExpect(jsonPath("$.payload.seo", is(true)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.description", is("test")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.keywords", is("keyword1, keyword 2")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is("test_page_2_en")))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.inheritDescriptionFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritKeywordsFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritFriendlyCodeFromDefaultLang", is(true)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags.size()", is(3)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].key", is("copyright")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].value", is("2020")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].value", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[2].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.description", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_2_it")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.keywords", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags.size()", is(3)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].key", is("copyright")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].value", is("metatag di prova")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[2].useDefaultLang", is(false)));

            assertNotNull(this.pageService.getPage(SEO_TEST_2, IPageService.STATUS_DRAFT));
            FriendlyCodeVO vo = this.seoMappingManager.getReference("test_page_2_it");
            assertNull(vo);
            String reference = this.seoMappingManager.getDraftPageReference("test_page_2_it");
            assertEquals(SEO_TEST_2, reference);

            final ResultActions resultPut = this
                    .executePutSeoPage("2_PUT_valid.json", accessToken, status().isOk());
            this.waitNotifyingThread();

            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_2, IPageService.STATUS_DRAFT));
            resultPut.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("home")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("homepage")))
                    .andExpect(jsonPath("$.payload.seo", is(true)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.description", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is("test_page_2_friendly_url_en")))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.keywords",
                                    is("keyword number 1, keyword number 2")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].value", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.inheritDescriptionFromDefaultLang", is(false)))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.inheritKeywordsFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritFriendlyCodeFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.description", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.keywords", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_2_friendly_url_it")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].type", is("name")))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].value",
                                    is("descrizione meta test")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].useDefaultLang", is(false)));
            
            assertNull(this.seoMappingManager.getReference("test_page_2_friendly_url_it"));
            assertNull(this.seoMappingManager.getDraftPageReference("test_page_2_it"));
            reference = this.seoMappingManager.getDraftPageReference("test_page_2_friendly_url_it");
            assertEquals(SEO_TEST_2, reference);

            final ResultActions resultPutMetaDefaultLangTrue = this
                    .executePutSeoPage("2_PUT_valid_meta_default_lang_true.json", accessToken, status().isOk());
            this.waitNotifyingThread();
            
            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_2, IPageService.STATUS_DRAFT));
            resultPutMetaDefaultLangTrue.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("home")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("homepage")))
                    .andExpect(jsonPath("$.payload.seo", is(true)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.description", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is(
                            "test_page_2_friendly_url_en")))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.keywords",
                                    is("keyword number 1, keyword number 2")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[0].useDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].value", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags[1].useDefaultLang", is(false)))
                    .andExpect(
                            jsonPath("$.payload.seoData.seoDataByLang.en.inheritDescriptionFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritKeywordsFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritFriendlyCodeFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.description", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_2_friendly_url_it")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.keywords", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].key", is("author")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].value", is("entando")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[0].useDefaultLang", is(true)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].key", is("description")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].type", is("name")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].value", is("test in italiano")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags[1].useDefaultLang", is(true)));
            
            final ResultActions resultNoMetatag = this
                    .executePutSeoPage("2_PUT_valid_no_metatag.json", accessToken, status().isOk());
            this.waitNotifyingThread();

            Assertions.assertNotNull(this.pageService.getPage(SEO_TEST_2, IPageService.STATUS_DRAFT));
            resultNoMetatag.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2)))
                    .andExpect(jsonPath("$.payload.status", is("unpublished")))
                    .andExpect(jsonPath("$.payload.onlineInstance", is(false)))
                    .andExpect(jsonPath("$.payload.displayedInMenu", is(true)))
                    .andExpect(jsonPath("$.payload.pageModel", is("home")))
                    .andExpect(jsonPath("$.payload.charset", is("utf-8")))
                    .andExpect(jsonPath("$.payload.contentType", is("text/html")))
                    .andExpect(jsonPath("$.payload.parentCode", is("homepage")))
                    .andExpect(jsonPath("$.payload.seo", is(true)))
                    .andExpect(jsonPath("$.payload.titles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.fullTitles.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.size()", is(2)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.description", is("test page")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.friendlyCode", is("test_page_2_friendly_url_en")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.keywords",
                            is("keyword number 1, keyword number 2")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.metaTags.size()", is(0)))

                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritDescriptionFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritKeywordsFromDefaultLang", is(false)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.en.inheritFriendlyCodeFromDefaultLang", is(true)))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.description", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.keywords", is("")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.friendlyCode", is("test_page_2_friendly_url_it")))
                    .andExpect(jsonPath("$.payload.seoData.seoDataByLang.it.metaTags.size()", is(0)));
            
            final ResultActions resultCheckFriendlyCode1 = this.executePostSeoPage("2_POST_valid_friendly_code.json", accessToken, status().isOk());
            resultCheckFriendlyCode1.andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.code", is(SEO_TEST_2_FC)));
            this.waitNotifyingThread();
            
            assertNull(this.seoMappingManager.getReference("test_page_2_fc_it"));
            reference = this.seoMappingManager.getDraftPageReference("test_page_2_fc_it");
            assertEquals(SEO_TEST_2_FC, reference);
            assertNotNull(this.pageService.getPage(SEO_TEST_2_FC, IPageService.STATUS_DRAFT));

            this.executePutSeoPage("2_PUT_invalid_friendly_code.json", accessToken, status().isConflict());

            this.pageManager.setPageOnline(SEO_TEST_2_FC);
            this.waitNotifyingThread();
            this.executePutSeoPage("2_PUT_invalid_friendly_code.json", accessToken, status().isConflict());
        } finally {
            IPage page1 = this.pageManager.getDraftPage(SEO_TEST_2);
            IPage page2 = this.pageManager.getDraftPage(SEO_TEST_2_FC);
            if (null != page1) {
                this.pageManager.setPageOffline(SEO_TEST_2);
                this.pageManager.deletePage(SEO_TEST_2);
            }
            if (null != page2) {
                this.pageManager.setPageOffline(SEO_TEST_2_FC);
                this.pageManager.deletePage(SEO_TEST_2_FC);
            }
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_2);
            seoMappingManager.getSeoMappingDAO().deleteMappingForPage(SEO_TEST_2_FC);
        }
    }

    @Test
    void testCreatePageIntoDifferentOwnerGroupPages() throws Throwable {
        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24")
                .withAuthorization(Group.FREE_GROUP_NAME, "managePages", Permission.MANAGE_PAGES)
                .build();
        String accessToken = mockOAuthInterceptor(user);
        try {

            pageManager.addPage(createPage("page_root",  null, Group.FREE_GROUP_NAME));
            pageManager.addPage(createPage("free_pg",  "page_root", Group.FREE_GROUP_NAME));
            pageManager.addPage(createPage("admin_pg",  "page_root", Group.ADMINS_GROUP_NAME));
            pageManager.addPage(createPage("group1_pg",  "page_root", "coach"));
            pageManager.addPage(createPage("group2_pg",  "page_root", "customers"));

            String pageCode = "free_pg_into_admin_pg";

            mockMvc.perform(post("/plugins/seo/pages", pageCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(
                            createPageRequest(
                                    pageCode,
                                    Group.FREE_GROUP_NAME,
                                    "admin_pg"))))
                    .andDo(resultPrint())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.payload.size()", CoreMatchers.is(0)))
                    .andExpect(jsonPath("$.errors.size()", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.errors[0].code", CoreMatchers.is("2")))
                    .andExpect(jsonPath("$.errors[0].message", CoreMatchers.is("Cannot move a free page under a reserved page")));

            pageCode = "admin_pg_into_group1_pg";

            mockMvc.perform(post("/plugins/seo/pages/", pageCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(
                            createPageRequest(
                                    pageCode,
                                    Group.ADMINS_GROUP_NAME,
                                    "group1_pg"))))
                    .andDo(resultPrint())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.payload.size()", CoreMatchers.is(0)))
                    .andExpect(jsonPath("$.errors.size()", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.errors[0].code", CoreMatchers.is("2")))
                    .andExpect(jsonPath("$.errors[0].message", CoreMatchers.is("Can not move a page under a page owned by a different group")));

            pageCode = "group1_pg_into_admin_pg";

            mockMvc.perform(post("/plugins/seo/pages/", pageCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(
                            createPageRequest(
                                    pageCode,
                                    "coach",
                                    "admin_pg"))))
                    .andDo(resultPrint())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.payload.size()", CoreMatchers.is(0)))
                    .andExpect(jsonPath("$.errors.size()", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.errors[0].code", CoreMatchers.is("2")))
                    .andExpect(jsonPath("$.errors[0].message", CoreMatchers.is("Can not move a page under a page owned by a different group")));

            pageCode = "group1_pg_into_group2_pg";

            mockMvc.perform(post("/plugins/seo/pages/", pageCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(
                            createPageRequest(
                                    pageCode,
                                    "coach",
                                    "group2_pg"))))
                    .andDo(resultPrint())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.payload.size()", CoreMatchers.is(0)))
                    .andExpect(jsonPath("$.errors.size()", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.errors[0].code", CoreMatchers.is("2")))
                    .andExpect(jsonPath("$.errors[0].message", CoreMatchers.is("Can not move a page under a page owned by a different group")));

            pageCode = "group2_pg_into_group1_pg";

            mockMvc.perform(post("/plugins/seo/pages/", pageCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(
                            createPageRequest(
                                    pageCode,
                                    "customers",
                                    "group1_pg"))))
                    .andDo(resultPrint())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.payload.size()", CoreMatchers.is(0)))
                    .andExpect(jsonPath("$.errors.size()", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.errors[0].code", CoreMatchers.is("2")))
                    .andExpect(jsonPath("$.errors[0].message", CoreMatchers.is("Can not move a page under a page owned by a different group")));

        } finally {
            this.pageManager.deletePage("group2_pg_into_group1_pg");
            this.pageManager.deletePage("group1_pg_into_group2_pg");
            this.pageManager.deletePage("group1_pg_into_admin_pg");
            this.pageManager.deletePage("admin_pg_into_group1_pg");
            this.pageManager.deletePage("free_pg_into_admin_pg");
            this.pageManager.deletePage("group2_pg");
            this.pageManager.deletePage("group1_pg");
            this.pageManager.deletePage("admin_pg");
            this.pageManager.deletePage("free_pg");
            this.pageManager.deletePage("page_root");
        }
    }

    private PageRequest createPageRequest(String pageCode, String groupCode, String parentCode) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCode(pageCode);
        pageRequest.setPageModel("home");
        pageRequest.setOwnerGroup(groupCode);
        Map<String, String> titles = new HashMap<>();
        titles.put("it", pageCode);
        titles.put("en", pageCode);
        pageRequest.setTitles(titles);
        pageRequest.setParentCode(parentCode);
        return pageRequest;
    }

    protected Page createPage(String pageCode, String parent, String group) {
        if (null == parent) {
            parent = "service";
        }
        IPage parentPage = pageManager.getDraftPage(parent);
        PageModel pageModel = parentPage.getMetadata().getModel();
        PageMetadata metadata = PageTestUtil
                .createPageMetadata(pageModel, true, pageCode + "_title", null, null, false, null, null);
        ApsProperties config = new ApsProperties();
        config.put("actionPath", "/mypage.jsp");
        Page pageToAdd = PageTestUtil.createPage(pageCode, parentPage.getCode(), group, metadata, null);
        return pageToAdd;
    }
    
    protected void waitNotifyingThread() throws InterruptedException {
        synchronized (this) {
            this.wait(500);
        }
        String threadNamePrefix = NotifyManager.NOTIFYING_THREAD_NAME;
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);
        for (int i = 0; i < threads.length; i++) {
            Thread currentThread = threads[i];
            if (currentThread != null
                    && currentThread.getName().startsWith(threadNamePrefix)) {
                currentThread.join();
            }
        }
    }

    private ResultActions executePostSeoPage(String fileName, String accessToken, ResultMatcher expected)
            throws Exception {
        InputStream isJsonPostValid2 = this.getClass().getResourceAsStream(fileName);
        String jsonPostValid2 = FileTextReader.getText(isJsonPostValid2);
        String path = "/plugins/seo/pages/";
        ResultActions result = mockMvc
                .perform(post(path)
                        .content(jsonPostValid2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(expected);
        return result;
    }

    private ResultActions executePutSeoPage(String fileName, String accessToken, ResultMatcher expected)
            throws Exception {
        InputStream isJsonPutValid = this.getClass().getResourceAsStream(fileName);
        String jsonPutValid = FileTextReader.getText(isJsonPutValid);
        String path = "/plugins/seo/pages/{pageCode}";
        ResultActions result1 = mockMvc
                .perform(put(path, SEO_TEST_2)
                        .content(jsonPutValid)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result1.andExpect(expected);
        return result1;
    }

    private ResultActions executeGetSeoPage(String pageCode, String accessToken)
            throws Exception {
        String path = "/plugins/seo/pages/{pageCode}";
        ResultActions result = mockMvc
                .perform(get(path, pageCode)
                        .header("Authorization", "Bearer " + accessToken));
        return result;
    }

    private String createAccessToken() throws Exception {
        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24")
                .withAuthorization(Group.FREE_GROUP_NAME, Permission.MANAGE_PAGES, Permission.MANAGE_PAGES)
                .build();
        return mockOAuthInterceptor(user);
    }

}
