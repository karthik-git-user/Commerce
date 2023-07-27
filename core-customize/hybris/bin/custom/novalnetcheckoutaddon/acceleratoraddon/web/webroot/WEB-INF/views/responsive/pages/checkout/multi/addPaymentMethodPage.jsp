<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="multiCheckoutNovalnet" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi" %>
<script src="https://cdn.novalnet.de/js/v2/NovalnetUtility.js"></script>
<spring:htmlEscape defaultHtmlEscape="true"/>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">
    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">

                                <div class="headline"><spring:theme code="checkout.multi.paymentMethod"/></div>

                                    <ycommerce:testId code="paymentDetailsForm">

                                        <form:form id="paymentDetailsForm" name="paymentDetailsForm" modelAttribute="paymentDetailsForm"  method="POST">

                                        <div id="billingAdrressInfo" style="display:block">
                                            <h1 class="headline">
                                                <spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress"/></h1>

                                            <c:if test="${cartData.deliveryItemsQuantity > 0}">
                                                <div id="useDeliveryAddressData"
                                                    data-title="${fn:escapeXml(deliveryAddress.title)}"
                                                    data-firstname="${fn:escapeXml(deliveryAddress.firstName)}"
                                                    data-lastname="${fn:escapeXml(deliveryAddress.lastName)}"
                                                    data-line1="${fn:escapeXml(deliveryAddress.line1)}"
                                                    data-line2="${fn:escapeXml(deliveryAddress.line2)}"
                                                    data-town="${fn:escapeXml(deliveryAddress.town)}"
                                                    data-postalcode="${fn:escapeXml(deliveryAddress.postalCode)}"
                                                    data-countryisocode="${fn:escapeXml(deliveryAddress.country.isocode)}"
                                                    data-regionisocode="${fn:escapeXml(deliveryAddress.region.isocodeShort)}"
                                                    data-address-id="${fn:escapeXml(deliveryAddress.id)}"
                                                ></div>

                                                <formElement:formCheckbox
                                                    path="useDeliveryAddress"
                                                    idKey="useDeliveryAddress"
                                                    labelKey="checkout.multi.sop.useMyDeliveryAddress"
                                                    tabindex="11"/>
                                            </c:if>

                                            <div id="novalnetBillAddressForm">
                                                <address:billAddressFormSelector supportedCountries="${countries}" regions="${regions}" tabindex="12"/>
                                            </div>

                                            <h1 class="headline">
                                                <spring:theme code="checkout.summary.select.payment.method"/></h1>

                                        <input type="hidden" id="ship_firstName" value="${fn:escapeXml(deliveryAddress.firstName)}"/>
                                        <input type="hidden" id="ship_lastName" value="${fn:escapeXml(deliveryAddress.lastName)}"/>
                                        <input type="hidden" id="ship_zip" value="${fn:escapeXml(deliveryAddress.postalCode)}"/>
                                        <input type="hidden" id="ship_country" value="${fn:escapeXml(deliveryAddress.country.isocode)}"/>
                                        <input type="hidden" id="ship_city" value="${fn:escapeXml(deliveryAddress.town)}"/>
                                        <input type="hidden" id="ship_street" value="${fn:escapeXml(deliveryAddress.line1)} ${fn:escapeXml(deliveryAddress.line2)}"/>
                                        <input type="hidden" id="email" value="${email} "/>

                                        <c:if test="${novalnetBaseStoreConfiguration.novalnetTariffId != null && novalnetBaseStoreConfiguration.novalnetPaymentAccessKey != null}">
                                        <div class="paymentMethods">
                                        <c:if test="${novalnetCreditCard.active == true}">

                                            <div class="novalnet-select-payment">
                                                <form:radiobutton path="selectedPaymentMethodId" id="novalnetCreditCard" value="novalnetCreditCard" label="${novalnetCreditCard.name}"/>

                                                &nbsp;&nbsp;<a href=<spring:theme code="http://www.novalnet.com"/> target="_new">

                                                <c:choose>
                                                    <c:when test="${novalnetCreditCard.novalnetAmexLogo == true}">
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master_amex.png" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master.png" />
                                                    </c:otherwise>
                                                </c:choose>

                                                </a>&nbsp;&nbsp;
                                                <div id="novalnetCreditCardPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetCreditCard.novalnetTestMode == true}">
                                                        <input type="hidden" id="novalnetTestMode" value=1/>
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${novalnetCreditCard.novalnetEndUserInfo != null}">
                                                        ${novalnetCreditCard.novalnetEndUserInfo} <br/>
                                                    </c:if>
                                                    <form:hidden path="novalnetCreditCardPanHash" id="novalnetCreditCardPanHash"/>
                                                        <form:hidden path="novalnetCreditCardUniqueId" id="novalnetCreditCardUniqueId"/>
                                                        <form:hidden path="do_redirect" id="do_redirect"/>

                                                        <input type="hidden" id="novalnetStandardLabelCss" value="${novalnetCreditCard.novalnetStandardLabelCss}"/>
                                                        <input type="hidden" id="novalnetStandardInputCss" value="${novalnetCreditCard.novalnetStandardInputCss}"/>
                                                        <input type="hidden" id="novalnetStandardCss" value="${novalnetCreditCard.novalnetStandardCss}"/>
                                                        <c:if test="${novalnetCreditCard.novalnetInlineCC == true}">
                                                            <input type="hidden" id="novalnetInlineCC" value=1/>
                                                        </c:if>
                                                        <input type="hidden" id="lang" value="${lang}"/>
                                                        <input type="hidden" id="orderAmount" value="${orderAmountCent}"/>
                                                        <input type="hidden" id="currency" value="${currency}"/>
                                                        <input type="hidden" id="Clientkey" value="${novalnetBaseStoreConfiguration.novalnetClientKey}"/>

                                                    <c:if test="${novalnetCreditCardOneClick == true}">

                                                        <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData1" value="1" label= "${novalnetCreditCardOneClickCardType} ${endswith} ${novalnetCreditCardOneClickMaskedCardNumber} ( ${expires} ${novalnetCreditCardOneClickCardExpiry}) " tabindex="12"/>
                                                        <br/>
                                                        <c:if test="${novalnetCreditCardOneClickToken2 != null}">
                                                            <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData2" value="2" label="${novalnetCreditCardOneClickCardType2} ${endswith} ${novalnetCreditCardOneClickMaskedCardNumber2}  ( ${expires} ${novalnetCreditCardOneClickCardExpiry2}) " tabindex="12"/>
                                                        <br/>
                                                        </c:if>
                                                        <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickNewDeatails" value="3" label="${creditcardAddNew}" tabindex="12"/>
                                                        <div class="novalnetCreditCardOneClickForm" style = "display:none">

                                                            <a href="" class="help js-cart-help" data-help=""><spring:theme code="novalnet.creditcardOneClickInfo.text"/><span class="glyphicon glyphicon-question-sign"></span></a>
                                                            <div class="help-popup-content-holder js-help-popup-content novalnet-hide">
                                                                <div class="help-popup-content">
                                                                    <spring:theme code="novalnet.creditcardOneClickInfo.description"/>
                                                                </div>
                                                            </div>
                                                            <br/><br/>
                                                            <div id = "nn_overlay">
                                                            </div>
                                                            <iframe id='novalnetCreditCardIframe' frameborder='0' scrolling='no' style = "min-width: 40%;"></iframe><br/>
                                                            <formElement:formCheckbox
                                                            path="creditcardSaveData"
                                                            idKey="creditcardSaveData"
                                                            labelKey="novalnet.creditcard.aftersaveData"
                                                            tabindex="11"/>
                                                        </div>

                                                    </c:if>
                                                    <c:if test="${novalnetCreditCardOneClick == false}">
                                                    <span id="novalnetCreditCardPaymentFormElements">

                                                        <c:if test="${novalnetCreditCardOneClickEnabled == true}">
                                                            <a href="" class="help js-cart-help" data-help=""><spring:theme code="novalnet.creditcardOneClickInfo.text"/><span class="glyphicon glyphicon-question-sign"></span></a>
                                                            <div class="help-popup-content-holder js-help-popup-content novalnet-hide">
                                                                <div class="help-popup-content">
                                                                    <spring:theme code="novalnet.creditcardOneClickInfo.description"/>
                                                                </div>
                                                            </div>
                                                            <br/><br/>
                                                        </c:if>
                                                        <div id = "nn_overlay">
                                                        </div>
                                                        <iframe id="novalnetCreditCardIframe" frameborder="0" scrolling="no" style = "min-width: 40%;"></iframe>

                                                         <c:if test="${novalnetCreditCardOneClickEnabled == true}">
                                                            <formElement:formCheckbox
                                                            path="creditcardSaveData"
                                                            idKey="creditcardSaveData"
                                                            labelKey="novalnet.creditcard.beforesaveData"
                                                            tabindex="11"/>
                                                        </c:if>

                                                    </span>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetCreditCard.description}
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetGuaranteedDirectDebitSepa.active == true}">
                                            <input type="hidden" name="sepaforceGuaranteeCheck" id="sepaforceGuaranteeCheck" value="${novalnetGuaranteedDirectDebitSepa.novalnetForceGuarantee}">
                                        </c:if>
                                        <c:if test="${novalnetGuaranteedDirectDebitSepa.active == true && orderAmountCent > novalnetGuaranteedDirectDebitSepaMinAmount}">

                                            <input type="hidden" name="sepaGuaranteeCheck" id="sepaGuaranteeCheck" value="1">
                                            <div class="novalnetGuaranteedDirectDebitSepa">
                                                <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedDirectDebitSepa" value="novalnetGuaranteedDirectDebitSepa" label="${novalnetGuaranteedDirectDebitSepa.name}"/>
                                                    &nbsp;&nbsp;
                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedDirectDebitSepa.png" />
                                                    </a>&nbsp;&nbsp;
                                                </div>
                                                <div id="novalnetGuaranteedDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">

                                                    <c:if test="${novalnetGuaranteedDirectDebitSepa.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${novalnetGuaranteedDirectDebitSepa.novalnetEndUserInfo != null}">
                                                        ${novalnetGuaranteedDirectDebitSepa.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <c:if test="${novalnetGuaranteedDirectDebitSepaOneClick == true}">
                                                        <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1" id="GuaranteedDirectDebitSepaOneClickData1" value="1" label="IBAN ${novalnetDirectDebitSepaAccountIban}" tabindex="12"/>
                                                        <br/>
                                                        <c:if test="${novalnetDirectDebitSepaOneClickToken2 != null}">
                                                            <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1" id="guaranteedDirectDebitSepaOneClickData2" value="2" label="IBAN ${novalnetDirectDebitSepaAccountIban2}" tabindex="12"/>
                                                            <br/>
                                                        </c:if>
                                                        <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1" id="guaranteedDirectDebitSepaOneClickData1AddNew" value="3" label="${sepaAddNew}" tabindex="12"/>
                                                        <div class="novalnetGuaranteedDirectDebitSepaOneClickForm" style = "display:none">
                                                            <div class="form-group">
                                                                <formElement:formInputBox idKey="guaranteeAccountIban" labelKey="novalnet.iban" path="guaranteeAccountIban" inputCSS="form-control" placeholder= "DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                                                            </div>
                                                            <br/>
                                                            <formElement:formCheckbox
                                                            path="guaranteedDirectDebitSepaSaveData"
                                                            idKey="guaranteedDirectDebitSepaSaveData"
                                                            labelKey="novalnet.sepa.aftersaveData"
                                                            tabindex="11"/>
                                                        </div>

                                                    </c:if>
                                                    <c:if test="${novalnetGuaranteedDirectDebitSepaOneClick == false}">


                                                        <span id="novalnetDirectDebitSepaPaymentFormElements">
                                                            <div class="form-group">
                                                                <formElement:formInputBox idKey="guaranteeAccountIban" labelKey="novalnet.iban" path="guaranteeAccountIban" inputCSS="form-control" placeholder= "DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                                                            </div>
                                                        </span>
                                                        <c:if test="${novalnetGuaranteedDirectDebitSepaOneClickEnabled == true}">
                                                            <formElement:formCheckbox
                                                            path="guaranteedDirectDebitSepaSaveData"
                                                            idKey="guaranteedDirectDebitSepaSaveData"
                                                            labelKey="novalnet.sepa.beforesaveData"
                                                            tabindex="11"/>
                                                        </c:if>
                                                    </c:if>
                                                    <div class="form-group">
                                                        <multiCheckoutNovalnet:formDate path="novalnetGuaranteedDirectDebitSepaDateOfBirth" idKey="novalnetGuaranteedDirectDebitSepaDateOfBirth" labelKey="novalnet.dob" inputCSS="" labelCSS=""/>
                                                    </div>
                                                    <div class="description novalnet-info-box">
                                                        <ul>
                                                            <li>${novalnetGuaranteedDirectDebitSepa.description}</li> <br/>
                                                            <li>
                                                                <div class="form-group">
                                                                    <a id="novalnet-sepa-mandate" style="cursor:pointer;" onclick="jQuery( '#novalnet-about-mandate' ).toggle();" ><spring:theme code="novalnet.sepaNotificationText"/></a>
                                                                </div>
                                                                <div class="form-group" id="novalnet-about-mandate" style="display:none;">
                                                                    <spring:theme code="novalnet.sepaAboutMandateDescOne"/><br/><br/><strong><spring:theme code="novalnet.sepaAboutMandateDescTwo"/></strong><br/><br/><spring:theme code="novalnet.sepaAboutMandateDescThree"/>
                                                                </div>
                                                            </li>
                                                        </ul>
                                                    </div><br/>
                                                </div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetDirectDebitSepa.active == true}">
                                            <div class="novalnetSepa" style = "display:none;">
                                                <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetDirectDebitSepa" value="novalnetDirectDebitSepa" label="${novalnetDirectDebitSepa.name}"/>
                                                    &nbsp;&nbsp;
                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetDirectDebitSepa.png" />


                                                    </a>&nbsp;&nbsp;
                                                </div>
                                                <div id="novalnetDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">


                                                    <c:if test="${novalnetDirectDebitSepa.novalnetTestMode == true}">

                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/>
                                                        </div><br/>
                                                    </c:if>
                                                    <c:if test="${novalnetDirectDebitSepa.novalnetEndUserInfo != null}">
                                                        ${novalnetDirectDebitSepa.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <c:if test="${novalnetDirectDebitSepaOneClick == true}">
                                                        <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1" value="1" label="IBAN  ${novalnetDirectDebitSepaAccountIban}" tabindex="12"/>
                                                        <br/>
                                                        <c:if test="${novalnetDirectDebitSepaOneClickToken2 != null}">
                                                            <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData2" value="2" label="IBAN ${novalnetDirectDebitSepaAccountIban2}" tabindex="12"/>
                                                            <br/>
                                                        </c:if>
                                                        <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1AddNew" value="3" label="${sepaAddNew}" tabindex="12"/>
                                                        <div class="novalnetDirectDebitSepaOneClickForm" style = "display:none">

                                                            <div class="form-group">
                                                                    <formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" placeholder= "DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                                                            </div>
                                                            <br/>
                                                            <formElement:formCheckbox
                                                            path="directDebitSepaSaveData"
                                                            idKey="directDebitSepaSaveData"
                                                            labelKey="novalnet.sepa.aftersaveData"
                                                            tabindex="11"/>

                                                        </div>

                                                    </c:if>
                                                    <c:if test="${novalnetDirectDebitSepaOneClick == false}">

                                                        <span id="novalnetDirectDebitSepaPaymentFormElements">
                                                            <div class="form-group">
                                                                <formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" placeholder= "DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                                                            </div>
                                                            <br/>
                                                            <c:if test="${novalnetDirectDebitSepaOneClickEnabled == true}">
                                                                <formElement:formCheckbox
                                                                path="directDebitSepaSaveData"
                                                                idKey="directDebitSepaSaveData"
                                                                labelKey="novalnet.sepa.beforesaveData"
                                                                tabindex="11"/>
                                                            </c:if>

                                                        </span>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">
                                                        <ul>
                                                            <li>${novalnetDirectDebitSepa.description}</li><br/>
                                                            <li>
                                                                <div class="form-group">
                                                                    <a id="novalnet-sepa-mandate" style="cursor:pointer;" onclick="jQuery( '#novalnet-about-mandate' ).toggle();" ><spring:theme code="novalnet.sepaNotificationText"/></a>
                                                                </div>
                                                                <div class="form-group" id="novalnet-about-mandate" style="display:none;">
                                                                    <spring:theme code="novalnet.sepaAboutMandateDescOne"/><br/><br/><strong><spring:theme code="novalnet.sepaAboutMandateDescTwo"/></strong><br/><br/><spring:theme code="novalnet.sepaAboutMandateDescThree"/>
                                                                </div>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetPayPal.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetPayPal" value="novalnetPayPal" label="${novalnetPayPal.name}"/>
                                                    &nbsp;&nbsp;


                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPayPal.png" />


                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetPayPalPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetPayPal.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/>
                                                        </div>
                                                        <br/>
                                                    </c:if>

                                                    <c:if test="${novalnetPayPal.novalnetEndUserInfo != null}">
                                                        ${novalnetPayPal.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                        <c:if test="${novalnetPayPalOneClick == true}">
                                                            <form:radiobutton path="payPalOneClickData1" id="payPalOneClickData1" value="1" label="${novalnetPaypalEmailID} ${novalnetPayPalTransactionId}" tabindex="12"/>
                                                            <br/>
                                                            <c:if test="${novalnetPayPalOneClickToken2 != null}">
                                                                <form:radiobutton path="payPalOneClickData1" id="payPalOneClickData2" value="2" label="${novalnetPaypalEmailID2} ${novalnetPayPalTransactionId2}" tabindex="12"/>
                                                                <br/>
                                                            </c:if>
                                                            <form:radiobutton path="payPalOneClickData1" id="payPalOneClickData1AddNew" value="3" label="${paypalAddNew}" tabindex="12"/>
                                                            <div class="novalnetPayPalOneClickForm" style = "display:none">
                                                                <formElement:formCheckbox
                                                                path="paypalSaveData"
                                                                idKey="paypalSaveData"
                                                                labelKey="novalnet.paypal.aftersaveData"
                                                                tabindex="11"/>
                                                                <br/><br/>
                                                            </div>
                                                        </c:if>
                                                        <c:if test="${novalnetPayPalOneClick == false}">
                                                    <span id="novalnetPayPalPaymentFormElements">
                                                        <c:if test="${novalnetPayPalOneClickEnabled == true}">
                                                            <formElement:formCheckbox
                                                            path="paypalSaveData"
                                                            idKey="paypalSaveData"
                                                            labelKey="novalnet.paypal.beforesaveData"
                                                            tabindex="11"/>
                                                        </c:if>
                                                    </span>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetPayPal.description}</div><br/>
                                            </div>

                                        </c:if>
                                        <c:if test="${novalnetInvoice.active == true}">
                                            <div class="novalnetInvoice" style = "display:none;">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetInvoice" value="novalnetInvoice" label="${novalnetInvoice.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetInvoice.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetInvoicePaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                <c:if test="${novalnetInvoice.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/>
                                                    </div></br>
                                                </c:if>
                                                <c:if test="${novalnetDirectDebitSepa.novalnetEndUserInfo != null}">
                                                    ${novalnetDirectDebitSepa.novalnetEndUserInfo}<br/>
                                                </c:if>
                                                <div class="description novalnet-info-box">${novalnetInvoice.description}</div><br/>
                                            </div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetGuaranteedInvoice.active == true }">
                                                <input type="hidden" name="invoiceforceGuaranteeCheck" id="invoiceforceGuaranteeCheck" value="${novalnetGuaranteedInvoice.novalnetForceGuarantee}">
                                            </c:if>
                                        <c:if test="${novalnetGuaranteedInvoice.active == true && orderAmountCent >= novalnetGuaranteedInvoiceMinAmount}">
                                            <input type="hidden" name="invoiceGuaranteeCheck" id="invoiceGuaranteeCheck" value="1">

                                            <div class="novalnetGuaranteedInvoice">
                                            <div class="novalnet-select-payment">

                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedInvoice" value="novalnetGuaranteedInvoice" label="${novalnetGuaranteedInvoice.name}"/>
                                                    &nbsp;&nbsp;


                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedInvoice.png" />


                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetGuaranteedInvoicePaymentForm" style="display:none;" class="novalnetPaymentForm">


                                                <c:if test="${novalnetGuaranteedInvoice.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/><br/>
                                                    </div></br>

                                                </c:if>
                                                <c:if test="${novalnetGuaranteedInvoice.novalnetEndUserInfo != null}">
                                                        ${novalnetGuaranteedInvoice.novalnetEndUserInfo}<br/>
                                                    </c:if>

                                                <div class="form-group">
                                                    <multiCheckoutNovalnet:formDate path="novalnetGuaranteedInvoiceDateOfBirth" idKey="novalnetGuaranteedInvoiceDateOfBirth" labelKey="novalnet.dob" inputCSS="" labelCSS=""/>
                                                </div>
                                                <div class="description novalnet-info-box">${novalnetGuaranteedInvoice.description}</div>
                                            </div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetPrepayment.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetPrepayment" value="novalnetPrepayment" label="${novalnetPrepayment.name}"/>
                                                    &nbsp;&nbsp;


                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPrepayment.png" />

                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetPrepaymentPaymentForm" style="display:none;" class="novalnetPaymentForm">


                                                    <c:if test="${novalnetPrepayment.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div><br/>
                                                    </c:if>
                                                    <c:if test="${novalnetPrepayment.novalnetEndUserInfo != null}">
                                                        ${novalnetPrepayment.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetPrepayment.description}</div>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetMultibanco.active == true}">
                                            <div class="novalnet-select-payment">
                                                <form:radiobutton path="selectedPaymentMethodId" id="novalnetMultibanco" value="novalnetMultibanco" label="${novalnetMultibanco.name}"/>
                                                &nbsp;&nbsp;


                                                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetMultibanco.png" />
                                                </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetMultibancoPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                <c:if test="${novalnetMultibanco.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/><br/>
                                                    </div><br/>
                                                    </c:if>
                                                    <c:if test="${novalnetMultibanco.novalnetEndUserInfo != null}">
                                                        ${novalnetMultibanco.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetMultibanco.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetBarzahlen.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetBarzahlen" value="novalnetBarzahlen" label="${novalnetBarzahlen.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetBarzahlen.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetBarzahlenPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetBarzahlen.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                        <c:if test="${novalnetBarzahlen.novalnetEndUserInfo != null}">
                                                            ${novalnetBarzahlen.novalnetEndUserInfo}<br/>
                                                        </c:if>

                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetBarzahlen.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetIdeal.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetIdeal" value="novalnetIdeal" label="${novalnetIdeal.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetIdeal.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetIdealPaymentForm" style="display:none;" class="novalnetPaymentForm">


                                                    <c:if test="${novalnetIdeal.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                        <c:if test="${novalnetIdeal.novalnetEndUserInfo != null}">
                                                            ${novalnetIdeal.novalnetEndUserInfo}<br/>
                                                        </c:if>

                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetIdeal.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetGiropay.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetGiropay" value="novalnetGiropay" label="${novalnetGiropay.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGiropay.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetGiropayPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetGiropay.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                    </c:if>
                                                    <c:if test="${novalnetGiropay.novalnetEndUserInfo != null}">
                                                        ${novalnetGiropay.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetGiropay.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetPrzelewy24.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetPrzelewy24" value="novalnetPrzelewy24" label="${novalnetPrzelewy24.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPrzelewy24.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetPrzelewy24PaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetPrzelewy24.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>

                                                    </c:if>
                                                    <c:if test="${novalnetPrzelewy24.novalnetEndUserInfo != null}">
                                                        ${novalnetPrzelewy24.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetPrzelewy24.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetEps.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetEps" value="novalnetEps" label="${novalnetEps.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetEps.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetEpsPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetEps.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                    </c:if>
                                                    <c:if test="${novalnetEps.novalnetEndUserInfo != null}">
                                                        ${novalnetEps.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetEps.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetInstantBankTransfer.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetInstantBankTransfer" value="novalnetInstantBankTransfer" label="${novalnetInstantBankTransfer.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetInstantBankTransfer.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetInstantBankTransferPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetInstantBankTransfer.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                    </c:if>
                                                    <c:if test="${novalnetInstantBankTransfer.novalnetEndUserInfo != null}">
                                                        ${novalnetInstantBankTransfer.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetInstantBankTransfer.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetOnlineBankTransfer.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetOnlineBankTransfer" value="novalnetOnlineBankTransfer" label="${novalnetOnlineBankTransfer.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetOnlineBankTransfer.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetOnlineBankTransferPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                    <c:if test="${novalnetOnlineBankTransfer.novalnetTestMode == true}">
                                                        <div id= "testModeText">
                                                            <spring:theme code="novalnet.testModeText"/><br/>
                                                        </div></br>
                                                    </c:if>
                                                    <c:if test="${novalnetOnlineBankTransfer.novalnetEndUserInfo != null}">
                                                        ${novalnetOnlineBankTransfer.novalnetEndUserInfo}<br/>
                                                    </c:if>
                                                    <div class="description novalnet-info-box">${novalnetOnlineBankTransfer.description}</div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetBancontact.active == true}">
                                            <div class="novalnet-select-payment">
                                                    <form:radiobutton path="selectedPaymentMethodId" id="novalnetBancontact" value="novalnetBancontact" label="${novalnetBancontact.name}"/>
                                                    &nbsp;&nbsp;
                                                        <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetBancontact.png" />
                                                    </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetBancontactPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                <c:if test="${novalnetBancontact.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/><br/>
                                                    </div></br>
                                                </c:if>
                                                <c:if test="${novalnetBancontact.novalnetEndUserInfo != null}">
                                                    ${novalnetBancontact.novalnetEndUserInfo}<br/>
                                                </c:if>
                                                <div class="description novalnet-info-box">
                                                    ${novalnetBancontact.description}
                                                </div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetPostFinance.active == true}">
                                            <div class="novalnet-select-payment">
                                                <form:radiobutton path="selectedPaymentMethodId" id="novalnetPostFinance" value="novalnetPostFinance" label="${novalnetPostFinance.name}"/>
                                                &nbsp;&nbsp;
                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPostFinance.png" />
                                                </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetPostFinancePaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                <c:if test="${novalnetPostFinance.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/><br/><br/>
                                                    </div></br>
                                                </c:if>
                                                <c:if test="${novalnetPostFinance.novalnetEndUserInfo != null}">
                                                    <br/><br/>${novalnetPostFinance.novalnetEndUserInfo}<br/>
                                                </c:if>
                                                <div class="description novalnet-info-box">
                                                    ${novalnetPostFinance.description}
                                                </div><br/>
                                            </div>
                                        </c:if>
                                        <c:if test="${novalnetPostFinanceCard.active == true}">
                                            <div class="novalnet-select-payment">
                                                <form:radiobutton path="selectedPaymentMethodId" id="novalnetPostFinanceCard" value="novalnetPostFinanceCard" label="${novalnetPostFinanceCard.name}"/>
                                                &nbsp;&nbsp;
                                                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPostFinanceCard.png" />
                                                </a>&nbsp;&nbsp;
                                            </div>
                                            <div id="novalnetPostFinanceCardPaymentForm" style="display:none;" class="novalnetPaymentForm">
                                                <c:if test="${novalnetPostFinanceCard.novalnetTestMode == true}">
                                                    <div id= "testModeText">
                                                        <spring:theme code="novalnet.testModeText"/><br/><br/>
                                                    </div></br>
                                                </c:if>
                                                <c:if test="${novalnetPostFinanceCard.novalnetEndUserInfo != null}">
                                                    <br/><br/>${novalnetPostFinanceCard.novalnetEndUserInfo}<br/>
                                                </c:if>
                                                <div class="description novalnet-info-box">
                                                    ${novalnetPostFinanceCard.description}
                                                </div><br/>
                                            </div>
                                        </c:if>
                                        
                                        </div>
                                        </c:if>
                                            <p><spring:theme code="checkout.multi.paymentMethod.seeOrderSummaryForMoreInformation"/></p>
                                        </div>
                                        </form:form>
                                        <button type="button"
                                                class="btn btn-primary btn-block submit_novalnetPaymentDetailsForm checkout-next" id = "submit_novalnetPaymentDetailsForm">
                                                <spring:theme code="checkout.multi.paymentMethod.continue"/>
                                        </button>
                                    </ycommerce:testId>
                            </div>
                        </div>

                        <c:if test="${not empty paymentInfos}">
                            <div id="savedpayments">
                                <div id="savedpaymentstitle">
                                    <div class="headline">
                                        <span class="headline-text"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useSavedCard"/></span>
                                    </div>
                                </div>
                                <div id="savedpaymentsbody">
                                    <spring:url var="choosePaymentMethod" value="{contextPath}/checkout/multi/payment-method/choose" htmlEscape="false">
                                        <spring:param name="contextPath" value="${request.contextPath}" />
                                    </spring:url>
                                    <c:forEach items="${paymentInfos}" var="paymentInfo" varStatus="status">
                                        <form action="${fn:escapeXml(choosePaymentMethod)}" method="GET">
                                            <input type="hidden" name="selectedPaymentMethodId" value="${fn:escapeXml(paymentInfo.id)}"/>
                                                    <strong>${fn:escapeXml(paymentInfo.billingAddress.firstName)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.lastName)}</strong><br/>
                                                    ${fn:escapeXml(paymentInfo.cardType)}<br/>
                                                    ${fn:escapeXml(paymentInfo.accountHolderName)}<br/>
                                                    ${fn:escapeXml(paymentInfo.cardNumber)}<br/>
                                                    <spring:theme code="checkout.multi.paymentMethod.paymentDetails.expires" arguments="${paymentInfo.expiryMonth},${paymentInfo.expiryYear}"/><br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.line1)}<br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.town)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.region.isocodeShort)}<br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.postalCode)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.country.isocode)}<br/>
                                                <button type="submit" class="btn btn-primary btn-block" tabindex="${(status.count * 2) - 1}"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useThesePaymentDetails"/></button>

                                                <button type="button"
                                class="btn btn-primary btn-block submit_silentOrderPostForm checkout-next">
                            <spring:theme code="checkout.multi.paymentMethod.continue"/>
                        </button>
                                        </form>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>

                    </ycommerce:testId>
               </jsp:body>

            </multiCheckout:checkoutSteps>
        </div>

        <div class="col-sm-6 hidden-xs">
            <multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
        </div>

        <div class="col-sm-12 col-lg-12">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}"/>
            </cms:pageSlot>
        </div>
    </div>

</template:page>


