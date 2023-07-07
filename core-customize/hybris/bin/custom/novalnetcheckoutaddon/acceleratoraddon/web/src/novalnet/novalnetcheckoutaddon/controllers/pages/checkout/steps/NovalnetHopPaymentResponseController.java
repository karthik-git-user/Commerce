/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import de.hybris.platform.acceleratorfacades.payment.data.PaymentSubscriptionResultData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.yacceleratorstorefront.controllers.ControllerConstants;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.order.InvalidCartException;

import de.hybris.platform.store.BaseStoreModel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.UnsupportedEncodingException;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

import de.novalnet.order.NovalnetOrderFacade;


@Controller
@RequestMapping(value = "/checkout/multi/novalnet")
public class NovalnetHopPaymentResponseController extends NovalnetPaymentMethodCheckoutStepController
{
	private static final Logger LOGGER = Logger.getLogger(NovalnetHopPaymentResponseController.class);
	
	protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/multi/novalnet/order/confirmation/";
	
	@Resource(name = "novalnetOrderFacade")
    NovalnetOrderFacade novalnetOrderFacade;

	@RequestMapping(value = "/hop-response", method = RequestMethod.GET)
	@RequireHardLogIn
	public String doHandleHopResponse(final HttpServletRequest request) throws CMSItemNotFoundException, // NOSONAR
    InvalidCartException, CommerceCartModificationException
	{
		final Map<String, String> resultMap = getRequestParameterMap(request);
		String errorMessage = "While redirecting some data has been changed. The hash check failed";

		if ("SUCCESS".equals(resultMap.get("status").toString())) 
		{
            String transactionSecret = getSessionService().getAttribute("txn_secret");
            
			if (! "".equals(resultMap.get("checksum").toString()) && ! "".equals(resultMap.get("tid").toString()) && ! "".equals(transactionSecret) && ! "".equals(resultMap.get("status").toString())) 
			{
				final BaseStoreModel baseStore = this.getBaseStoreModel();
				String password = baseStore.getNovalnetPaymentAccessKey().trim();
				
				String token_string = resultMap.get("tid").toString() + transactionSecret + resultMap.get("status").toString() + new StringBuilder( password ).reverse().toString();
 
				String generatedChecksum = generateChecksum(token_string);

				if (generatedChecksum.equals(resultMap.get("checksum").toString())) 
			    {
					try {
						return processTransaction(resultMap);
					} catch(RuntimeException ex) {
						LOGGER.error("RuntimeException" + ex);
						getSessionService().setAttribute("novalnetCheckoutError", "checkout.placeOrder.failed");
						return getCheckoutStep().currentStep();
						
					} catch(InvalidCartException ex) {
						LOGGER.error("InvalidCartException" + ex);
						getSessionService().setAttribute("novalnetCheckoutError", "checkout.placeOrder.failed");
						return getCheckoutStep().currentStep();
					}
			    }
			}
			
		} else {
			errorMessage = resultMap.get("status_text").toString();
		}
		
		getSessionService().setAttribute("novalnetCheckoutError", errorMessage );
		return getCheckoutStep().currentStep();
	}
	
	protected String generateChecksum(String TOKEN_STRING) {
		String checksum = ""; 
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(TOKEN_STRING.getBytes("UTF-8"));
			StringBuilder hexString = new StringBuilder();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) { 
					hexString.append('0');
				}
				hexString.append(hex);
			}
			
			checksum =  hexString.toString();
		} catch(RuntimeException ex) {
			LOGGER.error("RuntimeException" + ex);
		} catch(NoSuchAlgorithmException ex) {
			LOGGER.error("UnsupportedEncodingException" + ex);
		} catch(UnsupportedEncodingException ex) {
			LOGGER.error("NoSuchAlgorithmException" + ex);
		}
		return checksum;
    }
    
    public String processTransaction(Map<String, String> resultMap) throws CMSItemNotFoundException, // NOSONAR
    InvalidCartException, CommerceCartModificationException {

		final Map<String, Object> transactionParameters = new HashMap<String, Object>();
        final Map<String, Object> dataParameters = new HashMap<String, Object>();
        final Map<String, Object> customParameters = new HashMap<String, Object>();
                
        String[] successStatus = {"CONFIRMED", "ON_HOLD", "PENDING"};
        
		transactionParameters.put("tid", resultMap.get("tid"));
		customParameters.put("lang", "DE");

		dataParameters.put("transaction", transactionParameters);
		dataParameters.put("custom", customParameters);

		Gson gson = new GsonBuilder().create();
		String jsonString = gson.toJson(dataParameters);

		String url = "https://payport.novalnet.de/v2/transaction/details";
		StringBuilder response = novalnetOrderFacade.sendRequest(url, jsonString);
		
		JSONObject tomJsonObject = new JSONObject(response.toString());
		JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
		JSONObject transactionJsonObject = tomJsonObject.getJSONObject("transaction");
				
		if (Arrays.asList(successStatus).contains(transactionJsonObject.get("status").toString()) && resultJsonObject.get("status").toString().equals("SUCCESS")) {
			final OrderData orderData            = novalnetOrderFacade.placeOrder(response.toString());
			return confirmationPageURL(orderData);
		} else {
			// Unset the stored novalnet session
			getSessionService().setAttribute("novalnetOrderCurrency", null);
			getSessionService().setAttribute("novalnetOrderAmount", null);
			getSessionService().setAttribute("novalnetCustomerParams", null);
			getSessionService().setAttribute("novalnetRedirectPaymentTestModeValue", null);
			getSessionService().setAttribute("novalnetRedirectPaymentName", null);
			getSessionService().setAttribute("novalnetCreditCardPanHash", null);
			getSessionService().setAttribute("paymentAccessKey", null);
				
			final String statusMessage = resultJsonObject.get("status_text").toString() != null ? resultJsonObject.get("status_text").toString() : resultMap.get("status_desc").toString();
			getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
			return getCheckoutStep().currentStep();
		}
	}
	
	/*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.AbstractCheckoutController#redirectToOrderConfirmationPage
     * (javax.servlet.http.HttpServletRequest)
     */
    protected String confirmationPageURL(final OrderData orderData) {
        return REDIRECT_URL_ORDER_CONFIRMATION
                + (getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode());
    }

}
