/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalambos Chomenides, Pantelis Sopasakis)
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

import java.util.regex.Pattern;

/**
 * <p align=justify>A Class for validating email addresses according to the RFC
 * syntax rules (RFC 2822 specification) and other syntatical rules that can
 * identify various fake email addresses like john@yahoo.wtf. Two regular
 * expressions are used found at
 * http://code.iamcal.com/php/rfc822/full_regexp.txt and
 * http://www.regular-expressions.info/email.html respectively and a set of
 * valid email extensions was established from
 * http://www.velocityreviews.com/forums/t125158-java-email-validator.html.</p>
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class EmailValidator {

    private EmailValidator() {
        // Hidden Constructor - EmailValidator is a utility class.
    }

    /**
     * Regex copied from: http://www.regular-expressions.info/email.html
     */
    private static final String MAIL_REGEX_RFC_2822 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]//|\\\\[\\x01-"
            + "\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]"
            + "(?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
            + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
            + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    /**
     * Regex from http://code.iamcal.com/php/rfc822/full_regexp.txt
     */
    private static final String MAIL_REGEX_RFC_2822_FULL = "(((((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?"
            + "(([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])"
            + "+(\\x2e([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+)*)"
            + "((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|(((([\\x20\\x09]*(\\x0d\\x0a))?"
            + "[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?\\x22(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|"
            + "([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|[\\x21\\x23-\\x5b\\x5d-\\x7e])|"
            + "(\\x5c([\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f]|(\\x0a*\\x0d*([\\x00-\\x09\\x0b\\x0c\\x0e-\\x7f]\\x0a*\\x0d*)*))|"
            + "(\\x5c[\\x00-\\x7f]))))*((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?"
            + "\\x22((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|(((((([\\x20\\x09]*"
            + "(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|"
            + "[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|"
            + "([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)"
            + "[\\x20\\x09]+)*))*?\\x22(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?"
            + "(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|[\\x21\\x23-\\x5b\\x5d-\\x7e])|(\\x5c([\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f]|"
            + "(\\x0a*\\x0d*([\\x00-\\x09\\x0b\\x0c\\x0e-\\x7f]\\x0a*\\x0d*)*))|(\\x5c[\\x00-\\x7f]))))*((([\\x20\\x09]*(\\x0d\\x0a))?"
            + "[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?\\x22((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|"
            + "([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?))(\\x2e((((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+"
            + "((\\x0d\\x0a)[\\x20\\x09]+)*))*?([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f"
            + "\\x5e\\x5f\\x60\\x7b-\\x7e])+((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|"
            + "(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?\\x22(((([\\x20\\x09]*"
            + "(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|"
            + "[\\x21\\x23-\\x5b\\x5d-\\x7e])|(\\x5c([\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f]|(\\x0a*\\x0d*([\\x00-\\x09\\x0b\\x0c\\x0e-\\x7f]\\x0a*\\x0d*)*))|"
            + "(\\x5c[\\x00-\\x7f]))))*((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?"
            + "\\x22((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)))*))\\x40"
            + "((((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?"
            + "(([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+"
            + "(\\x2e([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+)*)"
            + "((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|"
            + "(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?\\x5b(((([\\x20\\x09]*"
            + "(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|"
            + "[\\x21-\\x5a\\x5e-\\x7e])|(\\x5c([\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f]|(\\x0a*\\x0d*"
            + "([\\x00-\\x09\\x0b\\x0c\\x0e-\\x7f]\\x0a*\\x0d*)*))|(\\x5c[\\x00-\\x7f]))))*((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|"
            + "([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))?\\x5d((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)|"
            + "((((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?([\\x41-\\x5a\\x61-\\x7a]|"
            + "[\\x30-\\x39]|[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+((([\\x20\\x09]*(\\x0d\\x0a))?"
            + "[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?)(\\x2e(((([\\x20\\x09]*(\\x0d\\x0a))?[\\x20\\x09]+)|"
            + "([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?([\\x41-\\x5a\\x61-\\x7a]|[\\x30-\\x39]|"
            + "[\\x21\\x23-\\x27\\x2a\\x2b\\x2d\\x2e\\x3d\\x3f\\x5e\\x5f\\x60\\x7b-\\x7e])+((([\\x20\\x09]*"
            + "(\\x0d\\x0a))?[\\x20\\x09]+)|([\\x20\\x09]+((\\x0d\\x0a)[\\x20\\x09]+)*))*?))*)))";
    /**
     * Extensions copied from
     * http://www.velocityreviews.com/forums/t125158-java-email-validator.html
     */
    private static final String[] mailExt = new String[]{
        "ac", "ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "aq",
        "ar", "as", "at", "au", "aw", "az", "ba", "bb", "bd", "be", "bf",
        "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv",
        "bw", "by", "bz", "ca", "cc", "cd", "cf", "cg", "ch", "ci", "ck",
        "cl", "cm", "cn", "co", "cr", "cu", "cv", "cx", "cy", "cz", "de",
        "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "er", "es",
        "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "fx", "ga", "gb", "gd",
        "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr",
        "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu",
        "id", "ie", "il", "im", "in", "io", "iq", "ir", "is", "it", "je",
        "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr",
        "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt",
        "lu", "lv", "ly", "ma", "mc", "md", "mg", "mh", "mk", "ml", "mm",
        "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx",
        "my", "mz", "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np",
        "nr", "nu", "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl",
        "pm", "pn", "pr", "ps", "pt", "pw", "py", "qa", "re", "ro", "ru",
        "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk",
        "sl", "sm", "sn", "so", "sr", "st", "sv", "sy", "sz", "tc", "td",
        "tf", "tg", "th", "tj", "tk", "tm", "tn", "to", "tp", "tr", "tt",
        "tv", "tw", "tz", "ua", "ug", "uk", "um", "us", "uy", "uz", "va",
        "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws", "ye", "yt", "yu",
        "za", "zm", "zw", "aero", "biz", "coop", "com", "edu", "gov", "info",
        "mil", "museum", "name", "net", "org", "pro", "jobs"};

    /**
     * Validate an email address
     *
     * @param mail The email address to be validated
     * @return <code>true</code> if the e-mail address is RFC compliant and
     * meets the structural requirements imposed by this class.
     */
    public static boolean validate(String mail) {
        if (mail == null) {
            return false;
        }
        if (mail.length() > 256) { //too long email! (RFC 5321 specs)
            return false;
        }
        StringBuilder sb = new StringBuilder("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[A-Z]{2}|");
        for (int i = 0; i < mailExt.length; i++) {
            sb.append(mailExt[i]);
            if (i != mailExt.length - 1) {
                sb.append("|");
            }
        }
        sb.append(")\\b");
        String regex1 = new String(sb);
        Pattern pattern1 = Pattern.compile(regex1);
        Pattern pattern2 = Pattern.compile(MAIL_REGEX_RFC_2822);
        Pattern pattern3 = Pattern.compile(MAIL_REGEX_RFC_2822_FULL);
        return !(!pattern1.matcher(mail).matches() || !pattern2.matcher(mail).matches() || !pattern3.matcher(mail).matches());
    }
}
