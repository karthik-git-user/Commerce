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
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PlaceOrderForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.yacceleratorstorefront.controllers.ControllerConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.store.services.BaseStoreService;

import java.text.DecimalFormat;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.json.JSONObject;

import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import de.hybris.platform.store.BaseStoreModel;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Locale;
import java.text.NumberFormat;
import javax.annotation.Resource;
import java.io.*;

import java.util.Arrays;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.novalnet.core.model.NovalnetDirectDebitSepaPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetGuaranteedDirectDebitSepaPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPayPalPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetCreditCardPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetInvoicePaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetGuaranteedInvoicePaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPrepaymentPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetBarzahlenPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetInstantBankTransferPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetOnlineBankTransferPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetBancontactPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetMultibancoPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPostFinanceCardPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPostFinancePaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetIdealPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetEpsPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetGiropayPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPrzelewy24PaymentModeModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.order.CartService;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.core.model.user.AddressModel;

import java.util.Base64;

@Controller
@RequestMapping(value = "/checkout/multi/novalnet/summary")
public class NovalnetSummaryCheckoutStepController extends AbstractCheckoutStepController {

    private static final Logger LOGGER = Logger.getLogger(NovalnetSummaryCheckoutStepController.class);

    private static final String SUMMARY = "summary";
    
    private static final String PAYMENT_AUTHORIZE = "AUTHORIZE";
    
    public static final int CONVERT_TO_CENT_OR_SUCCESS_STATUS = 100;
    public static final int PREPAYMENT_FROM_DATE = 7;
    public static final int PREPAYMENT_TILL_DATE = 28;


    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/novalnet/orderConfirmation/";

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    @Resource(name = "cartService")
    private CartService cartService;

    @Resource
    private Converter<AddressData, AddressModel> addressReverseConverter;

    @Resource
    private PaymentModeService paymentModeService;

    @RequestMapping(value = "/enter", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException, // NOSONAR
            CommerceCartModificationException {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        if (cartData.getEntries() != null && !cartData.getEntries().isEmpty()) {
            for (final OrderEntryData entry : cartData.getEntries()) {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = getProductFacade().getProductForCodeAndOptions(productCode, Arrays.asList(
                        ProductOption.BASIC, ProductOption.PRICE, ProductOption.VARIANT_MATRIX_BASE, ProductOption.PRICE_RANGE));
                entry.setProduct(product);
            }
        }

        model.addAttribute("cartData", cartData);
        model.addAttribute("allItems", cartData.getEntries());
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("deliveryMode", cartData.getDeliveryMode());
        model.addAttribute("paymentInfo", cartData.getPaymentInfo());

        // Only request the security code if the SubscriptionPciOption is set to Default.
        final boolean requestSecurityCode = CheckoutPciOptionEnum.DEFAULT
                .equals(getCheckoutFlowFacade().getSubscriptionPciOption());
        model.addAttribute("requestSecurityCode", Boolean.valueOf(requestSecurityCode));

        model.addAttribute(new PlaceOrderForm());

        final ContentPageModel multiCheckoutSummaryPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, multiCheckoutSummaryPage);
        setUpMetaDataForContentPage(model, multiCheckoutSummaryPage);

        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.summary.breadcrumb"));
        model.addAttribute("metaRobots", "noindex,nofollow");
        setCheckoutStepLinksForModel(model, getCheckoutStep());

        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        String totalAmount = formatAmount(String.valueOf(cartData.getTotalPriceWithTax().getValue()));
        String orderAmount = decimalFormat.format(Float.parseFloat(totalAmount));
        getSessionService().setAttribute("novalnetOrderAmount", orderAmount);

        return NovalnetcheckoutaddonControllerConstants.CheckoutSummaryPage;
    }

    public static String formatAmount(String amount) {
        if (amount.contains(",")) {
            try {
                NumberFormat formattedAmount = NumberFormat.getNumberInstance(Locale.GERMANY);
                double formattedValue = formattedAmount.parse(amount).doubleValue();
                amount = Double.toString(formattedValue);
            } catch (Exception e) {
                amount = amount.replace(",", ".");
            }
        }
        return amount;
    }


