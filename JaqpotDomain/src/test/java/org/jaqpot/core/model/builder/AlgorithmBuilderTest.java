/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model.builder;

import java.util.Arrays;
import java.util.HashSet;
import org.jaqpot.core.model.Algorithm;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class AlgorithmBuilderTest {

    public AlgorithmBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addTitles method, of class AlgorithmBuilder.
     */
    @Test
    public void testAddTitles() {
        System.out.println("addTitles");
        String[] titles = {"title1", "title2", "this is another title"};
        Algorithm a = AlgorithmBuilder
                .builderRandomId()
                .addTitles(titles)
                .build();
        assertNotNull(a.getMeta());
        assertNotNull(a.getMeta().getTitles());
        assertEquals(titles.length, a.getMeta().getTitles().size());
        for (String t : titles) {
            assertTrue(a.getMeta().getTitles().contains(t));
        }
    }

    @Test
    public void testAddTitlesNull() {
        System.out.println("addTitles");
        String[] titles = null;
        Algorithm a = AlgorithmBuilder
                .builderRandomId()
                .addTitles(titles)
                .build();
        assertNull(a.getMeta());

    }

    /**
     * Test of addDescriptions method, of class AlgorithmBuilder.
     */
    @Test
    public void testAddDescriptions() {
        System.out.println("addDescriptions");
        String[] descriptions = {"descr1", "descr2", "this is another description"};
        Algorithm a = AlgorithmBuilder
                .builderRandomId()
                .addDescriptions(descriptions)
                .build();
        assertNotNull(a.getMeta());
        assertNotNull(a.getMeta().getDescriptions());
        assertEquals(descriptions.length, a.getMeta().getDescriptions().size());
        assertTrue(a.getMeta().getDescriptions().containsAll(new HashSet<>(Arrays.asList(descriptions))));
    }

    @Test
    public void testAddDescriptionsNull() {
        System.out.println("addDescriptions");
        String[] descriptions = null;
        AlgorithmBuilder
                .builderRandomId()
                .addDescriptions(descriptions)
                .build();
    }

    /**
     * Test of addTags method, of class AlgorithmBuilder.
     */
    @Test
    public void testAddTags() {
        System.out.println("addTags");
        String[] tags = {"algorithm", "training", "regression", "supervised"};
        Algorithm a = AlgorithmBuilder
                .builderRandomId()
                .addTags(tags)
                .build();
        assertNotNull(a.getMeta());
        assertNotNull(a.getMeta().getSubjects());
        assertEquals(tags.length, a.getMeta().getSubjects().size());
        assertTrue(a.getMeta().getSubjects().containsAll(new HashSet<>(Arrays.asList(tags))));
    }

    @Test
    public void testAddTagsNull() {
        System.out.println("addTagsNull");
        AlgorithmBuilder
                .builderRandomId()
                .addTags((String[]) null)
                .build();
    }

    @Test
    public void testAddTagsCSV() {
        System.out.println("addTagsCSV");
        String tagList = "tag1, tag2,tag3 , tag4    ";
        Algorithm a = AlgorithmBuilder
                .builderRandomId()
                .addTagsCSV(tagList)
                .build();               
        
        for (int i = 1; i <= 4; ++i) {
            assertTrue("Not found: 'tag" + i + "'", 
                    a.getMeta().getSubjects().contains("tag" + i));
        }
        
    }

}
