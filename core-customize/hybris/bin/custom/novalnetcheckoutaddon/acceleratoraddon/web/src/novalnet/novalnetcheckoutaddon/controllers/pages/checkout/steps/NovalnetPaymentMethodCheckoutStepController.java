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

import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorservices.payment.constants.PaymentConstants;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.enums.CountryType;
import de.hybris.platform.yacceleratorstorefront.controllers.ControllerConstants;
import novalnet.novalnetcheckoutaddon.forms.NovalnetPaymentDetailsForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.util.localization.Localization;

import java.util.Locale;

import org.json.JSONObject;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import novalnet.novalnetcheckoutaddon.forms.NovalnetPaymentInfoData;
import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentRefInfoModel;
import de.novalnet.order.NovalnetOrderFacade;

import de.hybris.platform.order.PaymentModeService;

import java.text.ParseException;
import java.text.NumberFormat;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.math.BigDecimal;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/checkout/multi/novalnet/select-payment-method")
public class NovalnetPaymentMethodCheckoutStepController extends AbstractCheckoutStepController {
    
    private static final Logger LOG = Logger.getLogger(NovalnetPaymentMethodCheckoutStepController.class);

    private static final String PAYMENT_METHOD = "payment-method";
    private static final String BILLING_COUNTRIES = "billingCountries";
    private static final String CART_DATA_ATTR = "cartData";

    public static final int CONVERT_TO_CENT = 100;

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;
    
    @Resource(name = "novalnetOrderFacade")
    NovalnetOrderFacade novalnetOrderFacade;

    @Resource(name = "addressDataUtil")
    private AddressDataUtil addressDataUtil;
    
    @Resource
    private PaymentModeService paymentModeService;
    
