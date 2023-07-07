package de.novalnet.order;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.Arrays;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Iterator;

import de.hybris.platform.webservicescommons.util.YSanitizer;
import javax.annotation.Resource;

import de.hybris.platform.core.PK;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.i18n.comparators.CountryComparator;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.order.CommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsListWsDTO;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.TitleModel;
import de.hybris.novalnet.core.model.NovalnetCallbackInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartFactory;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.NumberFormat;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.nio.charset.StandardCharsets;

import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentRefInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetCallbackInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentModeModel;

import de.novalnet.beans.NnPaymentDetailsData;
import de.novalnet.beans.NnCreditCardData;
import de.novalnet.beans.NnDirectDebitSepaData;
import de.novalnet.beans.NnPayPalData;
import de.novalnet.beans.NnGuaranteedDirectDebitSepaData;
import de.novalnet.beans.NnGuaranteedInvoiceData;
import de.novalnet.beans.NnInvoiceData;
import de.novalnet.beans.NnPrepaymentData;
import de.novalnet.beans.NnBarzahlenData;
import de.novalnet.beans.NnInstantBankTransferData;
import de.novalnet.beans.NnOnlineBankTransferData;
import de.novalnet.beans.NnBancontactData;
import de.novalnet.beans.NnMultibancoData;
import de.novalnet.beans.NnIdealData;
import de.novalnet.beans.NnEpsData;
import de.novalnet.beans.NnGiropayData;
import de.novalnet.beans.NnPaymentData;
import de.novalnet.beans.NnPrzelewy24Data;
import de.novalnet.beans.NnPostFinanceCardData;
import de.novalnet.beans.NnPostFinanceData;
import de.novalnet.beans.NnConfigData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Facade for setting shipping options on marketplace order entries
 */
public class NovalnetOrderFacade {

    private final static Logger LOG = Logger.getLogger(NovalnetOrderFacade.class);

    private BaseStoreService baseStoreService;
    private SessionService sessionService;
    private CartService cartService;
    private OrderFacade orderFacade;
    private CheckoutFacade checkoutFacade;
    private CheckoutCustomerStrategy checkoutCustomerStrategy;
    private ModelService modelService;
    private FlexibleSearchService flexibleSearchService;
    private CommerceCheckoutService commerceCheckoutService;
    private Converter<AddressData, AddressModel> addressReverseConverter;
    private Converter<CountryModel, CountryData> countryConverter;
    private Converter<OrderModel, OrderData> orderConverter;
    private CartFactory cartFactory;
    private CalculationService calculationService;
    private Populator<AddressModel, AddressData> addressPopulator;
    private CommonI18NService commonI18NService;
    private CustomerAccountService customerAccountService;

    public static final int DAYS_IN_A_YEAR = 365;
    public static final int TOTAL_HOURS = 24;
    public static final int TOTAL_MINUTES_SECONDS = 60;
    public static final int AGE_REQUIREMENT = 18;

    @Resource(name = "i18NFacade")
    private I18NFacade i18NFacade;

    @Resource(name = "cartLoaderStrategy")
    private CartLoaderStrategy cartLoaderStrategy;

    @Resource(name = "userFacade")
    private UserFacade userFacade;
    
    @Resource(name = "commerceWebServicesCartFacade2")
    private CartFacade cartFacade;

    @Resource
    private PaymentModeService paymentModeService;

    public static final String ADDRESS_DOES_NOT_EXIST = "Address with given id: '%s' doesn't exist or belong to another user";
    private static final String OBJECT_NAME_ADDRESS_ID = "addressId";

    public BaseStoreModel getBaseStoreModel() {
        return getBaseStoreService().getCurrentBaseStore();
    }

    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public CartService getCartService() {
        return cartService;
    }

    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    public OrderFacade getOrderFacade() {
        return orderFacade;
    }

    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    public CheckoutFacade getCheckoutFacade() {
        return checkoutFacade;
    }

    public void setCheckoutFacade(CheckoutFacade checkoutFacade) {
        this.checkoutFacade = checkoutFacade;
    }

    public CheckoutCustomerStrategy getCheckoutCustomerStrategy() {
        return checkoutCustomerStrategy;
    }

