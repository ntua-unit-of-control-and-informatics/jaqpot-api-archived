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
package org.jaqpot.core.model.validator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hampos
 */
public class EmailValidatorTest {

    public EmailValidatorTest() {
    }

    @Test
    public void testValidateWithoutExtension() {
        assertFalse(EmailValidator.validate("someone@somewhere"));
    }
    
    @Test
    public void testValidateWithWhitespace() {
        assertFalse(EmailValidator.validate("john smith@jaqpot.org"));
    }

    @Test
    public void testValidateWithoutAt() {
        assertFalse(EmailValidator.validate("someone.somewhere.gr"));
    }

    @Test
    public void testValidateWithoutDomain() {
        assertFalse(EmailValidator.validate("someone@gr"));
    }
    
    @Test
    public void testValidateNonLatin() {
        assertFalse(EmailValidator.validate("あいうえお@example.com"));
    }
    
    @Test
    public void testValidateGreek() {
        assertFalse(EmailValidator.validate("διεύθυνση@example.com"));
    }
    
    @Test
    public void testValidateWithIP() {
        assertFalse(EmailValidator.validate("email@123.123.123.123"));
    }        

    @Test
    public void testValidateWithoutName() {
        assertFalse(EmailValidator.validate("@somewhere.gr"));
    }

    @Test
    public void testValidateWithBadExtension() {
        assertFalse(EmailValidator.validate("someone@somewhere.asdf"));
    }

    @Test
    public void testValidateWithBadCharacters() {
        assertFalse(EmailValidator.validate("som\\eone@somewhere.com"));
        assertFalse(EmailValidator.validate("someone@some+where.com"));
        assertFalse(EmailValidator.validate("someone@some^where.com"));
        assertFalse(EmailValidator.validate("someone@some&where.com"));
        assertFalse(EmailValidator.validate("someone@some%where.com"));
        assertFalse(EmailValidator.validate("someone@some!where.com"));
        assertFalse(EmailValidator.validate("someone@some@where.com"));
        assertFalse(EmailValidator.validate("someone@some#where.com"));

    }

    @Test
    public void testNullEmail(){
        assertFalse(EmailValidator.validate(null));
    }
    
    @Test
    public void testTooLongEmail(){
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<50; i++) {sb.append("asdfg"); }
        sb.append("@");
        for (int i=0;i<20; i++) {sb.append("asdfg"); }
        sb.append(".org");
        assertTrue(sb.length()>256);
        assertFalse(EmailValidator.validate(sb.toString()));
    }
    
    @Test
    public void testValidateGood() {
        assertTrue(EmailValidator.validate("someone@somewhere.com"));
        assertTrue(EmailValidator.validate("someone@somewhere.anywhere.everywhere.com"));
        assertTrue(EmailValidator.validate("someone.anyone@somewhere.anywhere.everywhere.com"));
        assertTrue(EmailValidator.validate("someone-anyone@somewhere.anywhere.everywhere.com"));
        assertTrue(EmailValidator.validate("someone-anyone@somewhere-anywhere.everywhere.com"));
        assertTrue(EmailValidator.validate("someone+anyone@somewhere-anywhere.everywhere.com"));
    }
    

}
