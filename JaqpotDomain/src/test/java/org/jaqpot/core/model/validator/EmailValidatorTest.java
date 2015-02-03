/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    public void testValidateWithoutAt() {
        assertFalse(EmailValidator.validate("someone.somewhere.gr"));
    }

    @Test
    public void testValidateWithoutDomain() {
        assertFalse(EmailValidator.validate("someone@gr"));
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
        assertFalse(EmailValidator.validate("som\\eone@somewhere.asdf"));
        assertFalse(EmailValidator.validate("someone@some+where.asdf"));

    }

    @Test
    public void testValidateGood() {
        assertTrue(EmailValidator.validate("someone@somewhere.com"));
        assertTrue(EmailValidator.validate("info.someone@somewhere.asdf"));
        assertTrue(EmailValidator.validate("someone@somewhere.anywhere.everywhere.com"));
        assertTrue(EmailValidator.validate("someone@some-where.asdf"));
        assertTrue(EmailValidator.validate("info-someone@somewhere.asdf"));
    }

}
