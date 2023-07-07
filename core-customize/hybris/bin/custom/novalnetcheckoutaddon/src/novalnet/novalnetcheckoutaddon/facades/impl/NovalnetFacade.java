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
package novalnet.novalnetcheckoutaddon.facades;

import java.lang.*;
import java.io.*;

import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.annotation.Resource;
import java.math.BigDecimal;

import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;

import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;

import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.PaymentModeService;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentRefInfoModel;
//~ import de.hybris.novalnet.core.model.NovalnetDirectDebitSepaPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetGuaranteedDirectDebitSepaPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetGuaranteedInvoicePaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetPayPalPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetCreditCardPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetInvoicePaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetPrepaymentPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetBarzahlenPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetInstantBankTransferPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetOnlineBankTransferPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetBancontactPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetMultibancoPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetIdealPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetEpsPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetGiropayPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetPrzelewy24PaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetPostFinanceCardPaymentModeModel;
//~ import de.hybris.novalnet.core.model.NovalnetPostFinancePaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetCallbackInfoModel;

import de.hybris.platform.core.model.order.payment.PaymentModeModel;

import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import java.nio.charset.StandardCharsets;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Base64;

/**
 * NovalnetFacade
 */
public class NovalnetFacade extends DefaultAcceleratorCheckoutFacade {
	
	 private static final Logger LOGGER = Logger.getLogger(NovalnetFacade.class);

    @Resource(name = "cartService")
    private CartService cartService;

    @Resource
    private PaymentModeService paymentModeService;

    private FlexibleSearchService flexibleSearchService;

    @Resource
    private Converter<AddressData, AddressModel> addressReverseConverter;


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
     * Send request
     *
     * @param url to which the requst to be sent
     * @param jsonString the string to be passed in the request
     * @return response
     */
    public StringBuilder sendRequest(String url, String jsonString) {
        final BaseStoreModel baseStore = this.getBaseStoreModel();
        String password = baseStore.getNovalnetPaymentAccessKey().trim();
        StringBuilder response = new StringBuilder();

        try {
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
            LOGGER.error("MalformedURLException ", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException ", ex);
        }

        return response;

    }

    /**
     * Insert Payment Reference details
     *
     * @param response       Response of the transaction
     * @param customerNo     Customer ID
     * @param currentPayment Current payment code
     */
   
	
	/**
     * Save data in database
     *
     * @param billingAddress billing address of teh order
     * @param cartModel cart details of the order
     */
    public void saveData(AddressModel billingAddress, final CartModel cartModel) {
        this.getModelService().saveAll(billingAddress, cartModel);
    }
    
    /**
     * Save data in database
     *
     * @param orderCode Order code of the order
     * @return orderModel
     */
    public OrderModel getOrder(String orderCode) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);

        // Update OrderHistoryEntries
        OrderModel orderModel = this.getModelService().get(orderInfoModel.get(0).getPk());
        
        return orderModel;
    }
	
    /**
     * Get Basestore Model
     *
     * @return Basestore configuration
     */
    public BaseStoreModel getBaseStoreModel() {
        return getBaseStoreService().getCurrentBaseStore();
    }

	 /**
     * Create transaction entry
     *
     * @param requestId request id 
     * @param cartModel cart details
     * @param amount order amount
     * @param backendTransactionComments transaction comments
     * @param currencyCode order currency code
     * @return paymentTransactionEntry
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

	/**
     * Get Current user
     *
     * @return currentUser
     */
    public UserModel getCurrentUser() {
        final UserModel currentUser = getCurrentUserForCheckout();

        return currentUser;
    }
	
	/**
     * Get Billing address
     *
     * @return billingAddress
     */
    public AddressModel getBillingAddress() {
        AddressModel billingAddress = this.getModelService().create(AddressModel.class);
        return billingAddress;
    }

	/**
     * Get Billing address
     *
     * @return billingAddress
     */
    public CartModel getNovalnetCheckoutCart() {
        final CartModel cartModel = getCart();
        return cartModel;
    }

    /**
     * Get stored payment info model
     *
     * @param customerNo Customer Id of the transaction
     * @param paymentType payment type
     * @return SearchResult
     */
    public List<NovalnetPaymentRefInfoModel> getPaymentRefInfo(String customerNo, String paymentType) {
        // Initialize StringBuilder
        long customerId = Long.parseLong(customerNo);
        StringBuilder countQuery = new StringBuilder();
        countQuery.append("SELECT {pk} from {" + NovalnetPaymentRefInfoModel._TYPECODE + "} where {" + NovalnetPaymentRefInfoModel.CUSTOMERNO
                + "} = ?customerNo AND {" + NovalnetPaymentRefInfoModel.PAYMENTTYPE + "} = ?paymentType");
        FlexibleSearchQuery executeCountQuery = new FlexibleSearchQuery(countQuery.toString());
        executeCountQuery.addQueryParameter("paymentType", paymentType);
        executeCountQuery.addQueryParameter("customerNo", customerId);
        SearchResult<NovalnetPaymentRefInfoModel> countResult = getFlexibleSearchService().search(executeCountQuery);
        
        final List<NovalnetPaymentRefInfoModel> countPaymentInfo = countResult.getResult(); 
        
        StringBuilder query = new StringBuilder();

        // Select query for fetch NovalnetPaymentRefInfoModel
        query.append("SELECT {pk} from {" + NovalnetPaymentRefInfoModel._TYPECODE + "} where {" + NovalnetPaymentRefInfoModel.CUSTOMERNO
                + "} = ?customerNo AND {" + NovalnetPaymentRefInfoModel.PAYMENTTYPE + "} = ?paymentType ORDER BY {creationtime} DESC");
                
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(query.toString());

        // Add query parameter
        executeQuery.addQueryParameter("customerNo", customerId);
        executeQuery.addQueryParameter("paymentType", paymentType);

        // Execute query
        SearchResult<NovalnetPaymentRefInfoModel> result = getFlexibleSearchService().search(executeQuery);

        final List<NovalnetPaymentRefInfoModel> paymentInfo = result.getResult();
        return paymentInfo;
    }


    /**
     * @return the flexibleSearchService
     */
    @SuppressWarnings("javadoc")
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    private CurrencyModel getCurrencyForIsoCode(final String currencyIsoCode) {
        CurrencyModel currencyModel = new CurrencyModel();
        currencyModel.setIsocode(currencyIsoCode);
        currencyModel = flexibleSearchService.getModelByExample(currencyModel);
        return currencyModel;
    }

    /**
     * @param flexibleSearchService the flexibleSearchService to set
     */
    @SuppressWarnings("javadoc")
    public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
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
     * Is Guest User.
     *
     * @return Boolean
     */
    public Boolean isGuestUser() {
        final CartModel cart = cartService.getSessionCart();
        final UserModel user = cart.getUser();
        return (user instanceof CustomerModel && ((CustomerModel) user).getType() == CustomerType.GUEST) ? true : false;
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
     * Get guest Email adsress
     *
     * @return Email address
     */
    public String getGuestEmail() {
        final CartModel cart = cartService.getSessionCart();
        final UserModel user = cart.getUser();
        return (user instanceof CustomerModel && ((CustomerModel) user).getType() == CustomerType.GUEST) ? user.getUid().substring(user.getUid().indexOf('|') + 1) : null;
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
}