    public void setCheckoutCustomerStrategy(CheckoutCustomerStrategy checkoutCustomerStrategy) {
        this.checkoutCustomerStrategy = checkoutCustomerStrategy;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public CommonI18NService getCommonI18NService() {
        return commonI18NService;
    }

    public void setCommonI18NService(CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public Converter<AddressData, AddressModel> getAddressReverseConverter() {
        return addressReverseConverter;
    }

    public void setAddressReverseConverter(Converter<AddressData, AddressModel> addressReverseConverter) {
        this.addressReverseConverter = addressReverseConverter;
    }

    public I18NFacade getI18NFacade() {
        return i18NFacade;
    }

    public void setI18NFacade(I18NFacade i18NFacade) {
        this.i18NFacade = i18NFacade;
    }

    protected Converter<CountryModel, CountryData> getCountryConverter() {
        return countryConverter;
    }

    @Required
    public void setCountryConverter(final Converter<CountryModel, CountryData> countryConverter) {
        this.countryConverter = countryConverter;
    }

    public Converter<OrderModel, OrderData> getOrderConverter() {
        return orderConverter;
    }

    public void setOrderConverter(Converter<OrderModel, OrderData> orderConverter) {
        this.orderConverter = orderConverter;
    }

    public CartFactory getCartFactory() {
        return cartFactory;
    }

    public void setCartFactory(CartFactory cartFactory) {
        this.cartFactory = cartFactory;
    }

    public CalculationService getCalculationService() {
        return calculationService;
    }

    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public Populator<AddressModel, AddressData> getAddressPopulator() {
        return addressPopulator;
    }

    /**
     * Set address populator
     *
     * @param addressPopulator
     */
    public void setAddressPopulator(Populator<AddressModel, AddressData> addressPopulator) {
        this.addressPopulator = addressPopulator;
    }

    public void addPaymentDetailsInternal(final NovalnetPaymentInfoModel paymentInfo)
    {
        final CustomerModel currentCustomer = getCurrentUserForCheckout();
        getCustomerAccountService().setDefaultPaymentInfo(currentCustomer, paymentInfo);
        final CartModel cartModel = getCart();
        modelService.save(paymentInfo);
        cartModel.setPaymentInfo(paymentInfo);
        modelService.save(cartModel);
    }

    /**
     * Get Payment transaction entry
     *
     * @param requestId
     * @param cartModel
     * @param amount
     * @param backendTransactionComments
     * @param currencyCode
     */
    public PaymentTransactionEntryModel createTransactionEntry(final String requestId, final CartModel cartModel, final int amount, String backendTransactionComments, String currencyCode) {
        final PaymentTransactionEntryModel paymentTransactionEntry = getModelService().create(PaymentTransactionEntryModel.class);
        paymentTransactionEntry.setRequestId(requestId);
        paymentTransactionEntry.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntry.setTransactionStatus(TransactionStatus.ACCEPTED.name());
        paymentTransactionEntry.setTransactionStatusDetails(backendTransactionComments);
        paymentTransactionEntry.setCode(cartModel.getCode());

        final CurrencyModel currency = getCurrencyForIsoCode(currencyCode);
        paymentTransactionEntry.setCurrency(currency);

        final BigDecimal transactionAmount = BigDecimal.valueOf(amount / 100);
        paymentTransactionEntry.setAmount(transactionAmount);
        paymentTransactionEntry.setTime(new Date());

        return paymentTransactionEntry;
    }

    private CurrencyModel getCurrencyForIsoCode(final String currencyIsoCode) {
        CurrencyModel currencyModel = new CurrencyModel();
        currencyModel.setIsocode(currencyIsoCode);
        currencyModel = getFlexibleSearchService().getModelByExample(currencyModel);
        return currencyModel;
    }

    public CustomerModel getCurrentUserForCheckout()
    {
        return getCheckoutCustomerStrategy().getCurrentUserForCheckout();
    }

    protected CommerceCheckoutParameter createCommerceCheckoutParameter(final CartModel cart, final boolean enableHooks)
    {
        final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
        parameter.setEnableHooks(enableHooks);
        parameter.setCart(cart);
        return parameter;
    }

    protected CustomerAccountService getCustomerAccountService()
    {
        return customerAccountService;
    }

    @Required
    public void setCustomerAccountService(final CustomerAccountService customerAccountService)
    {
        this.customerAccountService = customerAccountService;
    }

    public boolean hasCheckoutCart()
    {
        return getCartFacade().hasSessionCart();
    }

    public CartModel getCart()
    {
        return hasCheckoutCart() ? getCartService().getSessionCart() : null;
    }

    public CartFacade getCartFacade()
    {
        return cartFacade;
    }

    @Required
    public void setCartFacade(final CartFacade cartFacade)
    {
        this.cartFacade = cartFacade;
    }

    public CartData getSessionCart()
    {
        return cartFacade.getSessionCart();
    }

    protected CommerceCheckoutService getCommerceCheckoutService()
    {
        return commerceCheckoutService;
    }

    @Required
    public void setCommerceCheckoutService(final CommerceCheckoutService commerceCheckoutService)
    {
        this.commerceCheckoutService = commerceCheckoutService;
    }

    public CartData loadCart(final String cartId) {
        cartLoaderStrategy.loadCart(cartId);
        final CartData cartData = getSessionCart();
        return cartData;
    }

    public AddressModel createBillingAddress(String addressId) {

        final AddressModel billingAddress = getModelService().create(AddressModel.class);
        billingAddress.setFirstname("");
        billingAddress.setLastname("");
        billingAddress.setLine1("");
        billingAddress.setLine2("");
        billingAddress.setTown("");
        billingAddress.setPostalcode("");
        billingAddress.setCountry(getCommonI18NService().getCountry("DE"));

        final AddressData addressData = getAddressData(addressId);

        getAddressReverseConverter().convert(addressData, billingAddress);

        return billingAddress;
    }

    /**
     * retrieve address data
     *
     * @param addressId id of the address
     * @return addressData
     */
    public AddressData getAddressData(final String addressId)
    {
        final AddressData addressData = getUserFacade().getAddressForCode(addressId);
        if (addressData == null)
        {
            throw new RequestParameterException(String.format(ADDRESS_DOES_NOT_EXIST, sanitize(addressId)),
                    RequestParameterException.INVALID, OBJECT_NAME_ADDRESS_ID);
        }
        return addressData;
    }

    /**
     * retrieve user facade
     *
     * @param input string
     * @return userFacade
     */
    protected UserFacade getUserFacade()
    {
        return userFacade;
    }

    /**
     * sanitize strinf
     *
     * @param input string
     * @return String
     */
    protected static String sanitize(final String input)
    {
        return YSanitizer.sanitize(input);
    }

     /**
     * Update Order comments
     *
     * @param comments  Formed comments
     * @param orderCode Order code of the order
     * @param transactionStatus transaction status for the order
     */
    public void updateCallbackComments(String comments, String orderCode, String transactionStatus) {
        List<NovalnetPaymentInfoModel> paymentInfo = getNovalnetPaymentInfo(orderCode);

        // Update NovalnetPaymentInfo Order entry notes
        NovalnetPaymentInfoModel paymentInfoModel = this.getModelService().get(paymentInfo.get(0).getPk());
        String previousComments = paymentInfoModel.getOrderHistoryNotes();
        paymentInfoModel.setOrderHistoryNotes(previousComments + "<br><br>" + comments);
        paymentInfoModel.setPaymentGatewayStatus(transactionStatus);
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);

        // Update OrderHistoryEntries
        OrderModel orderModel = this.getModelService().get(orderInfoModel.get(0).getPk());
        OrderHistoryEntryModel orderEntry = this.getModelService().create(OrderHistoryEntryModel.class);
        orderEntry.setTimestamp(new Date());
        orderEntry.setOrder(orderModel);
        orderEntry.setDescription(comments);

        // Save the updated models
        this.getModelService().saveAll(paymentInfoModel, orderEntry);
    }

    /**
     * Get order model
     *
     * @param orderCode Order code of the order
     * @return SearchResult
     */
    public List<OrderModel> getOrderInfoModel(String orderCode) {
        // Initialize StringBuilder
        StringBuilder query = new StringBuilder();

        // Select query for fetch OrderModel
        query.append("SELECT {pk} from {" + OrderModel._TYPECODE + "} where {" + OrderModel.CODE
                + "} = ?code");
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(query.toString());

        // Add query parameter
        executeQuery.addQueryParameter("code", orderCode);

        // Execute query
        SearchResult<OrderModel> result = getFlexibleSearchService().search(executeQuery);
        return result.getResult();
    }

    /**
     * Update OrderStatus of the order
     *
     * @param orderCode Order code of the order
     */
    public void updatePartPaidStatus(String orderCode) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);

