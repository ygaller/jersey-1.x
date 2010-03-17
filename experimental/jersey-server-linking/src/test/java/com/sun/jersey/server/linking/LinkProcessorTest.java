/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.server.linking;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import junit.framework.TestCase;

/**
 *
 * @author mh124079
 */
public class LinkProcessorTest extends TestCase {
    
    UriInfo mockUriInfo;

    public LinkProcessorTest(String testName) {
        super(testName);
        mockUriInfo = new UriInfo() {

            private final static String baseURI = "http://example.com/application/resources";

            public String getPath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getPath(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<PathSegment> getPathSegments() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<PathSegment> getPathSegments(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getRequestUri() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public UriBuilder getRequestUriBuilder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getAbsolutePath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public UriBuilder getAbsolutePathBuilder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getBaseUri() {
                return getBaseUriBuilder().build();
            }

            public UriBuilder getBaseUriBuilder() {
                return UriBuilder.fromUri(baseURI);
            }

            public MultivaluedMap<String, String> getPathParameters() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getPathParameters(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getQueryParameters() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<String> getMatchedURIs() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<String> getMatchedURIs(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<Object> getMatchedResources() {
                Object dummyResource = new Object(){};
                return Collections.singletonList(dummyResource);
            }

        };
    }

    private final static String TEMPLATE_A = "foo";

    public static class TestClassD {
        @Link(value=TEMPLATE_A, style=Link.Style.RELATIVE_PATH)
        private String res1;

        @Link(value=TEMPLATE_A, style=Link.Style.RELATIVE_PATH)
        private URI res2;
    }

    public void testProcessLinks() {
        System.out.println("Links");
        LinkProcessor<TestClassD> instance = new LinkProcessor(TestClassD.class);
        TestClassD testClass = new TestClassD();
        instance.processLinks(testClass, mockUriInfo);
        assertEquals(TEMPLATE_A, testClass.res1);
        assertEquals(TEMPLATE_A, testClass.res2.toString());
    }

    private final static String TEMPLATE_B = "widgets/{id}";

    public static class TestClassE {
        @Link(value=TEMPLATE_B, style=Link.Style.RELATIVE_PATH)
        private String link;

        private String id;

        public TestClassE(String id) {
            this.id = id;
        }
    }

    public void testProcessLinksWithFields() {
        System.out.println("Links from field values");
        LinkProcessor<TestClassE> instance = new LinkProcessor(TestClassE.class);
        TestClassE testClass = new TestClassE("10");
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("widgets/10", testClass.link);
    }

    public static class TestClassF {
        @Link(value=TEMPLATE_B, style=Link.Style.RELATIVE_PATH)
        private String thelink;

        private String id;
        private TestClassE nested;

        public TestClassF(String id, TestClassE e) {
            this.id = id;
            this.nested = e;
        }
    }

    public void testNesting() {
        System.out.println("Nesting");
        LinkProcessor<TestClassF> instance = new LinkProcessor(TestClassF.class);
        TestClassE nested = new TestClassE("10");
        TestClassF testClass = new TestClassF("20", nested);
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("widgets/20", testClass.thelink);
        assertEquals("widgets/10", testClass.nested.link);
    }

    public void testArray() {
        System.out.println("Array");
        LinkProcessor<TestClassE[]> instance = new LinkProcessor(TestClassE[].class);
        TestClassE item1 = new TestClassE("10");
        TestClassE item2 = new TestClassE("20");
        TestClassE array[] = {item1, item2};
        instance.processLinks(array, mockUriInfo);
        assertEquals("widgets/10", array[0].link);
        assertEquals("widgets/20", array[1].link);
    }

    public void testCollection() {
        System.out.println("Collection");
        LinkProcessor<List> instance = new LinkProcessor(List.class);
        TestClassE item1 = new TestClassE("10");
        TestClassE item2 = new TestClassE("20");
        List<TestClassE> list = Arrays.asList(item1, item2);
        instance.processLinks(list, mockUriInfo);
        assertEquals("widgets/10", list.get(0).link);
        assertEquals("widgets/20", list.get(1).link);
    }

    public static class TestClassG {
        @Link(value=TEMPLATE_B, style=Link.Style.RELATIVE_PATH)
        private String relativePath;

        @Link(value=TEMPLATE_B, style=Link.Style.ABSOLUTE_PATH)
        private String absolutePath;

        @Link(value=TEMPLATE_B, style=Link.Style.ABSOLUTE)
        private String absolute;

        @Link(TEMPLATE_B)
        private String defaultStyle;

        private String id;

        public TestClassG(String id) {
            this.id = id;
        }
    }

    public void testLinkStyles() {
        System.out.println("Link styles");
        LinkProcessor<TestClassG> instance = new LinkProcessor(TestClassG.class);
        TestClassG testClass = new TestClassG("10");
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("widgets/10", testClass.relativePath);
        assertEquals("/application/resources/widgets/10", testClass.absolutePath);
        assertEquals("/application/resources/widgets/10", testClass.defaultStyle);
        assertEquals("http://example.com/application/resources/widgets/10", testClass.absolute);
    }

    public static class TestClassH {
        @Link(TEMPLATE_B)
        private String link;

        public String getId() {
            return "10";
        }
    }

    public void testComputedProperty() {
        System.out.println("Computed property");
        LinkProcessor<TestClassH> instance = new LinkProcessor(TestClassH.class);
        TestClassH testClass = new TestClassH();
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("/application/resources/widgets/10", testClass.link);
    }

    public static class TestClassI {
        @Link("widgets/${entity.id}")
        private String link;

        public String getId() {
            return "10";
        }
    }

    public void testEL() {
        System.out.println("EL link");
        LinkProcessor<TestClassI> instance = new LinkProcessor(TestClassI.class);
        TestClassI testClass = new TestClassI();
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("/application/resources/widgets/10", testClass.link);
    }

    public static class TestClassJ {
        @Link("widgets/${entity.id}/widget/{id}")
        private String link;

        public String getId() {
            return "10";
        }
    }

    public void testMixed() {
        System.out.println("Mixed EL and template vars link");
        LinkProcessor<TestClassJ> instance = new LinkProcessor(TestClassJ.class);
        TestClassJ testClass = new TestClassJ();
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("/application/resources/widgets/10/widget/10", testClass.link);
    }

    public static class DependentInnerBean {
        @Link("${entity.id}")
        public String outerUri;
        @Link("${instance.id}")
        public String innerUri;
        public String getId() {
            return "inner";
        }
    }

    public static class OuterBean {
        public DependentInnerBean inner = new DependentInnerBean();
        public String getId() {
            return "outer";
        }
    }

    public void testELScopes() {
        System.out.println("EL scopes");
        LinkProcessor<OuterBean> instance = new LinkProcessor(OuterBean.class);
        OuterBean testClass = new OuterBean();
        instance.processLinks(testClass, mockUriInfo);
        assertEquals("/application/resources/inner", testClass.inner.innerUri);
        assertEquals("/application/resources/outer", testClass.inner.outerUri);
    }

}