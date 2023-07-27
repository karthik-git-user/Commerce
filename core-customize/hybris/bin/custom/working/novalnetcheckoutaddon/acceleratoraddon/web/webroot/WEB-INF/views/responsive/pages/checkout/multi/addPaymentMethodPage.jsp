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
<script src="https://cdn.novalnet.de/js/pv13/checkout.js"></script>
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
                        <div class="novalnet-checkout-paymentmethod">
                            <div class="checkout-indent">

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
                                        </div>
									
										<c:if test="${novalnetBaseStoreConfiguration.novalnetTariffId != null && novalnetBaseStoreConfiguration.novalnetPaymentAccessKey != null && novalnetBaseStoreConfiguration.novalnetDisplayPayments == true && novalnetPayment.active == true }">
											<div class="paymentMethods">
												<h1 class="headline"> <spring:theme code="checkout.summary.select.payment.method"/> </h1>
												<iframe id = "paymentFormIframe" src= "${iframeUrl}" style = "width:100%;margin-bottom:1rem;border:none;"></iframe>
												<input type = "hidden" id = "nnPaymentResponse" name = "nnPaymentResponse" >
											</div>
										</c:if>
									   
										<p><spring:theme code="checkout.multi.paymentMethod.seeOrderSummaryForMoreInformation"/></p>
                                        
                                        </form:form>
                                       
                                    </ycommerce:testId>
                            </div>
                        </div>
                        
						<button type="button" class="btn btn-primary btn-block submit_novalnetPaymentDetailsForm checkout-next" id = "submit_novalnetPaymentDetailsForm">
							<spring:theme code="checkout.multi.paymentMethod.continue"/>
						</button>
                        

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