    @RequestMapping(value = "/placeOrder")
    @PreValidateQuoteCheckoutStep
    @RequireHardLogIn
    public String placeOrder(@ModelAttribute("placeOrderForm") final PlaceOrderForm placeOrderForm, final Model model,
                             final HttpServletRequest request, final RedirectAttributes redirectModel) throws CMSItemNotFoundException, // NOSONAR
            InvalidCartException, CommerceCartModificationException {
        final Map<String, Object> transactionParameters = new HashMap<String, Object>();
        final Map<String, Object> merchantParameters = new HashMap<String, Object>();
        final Map<String, Object> customerParameters = new HashMap<String, Object>();
        final Map<String, Object> billingParameters = new HashMap<String, Object>();
        final Map<String, Object> shippingParameters = new HashMap<String, Object>();
        final Map<String, Object> customParameters = new HashMap<String, Object>();
        final Map<String, Object> paymentParameters = new HashMap<String, Object>();
        final Map<String, Object> dataParameters = new HashMap<String, Object>();

        final BaseStoreModel baseStore = this.getBaseStoreModel();

        final Integer tariff = baseStore.getNovalnetTariffId();
        final String apiKey = baseStore.getNovalnetAPIKey();
        String token = "";

        final CartData cartData = getCheckoutFacade().getCheckoutCart();

        final String currency = cartData.getTotalPriceWithTax().getCurrencyIso();
        final Map<String, Object> customerParameter = (Map<String, Object>) getSessionService().getAttribute("novalnetCustomerParams");
        String customerNo = JaloSession.getCurrentSession().getUser().getPK().toString();
        String currentPayment = getSessionService().getAttribute("selectedPaymentMethodId");
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);

        String orderAmount = getSessionService().getAttribute("novalnetOrderAmount");
        float floatAmount = Float.parseFloat(orderAmount);
        BigDecimal orderAmountCents = BigDecimal.valueOf(floatAmount).multiply(BigDecimal.valueOf(CONVERT_TO_CENT_OR_SUCCESS_STATUS));
        Integer orderAmountCent = orderAmountCents.intValue();
        Integer testMode = 0;
        boolean redirect = false;


        merchantParameters.put("signature", apiKey);
        merchantParameters.put("tariff", tariff);

        customerParameters.put("first_name", customerParameter.get("first_name"));
        customerParameters.put("last_name", customerParameter.get("last_name"));
        customerParameters.put("email", customerParameter.get("email"));
        customerParameters.put("customer_ip", getRemoteIpAddr(request));
        customerParameters.put("customer_no", customerNo);
        customerParameters.put("gender", "u");


        billingParameters.put("street", customerParameter.get("street"));
        billingParameters.put("city", customerParameter.get("city"));
        billingParameters.put("zip", customerParameter.get("zip"));
        billingParameters.put("country_code", customerParameter.get("country"));

        String sameAsBilling = getSessionService().getAttribute("same_as_billing");
        if ("1".equals(sameAsBilling)) {
            shippingParameters.put("same_as_billing", sameAsBilling);
            getSessionService().setAttribute("same_as_billing", null);
        } else {
            shippingParameters.put("street", customerParameter.get("shipping_street"));
            shippingParameters.put("city", customerParameter.get("shipping_city"));
            shippingParameters.put("zip", customerParameter.get("shipping_zip"));
            shippingParameters.put("country_code", customerParameter.get("shipping_country"));
            shippingParameters.put("first_name", customerParameter.get("shipping_first_name"));
            shippingParameters.put("last_name", customerParameter.get("shipping_last_name"));
        }
        

        customerParameters.put("billing", billingParameters);
        customerParameters.put("shipping", shippingParameters);

        transactionParameters.put("payment_type", getPaymentType(currentPayment));
        transactionParameters.put("currency", currency);
        transactionParameters.put("amount", orderAmountCent);
        transactionParameters.put("system_name", "SAP Commerce Cloud");
        transactionParameters.put("system_version", "2105-NN1.1.0");
        
        boolean verify_payment_data = false;

        boolean oneClickShopping = false;
        // Get shop current language
        final Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();
        final String languageCode = language.toString().toUpperCase();
        customParameters.put("lang", languageCode);
		Integer onholdOrderAmount = 0;
		 
