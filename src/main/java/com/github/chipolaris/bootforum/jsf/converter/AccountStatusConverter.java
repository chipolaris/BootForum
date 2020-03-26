package com.github.chipolaris.bootforum.jsf.converter;

import javax.faces.convert.EnumConverter;
import javax.faces.convert.FacesConverter;

import com.github.chipolaris.bootforum.enumeration.AccountStatus;

/**
 * 
 * Note: when using @javax.faces.convert.FacesConverter, the facelets must
 * refer to it using the converterId attribute. E.g.:
 * 
 * &lt;p:selectOneMenu...
 *  	&lt;f:converter converterId="accountStatusConverter"/&gt;
 * &lt;/selectOneMenu&gt; 
 * On the other hand, when implement javax.faces.convert.Converter interface and 
 * annotate with Spring @org.springframework.stereotype.Component, the facelet code
 * must refer to it using the converter attribute <... converter="#{converterBean}"..>
 */
@FacesConverter("accountStatusConverter")
public class AccountStatusConverter extends EnumConverter {

	public AccountStatusConverter() {
        super(AccountStatus.class);
    }
}
