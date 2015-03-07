/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.factory;

import java.util.UUID;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.util.ROG;
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
public class BibTeXFactoryTest {
    
    public BibTeXFactoryTest() {
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

    @Test
    public void testArticle() {
        System.out.println("article");
        String id = "asdf";
        String title = "qwerty";
        String authors = "A. Someone and A. N. Onymous";
        String journal = "Int. J. Something";
        int year = 2010;
        BibTeX result = BibTeXFactory.article(id, title, authors, journal, year);
        assertEquals(id, result.getId());
        assertEquals(journal, result.getJournal());
        assertEquals(title, result.getTitle());
        assertEquals(authors, result.getAuthor());
        assertEquals(Integer.toString(year), result.getYear());
        assertEquals(BibTeX.BibTYPE.Article, result.getBibType());
    }

    @Test
    public void testInProceedings() {
        System.out.println("inProceedings");
        String id = "1234";
        String title = "This is it!";
        String authors = "A. N. Onymous";
        String booktitle = "Nice Book";
        int year = 2012;
        BibTeX b = BibTeXFactory.inProceedings(id, title, authors, booktitle, year);
        assertEquals(id, b.getId());
        assertEquals(title, b.getTitle());
        assertEquals(authors, b.getAuthor());
        assertEquals(booktitle, b.getBookTitle());
        assertEquals(Integer.toString(year), b.getYear());
        assertEquals(BibTeX.BibTYPE.Inproceedings, b.getBibType());       
    }

    @Test
    public void testPhdThesis() {
        System.out.println("phdThesis");
        ROG rog = new ROG(true);
        String id = UUID.randomUUID().toString();
        String title = rog.nextString(15);
        String authors = rog.nextString(15);
        String school = rog.nextString(15);
        int year = 2014;
        BibTeX b = BibTeXFactory.phdThesis(id, title, authors, school, year);
        assertEquals(id, b.getId());
        assertEquals(title, b.getTitle());
        assertEquals(authors, b.getAuthor());
        assertEquals(school, b.getSchool());
        assertEquals(Integer.toString(year), b.getYear());
        assertEquals(BibTeX.BibTYPE.Phdthesis, b.getBibType());  
    }

    @Test
    public void testBook() {
        System.out.println("book");
        ROG rog = new ROG(true);
        String id = UUID.randomUUID().toString();
        String title = rog.nextString(15);
        String authors = rog.nextString(15);
        String publisher = rog.nextString(15);
        String address = rog.nextString(15);
        String series = rog.nextString(15);
        int year = 1024;
        BibTeX b = BibTeXFactory.book(id, title, authors, publisher, address, series, year);        
         assertEquals(id, b.getId());
        assertEquals(title, b.getTitle());
        assertEquals(authors, b.getAuthor());
        assertEquals(publisher, b.getPublisher());
        assertEquals(address, b.getAddress());
        assertEquals(series, b.getSeries());        
        assertEquals(Integer.toString(year), b.getYear());
        assertEquals(BibTeX.BibTYPE.Book, b.getBibType());  
    }
    
}
