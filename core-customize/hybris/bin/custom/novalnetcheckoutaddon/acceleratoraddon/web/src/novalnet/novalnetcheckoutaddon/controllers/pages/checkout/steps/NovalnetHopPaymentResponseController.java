/*
 *
 * @author    Novalnet AG
 * @copyright Copyright by Novalnet
 * @license   https://www.novalnet.de/payment-plugins/kostenlos/lizenz
 *
 * If you have found this script useful a small
 * recommendation as well as a comment on merchant form
 * would be greatly appreciated.
 *
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import java.io.*;

import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.xml.sax.SAXException;

import java.net.MalformedURLException;

import java.nio.charset.StandardCharsets;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.json.JSONObject;

import java.io.StringReader;
import java.util.Date;
import java.security.MessageDigest;
import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.order.InvalidCartException;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping(value = "/")
public class NovalnetHopPaymentResponseController extends NovalnetPaymentMethodCheckoutStepController {

    private static final Logger LOGGER = Logger.getLogger(NovalnetHopPaymentResponseController.class);

    @Resource
    private PaymentModeService paymentModeService;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    @Resource(name = "cartService")
    private CartService cartService;

    @Resource
    private Converter<AddressData, AddressModel> addressReverseConverter;

    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/novalnet/orderConfirmation/";

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.checkout.steps.HopPaymentResponseController#doHandleHopResponse
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "checkout/multi/novalnet/hop-response", method = RequestMethod.POST)
    @RequireHardLogIn
    public String doHandleHopResponse(final HttpServletRequest request) throws // NOSONAR
    InvalidCartException {
        final OrderData orderData;
        try {
            orderData = getCheckoutFacade().placeOrder();
        } catch (final InvalidCartException e) {
            return getCheckoutStep().currentStep();
        }

        return confirmationPageURL(orderData);

    }

    @RequestMapping(value = "checkout/multi/novalnet/hop-response", method = RequestMethod.GET)
    @RequireHardLogIn
    public String handleHopResponse(final RedirectAttributes redirectAttributes, final HttpServletRequest request) {
		
        final Map<String, String> resultMap = getRequestParameterMap(request);
        
        String transactionSecret = getSessionService().getAttribute("txn_secret");
                
        if (! "".equals(resultMap.get("checksum").toString()) && ! "".equals(resultMap.get("tid").toString()) && ! "".equals(transactionSecret) && ! "".equals(resultMap.get("status").toString())) 
		{
		   String token_string = resultMap.get("tid").toString() + transactionSecret + resultMap.get("status").toString() + new StringBuilder( getSessionService().getAttribute("txn_check") ).reverse().toString();
 
		   String generatedChecksum = generateChecksum(token_string);

		   if (!generatedChecksum.equals(resultMap.get("checksum").toString())) 
		   {
			  final String statusMessage = "While redirecting some data has been changed. The hash check failed";
			  getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
			  return getCheckoutStep().currentStep();
		   } 
		   else
		   {
			    return processTransaction(resultMap);
		   }            
		} 
		else
		{
			  final String statusMessage = "While redirecting some data has been changed. The hash check failed";
			  getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
			  return getCheckoutStep().currentStep();
		}

    }
    
    public String processTransaction(Map<String, String> resultMap) {

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
		StringBuilder response = novalnetFacade.sendRequest(url, jsonString);

		JSONObject tomJsonObject = new JSONObject(response.toString());
		JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
		JSONObject customerJsonObject = tomJsonObject.getJSONObject("customer");
		JSONObject transactionJsonObject = tomJsonObject.getJSONObject("transaction");
		String currentPayment = getSessionService().getAttribute("selectedPaymentMethodId");
			
		if (Arrays.asList(successStatus).contains(transactionJsonObject.get("status").toString())) {

			String paymentName = novalnetFacade.getPaymentName(currentPayment);
            String orderComments = Localization.getLocalizedString("novalnet.paymentname") + ": " + paymentName + "<br>";

			orderComments += "Novalnet transaction id : " + transactionJsonObject.get("tid");
			AddressData addressData = getSessionService().getAttribute("novalnetAddressData");

			int orderAmountCent = Integer.parseInt(transactionJsonObject.get("amount").toString());

			final OrderData orderData;
			
			String bankDetails = "";
			
			String transactionEmail = customerJsonObject.getString("email");
			
			try
			{
				orderData = novalnetFacade.saveOrderData(orderComments, currentPayment, transactionJsonObject.get("status").toString(), orderAmountCent, transactionJsonObject.getString("currency"), transactionJsonObject.get("tid").toString(), transactionEmail, addressData, bankDetails);
			}
			catch (final InvalidCartException e)
			{
				getSessionService().setAttribute("novalnetCheckoutError", Localization.getLocalizedString("novalnet.unknown.error") );
				return getCheckoutStep().currentStep();
			}

			transactionParameters.clear();
			dataParameters.clear();

			transactionParameters.put("tid", resultMap.get("tid"));
			transactionParameters.put("order_no", orderData.getCode());

			dataParameters.put("transaction", transactionParameters);
			dataParameters.put("custom", customParameters);

			jsonString = gson.toJson(dataParameters);

			url = "https://payport.novalnet.de/v2/transaction/update";
			StringBuilder responseString = novalnetFacade.sendRequest(url, jsonString);
			
			handleStorePayment(currentPayment, response, customerJsonObject);
			
			getSessionService().setAttribute("tid", orderComments);
			getSessionService().setAttribute("email", transactionEmail);

			return confirmationPageURL(orderData);
		}
		else
		{
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
	
	public void handleStorePayment(String currentPayment, StringBuilder response, JSONObject customerJsonObject) {
			
		if ("novalnetPayPal".equals(currentPayment)) {

			boolean novalnetPayPalStorePaymentData = getSessionService().getAttribute("novalnetPayPalStorePaymentData");

			if (novalnetPayPalStorePaymentData == true) {
				novalnetFacade.handleReferenceTransactionInfo(response, customerJsonObject.get("customer_no").toString(), "novalnetPayPal");
			}
		} else if ("novalnetCreditCard".equals(currentPayment) && !novalnetFacade.isGuestUser()) {
			boolean novalnetCreditCardStorePaymentData = getSessionService().getAttribute("novalnetCreditCardStorePaymentData");
			
			if(novalnetCreditCardStorePaymentData == true) {
				novalnetFacade.handleReferenceTransactionInfo(response, customerJsonObject.get("customer_no").toString(), "novalnetCreditCard");
			}
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

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.addons.novalnetcheckoutaddon.controllers.pages.NovalnetCallbackHandler#beforeController
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "novalnet/callback", method = RequestMethod.POST)
    public void doHandle(final HttpServletRequest request) {
    }// Handle callback request in handler adaptee
}
