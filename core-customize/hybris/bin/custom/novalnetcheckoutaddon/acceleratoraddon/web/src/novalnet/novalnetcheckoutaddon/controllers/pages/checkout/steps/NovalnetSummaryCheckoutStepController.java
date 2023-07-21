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
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;
import de.novalnet.order.NovalnetOrderFacade;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.order.CartModel;
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


    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/multi/novalnet/order/confirmation/";

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    @Resource(name = "cartService")
    private CartService cartService;
    
    @Resource(name = "novalnetOrderFacade")
    NovalnetOrderFacade novalnetOrderFacade;

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
        //~ model.addAttribute("paymentInfo", cartData.getPaymentInfo());

        model.addAttribute(new PlaceOrderForm());

        final ContentPageModel multiCheckoutSummaryPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, multiCheckoutSummaryPage);
        setUpMetaDataForContentPage(model, multiCheckoutSummaryPage);

        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.summary.breadcrumb"));
        model.addAttribute("metaRobots", "noindex,nofollow");
        setCheckoutStepLinksForModel(model, getCheckoutStep());
        return NovalnetcheckoutaddonControllerConstants.CheckoutSummaryPage;
    }
    
    @RequestMapping(value = "/placeOrder")
    @PreValidateQuoteCheckoutStep
    @RequireHardLogIn
    public String placeOrder(@ModelAttribute("placeOrderForm") final PlaceOrderForm placeOrderForm, final Model model,
    final HttpServletRequest request, final RedirectAttributes redirectModel) throws CMSItemNotFoundException, // NOSONAR
    InvalidCartException, CommerceCartModificationException {
        
        if (validateOrderForm(placeOrderForm, model))
        {
            return enterStep(model, redirectModel);
        }

        //Validate the cart
        if (validateCart(redirectModel))
        {
            // Invalid cart. Bounce back to the cart page.
            return REDIRECT_PREFIX + "/cart";
        }
        Optional<HttpServletRequest> optionalRequest = Optional.ofNullable(request);
        String result =  novalnetOrderFacade.processPayment(optionalRequest, model);
        JSONObject tomJsonObject = new JSONObject(result.toString());
        JSONObject resultJson = tomJsonObject.getJSONObject("result");
        
        if(result.equals("payment_error")) {
			final String statusMessage = resultJson.get("status_text").toString() != null ? resultJson.get("status_text").toString() : resultJson.get("status_desc").toString();
            getSessionService().setAttribute("novalnetCheckoutError", statusMessage);
			return getCheckoutStep().previousStep();
		} else if (result.equals("order_error")) {
			GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");
			return enterStep(model, redirectModel);
			
		} else {
			return result;
		}
    }
    
    /**
     * Validates the order form before to filter out invalid order states
     *
     * @param placeOrderForm
     *           The spring form of the order being submitted
     * @param model
     *           A spring Model
     * @return True if the order form is invalid and false if everything is valid.
     */
    protected boolean validateOrderForm(final PlaceOrderForm placeOrderForm, final Model model)
    {
        final String securityCode = placeOrderForm.getSecurityCode();
        boolean invalid = false;

        if (!getCheckoutFlowFacade().hasValidCart())
        {
            GlobalMessages.addErrorMessage(model, "checkout.error.cart.invalid");
            invalid = true;
            return invalid;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryAddress())
        {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryAddress.notSelected");
            invalid = true;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryMode())
        {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryMethod.notSelected");
            invalid = true;
        }


        if (!placeOrderForm.isTermsCheck())
        {
            GlobalMessages.addErrorMessage(model, "checkout.error.terms.not.accepted");
            invalid = true;
            return invalid;
        }
        final CartData cartData = getCheckoutFacade().getCheckoutCart();

        if (!getCheckoutFacade().containsTaxValues())
        {
            LOGGER.error(String.format(
                    "Cart %s does not have any tax values, which means the tax cacluation was not properly done, placement of order can't continue",
                    cartData.getCode()));
            GlobalMessages.addErrorMessage(model, "checkout.error.tax.missing");
            invalid = true;
        }

        if (!cartData.isCalculated())
        {
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

}