    public BaseStoreModel getBaseStoreModel() {
        return getBaseStoreService().getCurrentBaseStore();
    }

    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }
    
    @Override
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        String page = addPaymentProcess(model);
        return page;
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequireHardLogIn
    public String add(final Model model, @Valid final NovalnetPaymentDetailsForm paymentDetailsForm, final BindingResult bindingResult)
    throws CMSItemNotFoundException {

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        AddressData addressData;
        AddressData deliveryAddressData;
        
        Integer skipSummaryPage = 0;
        
        final Map<String, Object> customerParameters = new HashMap<String, Object>();
        final Map<String, String> billingParameters  = new HashMap<String, String>();
        final Map<String, String> shippingParameters = new HashMap<String, String>();
        String firstName, lastname;
        String paymentDetails      = paymentDetailsForm.getNnPaymentResponse().trim();
        
        try {
        
			JSONObject paymentDataJson = new JSONObject(paymentDetails);
			JSONObject resultJson = paymentDataJson.getJSONObject("result");
			
			if(!resultJson.get("status").toString().equals("SUCCESS")) {
				GlobalMessages.addErrorMessage(model, resultJson.get("message").toString());
				return addPaymentProcess(model);
			}
			
			if (paymentDataJson.has("booking_details")) {
				
				JSONObject bookingDetails = paymentDataJson.getJSONObject("booking_details");
				if(bookingDetails.has("birth_date")) {
					customerParameters.put("birth_date", bookingDetails.get("birth_date").toString());
				}
				
				if(bookingDetails.has("wallet_token")) {
					skipSummaryPage = 1;
				}
			}
		
		} catch(Exception e) {
			GlobalMessages.addErrorMessage(model, "Payment details are not valid. Please try again");
			return addPaymentProcess(model);
		}
		
		System.out.println("isUseDeliveryAddress " + Boolean.TRUE.equals(paymentDetailsForm.isUseDeliveryAddress()));
		System.out.println("isUseDeliveryAddress 1" + paymentDetailsForm.isUseDeliveryAddress());

		if (Boolean.TRUE.equals(paymentDetailsForm.isUseDeliveryAddress()))  {
			addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
			
			System.out.println("inside if");
			
			if (addressData == null)
			{
				GlobalMessages.addErrorMessage(model,
						"checkout.multi.paymentMethod.createSubscription.billingAddress.noneSelectedMsg");
				return addPaymentProcess(model);
			}
			
			shippingParameters.put("same_as_billing", "1");
			
			billingParameters.put("street", addressData.getLine1().toString() + " " + addressData.getLine2().toString());
			billingParameters.put("city", addressData.getTown().toString());
			billingParameters.put("zip", addressData.getPostalCode().toString());
			billingParameters.put("country_code", addressData.getCountry().getIsocode().toString());
			
			customerParameters.put("first_name", addressData.getFirstName());
			customerParameters.put("last_name", addressData.getLastName());
			
			
		} else {
			
			System.out.println("inside else");

			deliveryAddressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
			
			customerParameters.put("first_name", paymentDetailsForm.getBillTo_firstName());
			customerParameters.put("last_name", paymentDetailsForm.getBillTo_lastName());
                
			billingParameters.put("street", paymentDetailsForm.getBillTo_street1().toString() + paymentDetailsForm.getBillTo_street2().toString());
			billingParameters.put("city", paymentDetailsForm.getBillTo_city().toString());
			billingParameters.put("zip", paymentDetailsForm.getBillTo_postalCode().toString());
			billingParameters.put("country_code", paymentDetailsForm.getBillTo_country().toString());
			
			
			shippingParameters.put("street", deliveryAddressData.getLine1().toString() + " " + deliveryAddressData.getLine2().toString());
			shippingParameters.put("city", deliveryAddressData.getTown().toString());
			shippingParameters.put("zip", deliveryAddressData.getPostalCode().toString());
			shippingParameters.put("country_code", deliveryAddressData.getCountry().getIsocode().toString());
		}
		
		
		
		String guestEmail         = novalnetOrderFacade.getGuestEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
		
		
        customerParameters.put("email", emailAddress);
        
     		
		LOG.info("Payment Deatils Selected by customer : " + paymentDetails);
		
		customerParameters.put("billing", billingParameters);
        customerParameters.put("shipping", shippingParameters);

		
		getSessionService().setAttribute("isUseDeliveryAddress", paymentDetailsForm.isUseDeliveryAddress());
		getSessionService().setAttribute("novalnetPaymentResponse", paymentDetails);
		getSessionService().setAttribute("novalnetAddressData", customerParameters);
		
		getSessionService().setAttribute("REDIRECT_PREFIX", REDIRECT_PREFIX);
		
		if(skipSummaryPage == 1) {			
			String result =  novalnetOrderFacade.processPayment(Optional.empty(), model);
			
			if(result.equals("payment_error") || result.equals("order_error")) {
				GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");
				return getCheckoutStep().currentStep();
			} else {
				return result;
			}
		}
		
        return getCheckoutStep().nextStep();
    }
    
    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException {
        model.addAttribute("metaRobots", "noindex,nofollow");
        model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.breadcrumb"));
        final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }
    
    /**
     * add payment process
     *
     * @param model
     * @return object
     */
    public String addPaymentProcess(Model model) throws CMSItemNotFoundException {
		
		String errorMessage = getSessionService().getAttribute("novalnetCheckoutError");
        if (errorMessage != null) {
            GlobalMessages.addErrorMessage(model, errorMessage);
        }
        getSessionService().setAttribute("novalnetCheckoutError", null);
		
        setupAddPaymentPage(model);
        final NovalnetPaymentDetailsForm paymentDetailsForm = new NovalnetPaymentDetailsForm();
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        
        AddressForm addressForm = getAddressForm(cartData, model);
        paymentDetailsForm.setBillingAddress(addressForm);
        model.addAttribute("commonPaymentDetailsForm", new NovalnetPaymentDetailsForm());
        model.addAttribute(CART_DATA_ATTR, cartData);
        
		model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
		
        model.addAttribute(BILLING_COUNTRIES, getCheckoutFacade().getBillingCountries());
        model.addAttribute("paymentDetailsForm", paymentDetailsForm);
        model.addAttribute("showPayment", getSessionService().getAttribute("showPayment"));
        
       
		model.addAttribute("iframeUrl", getPaymentUrl());
		model.addAttribute("novalnetPayment", paymentModeService.getPaymentModeForCode("novalnet"));
		final BaseStoreModel baseStore = this.getBaseStoreModel();
		model.addAttribute("novalnetBaseStoreConfiguration", baseStore);
		
        
        return NovalnetcheckoutaddonControllerConstants.AddPaymentMethodPage;
    }
    
    public String getPaymentUrl() {
        
        final CartData cartData   = getCheckoutFacade().getCheckoutCart();
        Integer orderAmountCent   = novalnetOrderFacade.getOrderAmount(cartData);
        final String currency     = cartData.getTotalPriceWithTax().getCurrencyIso();
        final Locale language     = JaloSession.getCurrentSession().getSessionContext().getLocale();
        final String languageCode = language.toString().toUpperCase();
        
        String guestEmail         = novalnetOrderFacade.getGuestEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
        
        final Map<String, Object> customerParameters = new HashMap<String, Object>();
        final Map<String, Object> billingParameters  = new HashMap<String, Object>();
        final Map<String, Object> shippingParameters = new HashMap<String, Object>();
        
         AddressData addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
        
        customerParameters.put("first_name", addressData.getFirstName());
        customerParameters.put("last_name", addressData.getLastName());
        customerParameters.put("email", emailAddress);
        
       
        
        billingParameters.put("street", addressData.getLine1() + " " + addressData.getLine2());
		billingParameters.put("city", addressData.getTown());
		billingParameters.put("zip", addressData.getPostalCode());
		billingParameters.put("country_code", addressData.getCountry().getIsocode());
        
        shippingParameters.put("same_as_billing", 1);
        
        customerParameters.put("billing", billingParameters);
        customerParameters.put("shipping", shippingParameters);

		getSessionService().setAttribute("novalnetAddressData", customerParameters);
        
        
        String requsetDeatils     = novalnetOrderFacade.formPaymentRequest(cartData, emailAddress, orderAmountCent, currency, languageCode, "paymentForm", Optional.empty());
        StringBuilder response    = novalnetOrderFacade.sendRequest( "https://payport.novalnet.de/v2/seamless/payment", requsetDeatils.toString());

        JSONObject tomJsonObject = new JSONObject(response.toString());
        JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
        return resultJsonObject.get("redirect_url").toString();
    }
    

    private AddressForm getAddressForm(CartData cartData, Model model) {
        AddressForm addressForm = new AddressForm();
        if (existBillingAddressInCartData(cartData)) {
            addressForm = populateAddressForm(cartData.getPaymentInfo().getBillingAddress());
        } else if (cartData.getDeliveryAddress() != null) {
            addressForm = populateAddressForm(cartData.getDeliveryAddress());
        }

        if (StringUtils.isNotBlank(addressForm.getCountryIso())) {
            model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressForm.getCountryIso()));
            model.addAttribute("country", addressForm.getCountryIso());
        }
        return addressForm;
    }

    private AddressForm populateAddressForm(final AddressData addressData) {
        final AddressForm addressForm = new AddressForm();
        addressForm.setAddressId(addressData.getId());
        addressForm.setFirstName(addressData.getFirstName());
        addressForm.setLastName(addressData.getLastName());
        addressForm.setLine1(addressData.getLine1());
        if (addressData.getLine2() != null) {
            addressForm.setLine2(addressData.getLine2());
        }
        addressForm.setTownCity(addressData.getTown());
        addressForm.setPostcode(addressData.getPostalCode());
        addressForm.setCountryIso(addressData.getCountry().getIsocode());
        if (addressData.getRegion() != null) {
            addressForm.setRegionIso(addressData.getRegion().getIsocode());
        }
        addressForm.setShippingAddress(addressData.isShippingAddress());
        addressForm.setBillingAddress(addressData.isBillingAddress());
        if (addressData.getPhone() != null) {
            addressForm.setPhone(addressData.getPhone());
        }
        return addressForm;

    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    private boolean existBillingAddressInCartData(final CartData cartData) {
        return cartData.getPaymentInfo() != null && cartData.getPaymentInfo().getBillingAddress() != null;
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(PAYMENT_METHOD);
    }

}