        // Update Part paid status
        OrderModel orderModel = this.getModelService().get(orderInfoModel.get(0).getPk());
        orderModel.setPaymentStatus(PaymentStatus.PARTPAID);

        this.getModelService().save(orderModel);
    }

    /**
     * Update callback info model
     *
     * @param callbackTid     Transaction Id of the executed callback
     * @param orderReference  Order reference list
     * @param orderPaidAmount Total paid amount
     */
    public void updateCallbackInfo(long callbackTid, List<NovalnetCallbackInfoModel> orderReference, int orderPaidAmount) {
        NovalnetCallbackInfoModel callbackInfoModel = this.getModelService().get(orderReference.get(0).getPk());

        // Update Callback TID
        callbackInfoModel.setCallbackTid(callbackTid);

        // Update Paid amount
        callbackInfoModel.setPaidAmount(orderPaidAmount);

        // Save the updated model
        this.getModelService().save(callbackInfoModel);
    }

    /**
     * Get Novalnet payment info model
     *
     * @param orderCode Order code of the order
     * @return SearchResult
     */
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {

        // Initialize StringBuilder
        StringBuilder query = new StringBuilder();

        // Select query for fetch NovalnetPaymentInfoModel
        query.append("SELECT {pk} from {PaymentInfo} where {" + PaymentInfoModel.CODE
                + "} = ?code AND {" + PaymentInfoModel.DUPLICATE + "} = ?duplicate");
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(query.toString());

        // Add query parameter
        executeQuery.addQueryParameter("code", orderCode);
        executeQuery.addQueryParameter("duplicate", Boolean.FALSE);

        // Execute query
        SearchResult<NovalnetPaymentInfoModel> result = getFlexibleSearchService().search(executeQuery);
        return result.getResult();

    }

    /**
     * Get Payment model
     *
     * @param paymentInfo info of the payment
     * @return paymentModel
     */
    public NovalnetPaymentInfoModel getPaymentModel(final List<NovalnetPaymentInfoModel> paymentInfo) {
        final NovalnetPaymentInfoModel paymentModel = this.getModelService().get(paymentInfo.get(0).getPk());
        return paymentModel;
    }

    /**
     * Update order status
     *
     * @param orderCode Order code of the order
     * @param paymentInfoModel payment configurations
     */
    public void updateOrderStatus(String orderCode, NovalnetPaymentInfoModel paymentInfoModel) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);

        OrderModel orderModel = this.getModelService().get(orderInfoModel.get(0).getPk());        
        
        if ("PENDING".equals(paymentInfoModel.getPaymentGatewayStatus())) {
            orderModel.setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
        }else if ("ON_HOLD".equals(paymentInfoModel.getPaymentGatewayStatus())) {
            orderModel.setStatus( OrderStatus.PAYMENT_AUTHORIZED);
        } else {
            orderModel.setStatus(OrderStatus.COMPLETED);
        }

        final String paymentMethod = paymentInfoModel.getPaymentProvider();
        
        String[] pendingStatusCode = {"ON_HOLD","PENDING"};

        // Check for payment pending payments
        if(Arrays.asList(pendingStatusCode).contains(paymentInfoModel.getPaymentGatewayStatus()))
        {
            orderModel.setPaymentStatus(PaymentStatus.NOTPAID);
        }
        else
        {
            // Update the payment status for completed payments
            orderModel.setPaymentStatus(PaymentStatus.PAID);
        }

        this.getModelService().save(orderModel);

    }

    /**
     * Update Cancel order status
     *
     * @param orderCode Order code of the order
     */
    public void updateCancelStatus(String orderCode) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);

        // Update OrderHistoryEntries
        OrderModel orderModel = this.getModelService().get(orderInfoModel.get(0).getPk());

        final BaseStoreModel baseStore = this.getBaseStoreModel();
        OrderStatus orderStatus = OrderStatus.CANCELLED;

        orderModel.setStatus(orderStatus);

        this.getModelService().save(orderModel);

    }

    /**
     * Update Payment info details in database
     *
     * @param orderReference order details
     * @param tidStatus status of the transacction
     */
    public void updatePaymentInfo(List<NovalnetPaymentInfoModel> orderReference, String tidStatus) {
        NovalnetPaymentInfoModel paymentInfoModel = this.getModelService().get(orderReference.get(0).getPk());

        // Update Callback TID
        paymentInfoModel.setPaymentGatewayStatus(tidStatus);

        // Save the updated model
        this.getModelService().save(paymentInfoModel);
    }

    /**
     * Check age requirement
     *
     * @param dateInString dob of the customer
     */
    public static boolean hasAgeRequirement(String dateInString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            Date birthDate = sdf.parse(dateInString);

            long ageInMillis = System.currentTimeMillis() - birthDate.getTime();

            long years = ageInMillis / (DAYS_IN_A_YEAR * TOTAL_HOURS * TOTAL_MINUTES_SECONDS * TOTAL_MINUTES_SECONDS * 1000l);

            if (years >= AGE_REQUIREMENT) {
                return true;
            }
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Get callback info model
     *
     * @param transactionId Transaction ID of the order
     * @return SearchResult
     */
    public List<NovalnetCallbackInfoModel> getCallbackInfo(String transactionId) {
        // Initialize StringBuilder
        StringBuilder query = new StringBuilder();

        // Select query for fetch NovalnetCallbackInfoModel
        query.append("SELECT {pk} from {" + NovalnetCallbackInfoModel._TYPECODE + "} where {" + NovalnetCallbackInfoModel.ORGINALTID
                + "} = ?transctionId");
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(query.toString());

        // Add query parameter
        executeQuery.addQueryParameter("transctionId", transactionId);

        // Execute query
        SearchResult<NovalnetCallbackInfoModel> result = getFlexibleSearchService().search(executeQuery);
        return result.getResult();
    }

    /**
     * Returns the default billing address for the given customer.
     *
     * @param billingAddressPk
     *           the customer's unique ID
     * @return the default billing address of the user
     */
    public AddressModel getBillingAddress(final String billingAddressPk)
    {
        return "0".equals(billingAddressPk) ? null : (AddressModel) getModelService().get(PK.parse(billingAddressPk));
    }
    
    public String formPaymentRequest(CartData cartData, String emailAddress, Integer orderAmountCent, String currency, String languageCode, String paymentData, Optional<HttpServletRequest> request) {

        final Map<String, Object> dataParameters    =  new HashMap<String, Object>();

        final AddressData addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
        
        dataParameters.put("customer",    buildCustomerData(cartData.getDeliveryAddress(), emailAddress, paymentData));
        dataParameters.put("merchant",    buildMerchanData(addressData));
        dataParameters.put("transaction", buildTransactionData(orderAmountCent, currency, paymentData, request));
        
        dataParameters.put("custom",      buildCustomData(languageCode));
        
        if ("paymentForm".equals(paymentData)) {
            dataParameters.put("hosted_page", buildHostedPageData());
        }
        
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(dataParameters);

        return jsonString;
    }
    
    public Map<String, Object> buildCustomData(String languageCode) {
        
        final Map<String, Object> customParameters = new HashMap<String, Object>();
        
        customParameters.put("lang", languageCode);
        
        return customParameters;
    }
    
    public Map<String, Object> buildTransactionData(Integer orderAmountCent, String currency, String paymentData, Optional<HttpServletRequest> request) {
        
        final Map<String, Object> transactionParameters = new HashMap<String, Object>();
        String returnUrl, currentUrl;
        returnUrl = currentUrl = "";
        
        transactionParameters.put("currency", currency);
        transactionParameters.put("amount", orderAmountCent);
        transactionParameters.put("system_name", "SAP Commerce Cloud");
        transactionParameters.put("system_version", "2211-NN2.0.0");
        
        if (!"paymentForm".equals(paymentData)) {
            
            final Map<String, Object> paymentDataParameters = new HashMap<String, Object>();
            
            JSONObject json = new JSONObject(paymentData);
            
            JSONObject paymentDetails = new JSONObject();
            
            if (json.has("payment_details")) {
                
                paymentDetails = json.getJSONObject("payment_details");
                
                if (paymentDetails.has("type")) {
                    transactionParameters.put("payment_type", paymentDetails.get("type").toString());
                }
                
                if (paymentDetails.has("process_mode") && paymentDetails.get("process_mode").toString().equals("redirect")) {
                    transactionParameters.put("payment_type", paymentDetails.get("type").toString());
                    if (request.isPresent()) {
						HttpServletRequest httpRequest = request.get();
						currentUrl = httpRequest.getRequestURL().toString();
						returnUrl  = currentUrl.replace("summary/placeOrder", "hop-response");
					}
					
					transactionParameters.put("return_url", returnUrl);
					transactionParameters.put("error_return_url", returnUrl);
                }
                getSessionService().setAttribute("novalnetProcessMode", paymentDetails.get("process_mode").toString());
            }
            
            if (json.has("booking_details")) {
                
                JSONObject bookingDetails = json.getJSONObject("booking_details");
                
                if(bookingDetails.has("create_token") && bookingDetails.get("create_token").toString().equals("1") ) {
                    transactionParameters.put("create_token", bookingDetails.get("create_token").toString());
                }
                
                if(bookingDetails.has("payment_action") && bookingDetails.get("payment_action").toString().equals("zero_amount") ) {
                    transactionParameters.put("amount", "0");
                }
                
                if(bookingDetails.has("test_mode")) {
                    transactionParameters.put("test_mode", bookingDetails.get("test_mode").toString());
                }
                
                if(bookingDetails.has("due_date")) {
                    transactionParameters.put("due_date", bookingDetails.get("due_date").toString());
                }
                
                if(bookingDetails.has("pan_hash")) {
                    paymentDataParameters.put("pan_hash", bookingDetails.get("pan_hash").toString());
                }
                
                if(bookingDetails.has("wallet_token")) {
                    paymentDataParameters.put("wallet_token", bookingDetails.get("wallet_token").toString());
                }
                
                if(bookingDetails.has("unique_id")) {
                    paymentDataParameters.put("unique_id", bookingDetails.get("unique_id").toString());
                }
                
                if(bookingDetails.has("iban")) {
                    paymentDataParameters.put("iban", bookingDetails.get("iban").toString());
                }
                
                if(bookingDetails.has("account_holder")) {
                    paymentDataParameters.put("account_holder", bookingDetails.get("account_holder").toString());
                }
                
                if(bookingDetails.has("do_redirect") && bookingDetails.get("do_redirect").toString().equals("true")) {
                    getSessionService().setAttribute("novalnetProcessMode", "redirect");
                }
                
                if(bookingDetails.has("payment_action") && bookingDetails.get("payment_action").toString().equals("authorized")) {
                    getSessionService().setAttribute("novalnetPaymentAction", "auth");
                } else {
                    getSessionService().setAttribute("novalnetPaymentAction", "capture");
                }
                
                if(bookingDetails.has("force_non_gurantee") && bookingDetails.has("min_amount") ) {
                    if(Integer.parseInt(bookingDetails.get("min_amount").toString()) > orderAmountCent && !bookingDetails.get("force_non_gurantee").toString().equals("1")) {
                        String type = paymentDetails.get("type").toString();
                        String[] parts = type.split("GUARANTEED_");
                        transactionParameters.put("payment_type", parts[1]);
                    }
                }
                
                transactionParameters.put("payment_data", paymentDataParameters);
            }
        }
        
        return transactionParameters;
    }
    
    public Map<String, Object> buildHostedPageData() {
        
        final Map<String, Object> hostedPageParameters    = new HashMap<String, Object>();
        
        hostedPageParameters.put("type", "PAYMENTFORM");
        hostedPageParameters.put("hide_blocks", new String[] {"ADDRESS_FORM", "SHOP_INFO", "LANGUAGE_MENU", "TARIFF"});
        hostedPageParameters.put("skip_pages", new String[] {"CONFIRMATION_PAGE", "SUCCESS_PAGE"});
        
        return hostedPageParameters;
    }
    
    public Map<String, Object> buildMerchanData(AddressData addressData) {
        
        final Map<String, Object> merchantParameters    = new HashMap<String, Object>();

        final BaseStoreModel baseStore = this.getBaseStoreModel();
        
        merchantParameters.put("signature", baseStore.getNovalnetAPIKey());
        merchantParameters.put("tariff", baseStore.getNovalnetTariffId());
        
        return merchantParameters;
    }
    
    public Map<String, Object> buildCustomerData(AddressData addressData, String emailAddress, String paymentData) {
        
        //~ final Map<String, Object> customerParameters = new HashMap<String, Object>();
        //~ final Map<String, Object> billingParameters  = new HashMap<String, Object>();
        //~ final Map<String, Object> shippingParameters = new HashMap<String, Object>();

        //~ customerParameters.put("first_name", addressData.getFirstName());
        //~ customerParameters.put("last_name", addressData.getLastName());
        //~ customerParameters.put("email", emailAddress);
        
		 return getSessionService().getAttribute("novalnetAddressData");
        
			//~ billingParameters.put("street", addressData.getLine1() + " " + addressData.getLine2());
			//~ billingParameters.put("city", addressData.getTown());
			//~ billingParameters.put("zip", addressData.getPostalCode());
			//~ billingParameters.put("country_code", addressData.getCountry().getIsocode());
        
			//~ shippingParameters.put("same_as_billing", 1);
			
		
			
			//~ AddressData billingAddress = getSessionService().getAttribute("novalnetAddressdata");
			
			//~ billingParameters.put("street", billingAddress.getLine1() + " " + billingAddress.getLine2());
			//~ billingParameters.put("city", billingAddress.getTown());
			//~ billingParameters.put("zip", billingAddress.getPostalCode());
			//~ billingParameters.put("country_code", billingAddress.getCountry().getIsocode());
			
			//~ shippingParameters.put("street", addressData.getLine1() + " " + addressData.getLine2());
			//~ shippingParameters.put("city", addressData.getTown());
			//~ shippingParameters.put("zip", addressData.getPostalCode());
			//~ shippingParameters.put("country_code", addressData.getCountry().getIsocode());
			
		
        
        //~ customerParameters.put("billing", billingParameters);
        //~ customerParameters.put("shipping", shippingParameters);
        
        //~ if (!"paymentForm".equals(paymentData)) {
            
            //~ JSONObject json = new JSONObject(paymentData);
            
            //~ if (json.has("booking_details")) {
                
                //~ JSONObject bookingDetails = json.getJSONObject("booking_details");
                
                //~ if(bookingDetails.has("birth_date")) {
                    //~ customerParameters.put("birth_date", bookingDetails.get("birth_date").toString());
                //~ }
            //~ }
        //~ }
        
        //~ return customerParameters;
    }
    
    public StringBuilder sendRequest(String url, String jsonString) {
        
        StringBuilder response = new StringBuilder();
        try {
            
            LOG.info("request sent to novalnet");
            LOG.info(jsonString);

            final BaseStoreModel baseStore = this.getBaseStoreModel();

            String password = baseStore.getNovalnetPaymentAccessKey().trim();
            
            String urly = url;
            URL obj = new URL(urly);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            byte[] postData = jsonString.getBytes(StandardCharsets.UTF_8);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Charset", "utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("X-NN-Access-Key", Base64.getEncoder().encodeToString(password.getBytes()));

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader iny = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;


            while ((output = iny.readLine()) != null) {
                response.append(output);
            }
            iny.close();
        } catch (MalformedURLException ex) {
            LOG.error("MalformedURLException ", ex);
        } catch (IOException ex) {
            LOG.error("IOException ", ex);
        }

        LOG.info("response recieved from novalnet");
        LOG.info(response.toString());

        return response;
    }
    

    public Integer getOrderAmount(CartData cartData) {
        BigDecimal totalAmount          = BigDecimal.valueOf(cartData.getTotalPriceWithTax().getValue().doubleValue());
        BigDecimal orderAmountCents     = totalAmount.multiply(BigDecimal.valueOf(100));
        Integer amount        = orderAmountCents.setScale(2, BigDecimal.ROUND_HALF_EVEN).intValueExact();
        return amount.intValue();
    }
    
    /**
     * Is Guest User.
     *
     * @return guestEmail
     */
    public String getGuestEmail() {
        final CartModel cart = cartService.getSessionCart();
        final UserModel user = cart.getUser();
        return (user instanceof CustomerModel && ((CustomerModel) user).getType() == CustomerType.GUEST) ? user.getUid().substring(user.getUid().indexOf('|') + 1) : null;
    }
    
    public OrderData placeOrder(String response) throws InvalidCartException {
        
        PaymentModeModel paymentModeModel              = paymentModeService.getPaymentModeForCode("novalnet");
        NovalnetPaymentModeModel novalnetPaymentMethod = (NovalnetPaymentModeModel) paymentModeModel;
        
        final CartData cartData      = getCheckoutFacade().getCheckoutCart();
        final String currency        = cartData.getTotalPriceWithTax().getCurrencyIso();
        String paymentData           = getSessionService().getAttribute("novalnetPaymentResponse");
        String newLine               = " <br/> ";
        String bankDetails           = "";
        Integer orderAmountCent      = getOrderAmount(cartData);
        AddressModel billingAddress =  new AddressModel();
        final AddressData addressData;
        
        final Map<String, Object> customerParameters = getSessionService().getAttribute("novalnetAddressData");
        
        JSONObject responseJson                                            = new JSONObject(response);
        JSONObject paymentDataJson                                         = new JSONObject(paymentData);
        JSONObject customerJson                                       	   = responseJson.getJSONObject("customer");
        JSONObject billingJson                                       	   = customerJson.getJSONObject("billing");
        NovalnetPaymentInfoModel paymentInfoModel                          = new NovalnetPaymentInfoModel();
        
        final List<PaymentTransactionEntryModel> paymentTransactionEntries = new ArrayList<>();
        
        if (Boolean.TRUE.equals(getSessionService().getAttribute("isUseDeliveryAddress")))  {
			addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
		} else {
			addressData    = new AddressData();
			billingAddress =  getModelService().create(AddressModel.class);
			addressData.setFirstName(customerJson.get("first_name").toString());
            addressData.setLastName(customerJson.get("last_name").toString());
            addressData.setLine1(billingJson.get("street").toString());
            addressData.setTown(billingJson.get("city").toString());
            addressData.setPostalCode(billingJson.get("zip").toString());
            addressData.setCountry(getI18NFacade().getCountryForIsocode(billingJson.get("country_code").toString()));
		}
		
		getSessionService().setAttribute("novalnetAddressData", addressData);
		
		final CartModel cartModel      = getCart();
        final UserModel currentUser    = getCurrentUserForCheckout();
        final BaseStoreModel baseStore = this.getBaseStoreModel();
        String guestEmail              = getGuestEmail();
        final String email             = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
		
		billingAddress                 = addressReverseConverter.convert(addressData, billingAddress);
        billingAddress.setEmail(email);
        billingAddress.setOwner(cartModel);
        
        if (Boolean.FALSE.equals(getSessionService().getAttribute("isUseDeliveryAddress")))  {
			System.out.println("---------inside save ======================");
			getModelService().save(billingAddress);
		}
        
        
        
        final Locale language     = JaloSession.getCurrentSession().getSessionContext().getLocale();
        final String languageCode = language.toString().toUpperCase();
        
        JSONObject paymentDetailsJson = paymentDataJson.getJSONObject("payment_details");
        JSONObject transactionJson    = responseJson.getJSONObject("transaction");
        
        String testMode    = (transactionJson.get("test_mode").toString().equals("1")) ? Localization.getLocalizedString("novalnet.testOrderText") : "";
        String paymentname = paymentDetailsJson.get("type").toString();
        
        if(transactionJson.has("bank_details")) {
            bankDetails += "";
            JSONObject bankdeatailsJson = transactionJson.getJSONObject("bank_details");
            bankDetails += newLine + String.format(Localization.getLocalizedString("novalnet.bankDetailsComments1"), cartData.getTotalPriceWithTax().getFormattedValue());
            
            if (transactionJson.has("due_date")  && !"ON_HOLD".equals(transactionJson.get("status").toString())) {
                bankDetails += " " +  String.format(Localization.getLocalizedString("novalnet.bankDetailsComments2"), transactionJson.get("due_date").toString());
            }
            
            bankDetails += newLine + Localization.getLocalizedString("novalnet.bankDetailsAccountHolder") + " " + bankdeatailsJson.get("account_holder").toString();
            bankDetails += newLine + Localization.getLocalizedString("novalnet.bankDetailsBank") + " " + bankdeatailsJson.get("bank_name").toString()+ newLine;
            bankDetails += Localization.getLocalizedString("novalnet.bankPlace") + " " + bankdeatailsJson.get("bank_place").toString();
            bankDetails += newLine + Localization.getLocalizedString("novalnet.bankDetailsIban") + " " + bankdeatailsJson.get("iban").toString();
            bankDetails += newLine + Localization.getLocalizedString("novalnet.bankDetailsBic") + " " + bankdeatailsJson.get("bic").toString();

            bankDetails += newLine + Localization.getLocalizedString("novalnet.bankDetailspaymentRefernceMulti") + newLine + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : TID  " + transactionJson.get("tid").toString() + newLine;
        }
        
        if(transactionJson.has("partner_payment_reference")) {
            bankDetails += "<br>" + Localization.getLocalizedString("novalnet.multibancocomments1") + " " + cartData.getTotalPriceWithTax().getFormattedValue() + " " + Localization.getLocalizedString("novalnet.multibancocomments2") + "<br>" + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : " + transactionJson.get("partner_payment_reference").toString();
        }
        
        if(transactionJson.has("nearest_stores")) {
            JSONObject storeJson = transactionJson.getJSONObject("nearest_stores");
            Iterator<String> keys = storeJson.keys();
            bankDetails += "<br><br>" + Localization.getLocalizedString("novalnet.neareststores");
            bankDetails += newLine + Localization.getLocalizedString("novalnet.slipexpiry") + " " + transactionJson.get("due_date").toString();

            while (keys.hasNext()) {

                String key = keys.next();
                if (storeJson.get(key) instanceof JSONObject) {
                    JSONObject nearestStoreJson = storeJson.getJSONObject(key);

                    bankDetails += newLine + nearestStoreJson.get("store_name");
                    bankDetails += newLine + nearestStoreJson.get("street");
                    bankDetails += newLine + nearestStoreJson.get("city");
                    bankDetails += newLine + nearestStoreJson.get("zip");
                    bankDetails += newLine + nearestStoreJson.get("country_code") + newLine;
                }
            }
        } 
        
       
        
        String comments = Localization.getLocalizedString("novalnet.paymentname")+ " : " + paymentname + newLine + "Novalnet Transaction ID : " + transactionJson.get("tid").toString();
        
        String orderComments = comments + newLine + testMode + bankDetails;
        
        getSessionService().setAttribute("novalnetOrderComments", orderComments);
        
        
		
        paymentInfoModel.setBillingAddress(billingAddress);
        paymentInfoModel.setPaymentEmailAddress(email);
        paymentInfoModel.setDuplicate(Boolean.FALSE);
        paymentInfoModel.setSaved(Boolean.TRUE);
        paymentInfoModel.setUser(currentUser);
        paymentInfoModel.setPaymentInfo(comments);
        paymentInfoModel.setOrderHistoryNotes(orderComments);
        paymentInfoModel.setPaymentProvider("Novalnet");
        paymentInfoModel.setPaymentGatewayStatus(transactionJson.get("status").toString());
        cartModel.setPaymentInfo(paymentInfoModel);
        paymentInfoModel.setCode("");
        
        PaymentTransactionEntryModel orderTransactionEntry = new PaymentTransactionEntryModel();
        orderTransactionEntry = createTransactionEntry(transactionJson.get("tid").toString(), cartModel, orderAmountCent, comments, currency);
        
        
        paymentTransactionEntries.add(orderTransactionEntry);

        // Initiate/ Update PaymentTransactionModel
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentProvider("Novalnet");
        paymentTransactionModel.setRequestId(transactionJson.get("tid").toString());
        paymentTransactionModel.setEntries(paymentTransactionEntries);
        paymentTransactionModel.setOrder(cartModel);
        
        cartModel.setPaymentTransactions(Arrays.asList(paymentTransactionModel));
        getModelService().save(cartModel);

        final OrderData orderData = getCheckoutFacade().placeOrder();
        
        String orderNumber = orderData.getCode();
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderNumber);
        
        paymentInfoModel.setCode(orderNumber);
        getModelService().save(paymentInfoModel);
        
        
        OrderModel orderModel = getModelService().get(orderInfoModel.get(0).getPk());
        orderModel.setPaymentMode(novalnetPaymentMethod);
        orderModel.setStatusInfo(Localization.getLocalizedString("novalnet.paymentname")+ " : " + comments); 
        orderModel.setPaymentInfo(paymentInfoModel);
        
        OrderHistoryEntryModel orderEntry = getModelService().create(OrderHistoryEntryModel.class);
        orderEntry.setTimestamp(new Date());
        orderEntry.setOrder(orderModel);
        orderEntry.setDescription("Novalnet Transaction ID : " + transactionJson.get("tid").toString());
        
        paymentTransactionModel.setInfo(paymentInfoModel);
        
        getModelService().saveAll(orderModel, orderEntry);
        updateOrderStatus(orderNumber, paymentInfoModel);
        
        createTransactionUpdate(transactionJson.get("tid").toString(), orderNumber, languageCode);

        long callbackInfoTid = Long.parseLong(transactionJson.get("tid").toString());
        int  orderPaidAmount = 0;

        String[] pendingStatusCode = {"PENDING"};

        // Check for payment pending payments
        if (Arrays.asList(pendingStatusCode).contains(transactionJson.get("status").toString())) {
            orderPaidAmount = 0;
        } else {
            orderPaidAmount = orderAmountCent;
        }

        NovalnetCallbackInfoModel novalnetCallbackInfo = new NovalnetCallbackInfoModel();
        novalnetCallbackInfo.setPaymentType(transactionJson.get("payment_type").toString());
        novalnetCallbackInfo.setOrderAmount(orderAmountCent);
        novalnetCallbackInfo.setCallbackTid(callbackInfoTid);
        novalnetCallbackInfo.setOrginalTid(callbackInfoTid);
        novalnetCallbackInfo.setPaidAmount(orderPaidAmount);
        novalnetCallbackInfo.setOrderNo(orderNumber);
        getModelService().save(novalnetCallbackInfo);

        return orderData;
    }
    
    public void createTransactionUpdate(String tid, String orderNumber, String languageCode) {

        Gson gson = new GsonBuilder().create();

        final Map<String, Object> transactionParameters = new HashMap<String, Object>();
        final Map<String, Object> customParameters      = new HashMap<String, Object>();
        final Map<String, Object> dataParameters        = new HashMap<String, Object>();

        transactionParameters.put("tid", tid);
        transactionParameters.put("order_no", orderNumber);
        customParameters.put("lang", languageCode);
        dataParameters.put("transaction", transactionParameters);
        dataParameters.put("custom", customParameters);
        String jsonString = gson.toJson(dataParameters);
        String url = "https://payport.novalnet.de/v2/transaction/update";
        sendRequest(url, jsonString);
    }

}