        if ("novalnetDirectDebitSepa".equals(currentPayment)) {

            NovalnetDirectDebitSepaPaymentModeModel novalnetPaymentMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentModeModel;

            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
            
            // Form sepa duedate
            Integer sepaDueDate = novalnetPaymentMethod.getNovalnetDueDate();
            if (sepaDueDate != null && sepaDueDate > 2 && sepaDueDate < 14) {
                transactionParameters.put("due_date", formatDate(sepaDueDate));
            }
           
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.error("onhold order amount is null");
			}
				
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                 verify_payment_data = true;
            }
            
            boolean novalnetDirectDebitSepaStorePaymentData = getSessionService().getAttribute("novalnetDirectDebitSepaStorePaymentData");
				
				if(getSessionService().getAttribute("novalnetDirectDebitSepatoken") != null) {
					token =  getSessionService().getAttribute("novalnetDirectDebitSepatoken");
				} else {
					LOGGER.info("novalnetDirectDebitSepatoken is null");
					token = "";
				}
				
             
				 if (Boolean.FALSE.equals(novalnetFacade.isGuestUser()) && Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping()) && Boolean.TRUE.equals(novalnetDirectDebitSepaStorePaymentData)) {
					transactionParameters.put("create_token", '1');
					oneClickShopping = true;
				}
				
				
				if (Boolean.FALSE.equals(novalnetFacade.isGuestUser()) && Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping()) && !"".equals(token)) {
					paymentParameters.put("token", token);
					getSessionService().setAttribute("novalnetDirectDebitSepatoken", null);
				}

                if("".equals(token)) {
					String accountHolder = customerParameter.get("first_name").toString() + ' ' + customerParameter.get("last_name").toString();
					paymentParameters.put("iban", getSessionService().getAttribute("novalnetDirectDebitSepaAccountIban").toString());
					paymentParameters.put("bank_account_holder", accountHolder.replace("&", ""));
					getSessionService().setAttribute("novalnetDirectDebitSepaAccountIban", null);
				}
            

        } else if ("novalnetGuaranteedDirectDebitSepa".equals(currentPayment)) {

            NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetPaymentMethod = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) paymentModeModel;

            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
            
            Integer guaranteedSepaDueDate = novalnetPaymentMethod.getNovalnetDueDate();
            if (guaranteedSepaDueDate != null && guaranteedSepaDueDate > 2 && guaranteedSepaDueDate < 14) {
                transactionParameters.put("due_date", formatDate(guaranteedSepaDueDate));
            }
            
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.error("onhold order amount is null");
			}
			
			boolean novalnetGuaranteedDirectDebitSepaStorePaymentData = getSessionService().getAttribute("novalnetGuaranteedDirectDebitSepaStorePaymentData");
				
				if(getSessionService().getAttribute("novalnetDirectDebitSepatoken") != null) {
					token =  getSessionService().getAttribute("novalnetDirectDebitSepatoken");
				} else {
					LOGGER.info("novalnetDirectDebitSepatoken is null");
					token = "";
				}
				
             
				 if (Boolean.FALSE.equals(novalnetFacade.isGuestUser()) && Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping()) && Boolean.TRUE.equals(novalnetGuaranteedDirectDebitSepaStorePaymentData)) {
					transactionParameters.put("create_token", '1');
					oneClickShopping = true;
				}
				
				
				if (Boolean.FALSE.equals(novalnetFacade.isGuestUser()) && Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping()) && !"".equals(token)) {
					paymentParameters.put("token", token);
					getSessionService().setAttribute("novalnetDirectDebitSepatoken", null);
				}

                if("".equals(token)) {
					String accountHolder = customerParameter.get("first_name").toString() + ' ' + customerParameter.get("last_name").toString();
					paymentParameters.put("iban", getSessionService().getAttribute("novalnetGuaranteedDirectDebitSepaAccountIban").toString());
					paymentParameters.put("bank_account_holder", accountHolder.replace("&", ""));
					getSessionService().setAttribute("novalnetGuaranteedDirectDebitSepaAccountIban", null);
				}
				
				String dob = getSessionService().getAttribute("novalnetGuaranteedDirectDebitSepaDateOfBirth");
				customerParameters.put("birth_date", dob);
            
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                  verify_payment_data = true;
            }
        } else if ("novalnetPayPal".equals(currentPayment)) {
            redirect = true;
            NovalnetPayPalPaymentModeModel novalnetPaymentMethod = (NovalnetPayPalPaymentModeModel) paymentModeModel;
            
            if(getSessionService().getAttribute("novalnetPayPaltoken") != null) {
				token =  getSessionService().getAttribute("novalnetPayPaltoken");
			} else {
				LOGGER.info("novalnetPayPaltoken is null");
				token = "";
			}
			
			boolean novalnetPayPalStorePaymentData = getSessionService().getAttribute("novalnetPayPalStorePaymentData");
                         
            if (!novalnetFacade.isGuestUser() && novalnetPaymentMethod.getNovalnetOneClickShopping() && Boolean.TRUE.equals(novalnetPayPalStorePaymentData)) {
                transactionParameters.put("create_token", '1');
                oneClickShopping = true;
            }
            
            
            if (!novalnetFacade.isGuestUser() && novalnetPaymentMethod.getNovalnetOneClickShopping() && !"".equals(token)) {
                paymentParameters.put("token", token);
                getSessionService().setAttribute("novalnetPayPaltoken", null);
            }
             
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.info("onhold order amount is null");
			}
			
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                 verify_payment_data = true;
            }

            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetCreditCard".equals(currentPayment)) {
            NovalnetCreditCardPaymentModeModel novalnetPaymentMethod = (NovalnetCreditCardPaymentModeModel) paymentModeModel;
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.info("onhold order amount is null");
			}
            
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                 verify_payment_data = true;
            }

            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
            
            if(getSessionService().getAttribute("novalnetCreditCardtoken") != null) {
				token =  getSessionService().getAttribute("novalnetCreditCardtoken");
			} else {
				LOGGER.info("novalnetCreditCardtoken is null");
				token = "";
			}
            
            if(getSessionService().getAttribute("novalnetCreditCardtoken") != null) {
				token =  getSessionService().getAttribute("novalnetCreditCardtoken");
			} else {
				LOGGER.info("onhold order amount is null");
				 onholdOrderAmount = 0;
			}
			
			boolean novalnetCreditCardStorePaymentData = getSessionService().getAttribute("novalnetCreditCardStorePaymentData");

            if (!novalnetFacade.isGuestUser() && novalnetPaymentMethod.getNovalnetOneClickShopping() && Boolean.TRUE.equals(novalnetCreditCardStorePaymentData)) {
                transactionParameters.put("create_token", '1');
                oneClickShopping = true;
            }

            String referenceTid = getSessionService().getAttribute("novalnetCreditCardReferenceTid");
            if (!novalnetFacade.isGuestUser() && novalnetPaymentMethod.getNovalnetOneClickShopping() && !"".equals(token)) {
                 paymentParameters.put("token", token);
                getSessionService().setAttribute("novalnetCreditCardtoken", null);
            } else {

                paymentParameters.put("pan_hash", getSessionService().getAttribute("novalnetCreditCardPanHash").toString());
                paymentParameters.put("unique_id", getSessionService().getAttribute("novalnetCreditCardUniqueId").toString());
                String do_redirect = getSessionService().getAttribute("do_redirect").toString();
                
                if(!"".equals(do_redirect)) {
					 redirect = true;
				}
                
                getSessionService().setAttribute("novalnetCreditCardPanHash", null);

            }
        } else if ("novalnetInvoice".equals(currentPayment)) {
            NovalnetInvoicePaymentModeModel novalnetPaymentMethod = (NovalnetInvoicePaymentModeModel) paymentModeModel;
            transactionParameters.put("invoice_type", "INVOICE");

            // Form invoice duedate
            Integer invoiceDueDate = novalnetPaymentMethod.getNovalnetDueDate();
            if (invoiceDueDate != null && invoiceDueDate > 7) {
                transactionParameters.put("due_date", formatDate(invoiceDueDate));
            }
            
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.info("onhold order amount is null");
			}
			
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                  verify_payment_data = true;
            }


            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetPrepayment".equals(currentPayment)) {
			NovalnetPrepaymentPaymentModeModel novalnetPaymentMethod = (NovalnetPrepaymentPaymentModeModel) paymentModeModel;
			Integer prepaymentDueDate = novalnetPaymentMethod.getNovalnetDueDate();
            if (prepaymentDueDate != null && PREPAYMENT_FROM_DATE >= 7 && prepaymentDueDate <= PREPAYMENT_TILL_DATE) {
                transactionParameters.put("due_date", formatDate(prepaymentDueDate));
            }
            transactionParameters.put("invoice_type", "PREPAYMENT");

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetMultibanco".equals(currentPayment)) {
			NovalnetMultibancoPaymentModeModel novalnetPaymentMethod = (NovalnetMultibancoPaymentModeModel) paymentModeModel;
			if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetGuaranteedInvoice".equals(currentPayment)) {
            NovalnetGuaranteedInvoicePaymentModeModel novalnetPaymentMethod = (NovalnetGuaranteedInvoicePaymentModeModel) paymentModeModel;
            
            if(novalnetPaymentMethod != null) {
				onholdOrderAmount = novalnetPaymentMethod.getNovalnetOnholdAmount();
				if (onholdOrderAmount == null) { 
					 onholdOrderAmount = 0;
				}
			} else {
				LOGGER.info("onhold order amount is null");
			}
            
            if (PAYMENT_AUTHORIZE.equals(novalnetPaymentMethod.getNovalnetOnholdAction().toString()) && orderAmountCent >= onholdOrderAmount) {
                 verify_payment_data = true;
            }
            
            String dob = getSessionService().getAttribute("novalnetGuaranteedInvoiceDateOfBirth");
            customerParameters.put("birth_date", dob);

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetBarzahlen".equals(currentPayment)) {
            NovalnetBarzahlenPaymentModeModel novalnetPaymentMethod = (NovalnetBarzahlenPaymentModeModel) paymentModeModel;
            // Form Barzahlen slip expiry date
            Integer slipExpiryDate = novalnetPaymentMethod.getNovalnetBarzahlenslipExpiryDate();
            if (slipExpiryDate != null) {
                transactionParameters.put("due_date", formatDate(slipExpiryDate));
            }
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetInstantBankTransfer".equals(currentPayment)) {
            NovalnetInstantBankTransferPaymentModeModel novalnetPaymentMethod = (NovalnetInstantBankTransferPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetOnlineBankTransfer".equals(currentPayment)) {
            NovalnetOnlineBankTransferPaymentModeModel novalnetPaymentMethod = (NovalnetOnlineBankTransferPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetBancontact".equals(currentPayment)) {
            NovalnetBancontactPaymentModeModel novalnetPaymentMethod = (NovalnetBancontactPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetIdeal".equals(currentPayment)) {
            NovalnetIdealPaymentModeModel novalnetPaymentMethod = (NovalnetIdealPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetEps".equals(currentPayment)) {
            NovalnetEpsPaymentModeModel novalnetPaymentMethod = (NovalnetEpsPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }

        } else if ("novalnetGiropay".equals(currentPayment)) {
            NovalnetGiropayPaymentModeModel novalnetPaymentMethod = (NovalnetGiropayPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetPostFinance".equals(currentPayment)) {
            NovalnetPostFinancePaymentModeModel novalnetPaymentMethod = (NovalnetPostFinancePaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetPostFinanceCard".equals(currentPayment)) {
            NovalnetPostFinanceCardPaymentModeModel novalnetPaymentMethod = (NovalnetPostFinanceCardPaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        } else if ("novalnetPrzelewy24".equals(currentPayment)) {
            NovalnetPrzelewy24PaymentModeModel novalnetPaymentMethod = (NovalnetPrzelewy24PaymentModeModel) paymentModeModel;

            // Redirect Flag
            redirect = true;

            // Check for test mode
            if (novalnetPaymentMethod.getNovalnetTestMode()) {
                testMode = 1;
            }
        }

        transactionParameters.put("test_mode", testMode);

        if (redirect == true) {
            final String currentUrl = request.getRequestURL().toString();
            String returnUrl = currentUrl.replace("summary/placeOrder", "hop-response");
            transactionParameters.put("return_url", returnUrl);
            transactionParameters.put("error_return_url", returnUrl);
            transactionParameters.put("payment_data", paymentParameters);
        } else {

            transactionParameters.put("payment_data", paymentParameters);
        }
        dataParameters.put("merchant", merchantParameters);
        dataParameters.put("customer", customerParameters);
        dataParameters.put("transaction", transactionParameters);
        dataParameters.put("custom", customParameters);

        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(dataParameters);

        String password = baseStore.getNovalnetPaymentAccessKey().trim();
        String url = "https://payport.novalnet.de/v2/payment";
        if(verify_payment_data == true) {
			url = "https://payport.novalnet.de/v2/authorize";
		}
        StringBuilder response = novalnetFacade.sendRequest(url, jsonString);
        JSONObject tomJsonObject = new JSONObject(response.toString());
        JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
        JSONObject transactionJsonObject = tomJsonObject.getJSONObject("transaction");
        
        if(!String.valueOf(CONVERT_TO_CENT_OR_SUCCESS_STATUS).equals(resultJsonObject.get("status_code").toString())) {
			final String statMessage = resultJsonObject.get("status_text").toString() != null ? resultJsonObject.get("status_text").toString() : resultJsonObject.get("status_desc").toString();
			getSessionService().setAttribute("novalnetCheckoutError", statMessage);
			return getCheckoutStep().previousStep();
		}

        if (redirect == true) {
			String redirectURL = resultJsonObject.get("redirect_url").toString();
			setupPageModel(model);
			model.addAttribute("paygateUrl", redirectURL);
			getSessionService().setAttribute("txn_secret", transactionJsonObject.get("txn_secret").toString());
			getSessionService().setAttribute("txn_check", baseStore.getNovalnetPaymentAccessKey().trim());
			return "redirect:" + redirectURL;
        }

        JSONObject customerJsonObject = tomJsonObject.getJSONObject("customer");
        

        if (validateOrderForm(placeOrderForm, model)) {
            return enterStep(model, redirectModel);
        }

        //Validate the cart
        if (validateCart(redirectModel)) {
            // Invalid cart. Bounce back to the cart page.
            return REDIRECT_PREFIX + "/cart";
        }

        String[] successStatus = {"CONFIRMED", "ON_HOLD", "PENDING"};

        if (Arrays.asList(successStatus).contains(transactionJsonObject.get("status").toString())) {
            final CartModel cartModel = novalnetFacade.getNovalnetCheckoutCart();

            String paymentName = novalnetFacade.getPaymentName(currentPayment);
            String orderComments = Localization.getLocalizedString("novalnet.paymentname") + ": " + paymentName + "<br>";
            orderComments += "Novalnet transaction id : " + transactionJsonObject.get("tid");
            AddressData addressData = getSessionService().getAttribute("novalnetAddressData");

            String bankDetails = "";
            if (("novalnetInvoice".equals(currentPayment) || "novalnetPrepayment".equals(currentPayment) || "novalnetGuaranteedInvoice".equals(currentPayment))) {
                bankDetails += "<br>";
                JSONObject bankdeatailsJsonObject = transactionJsonObject.getJSONObject("bank_details");
                bankDetails += "<br>" + String.format(Localization.getLocalizedString("novalnet.bankDetailsComments1"), cartData.getTotalPriceWithTax().getFormattedValue());
                if (transactionJsonObject.has("due_date")  && !"ON_HOLD".equals(transactionJsonObject.get("status").toString())) {
					bankDetails += " " +  String.format(Localization.getLocalizedString("novalnet.bankDetailsComments2"), transactionJsonObject.get("due_date").toString());
				}
                bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsAccountHolder") + " " + bankdeatailsJsonObject.get("account_holder").toString();
                bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsBank") + " " + bankdeatailsJsonObject.get("bank_name").toString()+ "<br>";
                bankDetails += Localization.getLocalizedString("novalnet.bankPlace") + " " + bankdeatailsJsonObject.get("bank_place").toString();
                bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsIban") + " " + bankdeatailsJsonObject.get("iban").toString();
                bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsBic") + " " + bankdeatailsJsonObject.get("bic").toString();
                
                bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailspaymentRefernceMulti") + "<br>" + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : " + transactionJsonObject.get("tid").toString() + "<br>";

            } else if ("novalnetBarzahlen".equals(currentPayment)) {
                JSONObject storeJsonObject = transactionJsonObject.getJSONObject("nearest_stores");
                Iterator<String> keys = storeJsonObject.keys();
                bankDetails += "<br><br>" + "Store(s) near you";
                bankDetails += "<br>" + "Slip expiry date:" + " " + transactionJsonObject.get("due_date").toString();

                while (keys.hasNext()) {

                    String key = keys.next();
                    if (storeJsonObject.get(key) instanceof JSONObject) {
                        JSONObject nearestStoreJsonObject = storeJsonObject.getJSONObject(key);

                        bankDetails += "<br>" + nearestStoreJsonObject.get("store_name");
                        bankDetails += "<br>" + nearestStoreJsonObject.get("street");
                        bankDetails += "<br>" + nearestStoreJsonObject.get("city");
                        bankDetails += "<br>" + nearestStoreJsonObject.get("zip");
                        bankDetails += "<br>" + nearestStoreJsonObject.get("country_code") + "<br>";
                    }
                }
            } else if ("novalnetMultibanco".equals(currentPayment)) {
				bankDetails += "<br>" + Localization.getLocalizedString("novalnet.multibancocomments1") + " " + cartData.getTotalPriceWithTax().getFormattedValue() + Localization.getLocalizedString("novalnet.multibancocomments2") + "<br>" + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : " + transactionJsonObject.get("partner_payment_reference").toString();
			}

            getSessionService().setAttribute("tid", orderComments + bankDetails);
            getSessionService().setAttribute("email", customerJsonObject.getString("email"));
            
			if ("novalnetCreditCard".equals(currentPayment) && !novalnetFacade.isGuestUser() && oneClickShopping) {
				boolean novalnetCreditCardStorePaymentData = getSessionService().getAttribute("novalnetCreditCardStorePaymentData");
				
				if(novalnetCreditCardStorePaymentData == true) {
					novalnetFacade.handleReferenceTransactionInfo(response, customerNo, "novalnetCreditCard");
				}
			} else if ("novalnetDirectDebitSepa".equals(currentPayment) && !novalnetFacade.isGuestUser() && oneClickShopping) {
				
				boolean novalnetDirectDebitSepaStorePaymentData = getSessionService().getAttribute("novalnetDirectDebitSepaStorePaymentData");

				if(novalnetDirectDebitSepaStorePaymentData == true) {
					novalnetFacade.handleReferenceTransactionInfo(response, customerNo, "novalnetDirectDebitSepa");
				}
			} else if ("novalnetGuaranteedDirectDebitSepa".equals(currentPayment) && !novalnetFacade.isGuestUser() && oneClickShopping) {
				
				boolean novalnetGuaranteedDirectDebitSepaStorePaymentData = getSessionService().getAttribute("novalnetGuaranteedDirectDebitSepaStorePaymentData");

				if(novalnetGuaranteedDirectDebitSepaStorePaymentData == true) {
					novalnetFacade.handleReferenceTransactionInfo(response, customerNo, "novalnetDirectDebitSepa");
				}
			}

            transactionParameters.clear();
            dataParameters.clear();
            
            final OrderData orderData;

            orderData = novalnetFacade.saveOrderData(orderComments, currentPayment, transactionJsonObject.get("status").toString(), orderAmountCent, transactionJsonObject.getString("currency"), transactionJsonObject.get("tid").toString(), customerJsonObject.getString("email"), addressData, bankDetails);
            

            transactionParameters.put("tid", transactionJsonObject.get("tid"));
            transactionParameters.put("order_no", orderData.getCode());

            dataParameters.put("transaction", transactionParameters);
            dataParameters.put("custom", customParameters);

            jsonString = gson.toJson(dataParameters);
            url = "https://payport.novalnet.de/v2/transaction/update";
            StringBuilder responseString = novalnetFacade.sendRequest(url, jsonString);

            return confirmationPageURL(orderData);
        } else {
            // Get server response text
            final String statusMessage = resultJsonObject.get("status_text").toString() != null ? resultJsonObject.get("status_text").toString() : resultJsonObject.get("status_desc").toString();
            getSessionService().setAttribute("novalnetCheckoutError", statusMessage);
            return getCheckoutStep().previousStep();
        }
    }

    /**
     * Get the value of the remote ip address.
     *
     * @param request Request value.
     * @return Remote IP address
     */
    public static String getRemoteIpAddr(HttpServletRequest request) {
        try {
            InetAddress ipAddr = InetAddress.getByName(request.getRemoteAddr());
            if (ipAddr instanceof Inet4Address) {
                return ipAddr.getHostAddress();
            } else if (ipAddr instanceof Inet6Address) {
                return "127.0.0.1";
            }
        } catch (UnknownHostException ex) {
            LOGGER.error("UnknownHostException ", ex);
        }
        return "127.0.0.1";
    }

    public static Map parseResponse(String response) {
        Map<String, String> parsedResponse = new HashMap<>();
        String parameters[] = response.split("&");
        for (String parameter : parameters) {
            String p[] = parameter.split("=");
            if (p != null && p.length > 1) {
                parsedResponse.put(p[0], p[1]);
            }
        }
        return parsedResponse;
    }

    private JsonObject getJSON(final String token) {
        final String claim = token;
        final StringReader reader = new StringReader(claim);

        final JsonReader jsonReader = Json.createReader(reader);
        final JsonObject json = jsonReader.readObject();
        jsonReader.close();
        reader.close();
        return json;

    }

    public static String formatDate(int date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendarInsatance = Calendar.getInstance();
        calendarInsatance.add(calendarInsatance.DATE, date);
        return dateFormat.format(calendarInsatance.getTime());
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

    /**
     * Validates the order form before to filter out invalid order states
     *
     * @param placeOrderForm The spring form of the order being submitted
     * @param model          A spring Model
     * @return True if the order form is invalid and false if everything is valid.
     */
    protected boolean validateOrderForm(final PlaceOrderForm placeOrderForm, final Model model) {
        final String securityCode = placeOrderForm.getSecurityCode();
        boolean invalid = false;

        if (getCheckoutFlowFacade().hasNoDeliveryAddress()) {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryAddress.notSelected");
            invalid = true;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryMode()) {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryMethod.notSelected");
            invalid = true;
        }

		// Only require the Security Code to be entered on the summary page if the SubscriptionPciOption is set to Default.
		if (!getCheckoutFlowFacade().hasNoPaymentInfo() && CheckoutPciOptionEnum.DEFAULT.equals(getCheckoutFlowFacade().getSubscriptionPciOption())
				&& StringUtils.isBlank(securityCode)) {
			GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.noSecurityCode");
			invalid = true;
		}


        if (!placeOrderForm.isTermsCheck()) {
            GlobalMessages.addErrorMessage(model, "checkout.error.terms.not.accepted");
            invalid = true;
            return invalid;
        }
        final CartData cartData = getCheckoutFacade().getCheckoutCart();

        if (!getCheckoutFacade().containsTaxValues()) {
            LOGGER.error(String.format(
                    "Cart %s does not have any tax values, which means the tax cacluation was not properly done, placement of order can't continue",
                    cartData.getCode()));
            GlobalMessages.addErrorMessage(model, "checkout.error.tax.missing");
            invalid = true;
        }

        if (!cartData.isCalculated()) {
            LOGGER.error(
                    String.format("Cart %s has a calculated flag of FALSE, placement of order can't continue", cartData.getCode()));
            GlobalMessages.addErrorMessage(model, "checkout.error.cart.notcalculated");
            invalid = true;
        }

        return invalid;
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

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(SUMMARY);
    }

    public BaseStoreModel getBaseStoreModel() {
        return getBaseStoreService().getCurrentBaseStore();
    }

    public static String getPaymentType(String paymentName) {
        final Map<String, String> paymentType = new HashMap<String, String>();
        paymentType.put("novalnetCreditCard", "CREDITCARD");
        paymentType.put("novalnetDirectDebitSepa", "DIRECT_DEBIT_SEPA");
        paymentType.put("novalnetGuaranteedDirectDebitSepa", "GUARANTEED_DIRECT_DEBIT_SEPA");
        paymentType.put("novalnetInvoice", "INVOICE");
        paymentType.put("novalnetGuaranteedInvoice", "GUARANTEED_INVOICE");
        paymentType.put("novalnetPrepayment", "PREPAYMENT");
        paymentType.put("novalnetBarzahlen", "CASHPAYMENT");
        paymentType.put("novalnetPayPal", "PAYPAL");
        paymentType.put("novalnetInstantBankTransfer", "ONLINE_TRANSFER");
        paymentType.put("novalnetOnlineBankTransfer", "ONLINE_BANK_TRANSFER");
        paymentType.put("novalnetBancontact", "BANCONTACT");
        paymentType.put("novalnetMultibanco", "MULTIBANCO");
        paymentType.put("novalnetIdeal", "IDEAL");
        paymentType.put("novalnetEps", "EPS");
        paymentType.put("novalnetGiropay", "GIROPAY");
        paymentType.put("novalnetPrzelewy24", "PRZELEWY24");
        paymentType.put("novalnetPostFinanceCard", "POSTFINANCE_CARD");
        paymentType.put("novalnetPostFinance", "POSTFINANCE");
        return paymentType.get(paymentName);
    }

    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    protected void setupPageModel(final Model model) throws CMSItemNotFoundException {
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

}
