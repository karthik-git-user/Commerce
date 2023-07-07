/*
 * [y] hybris Platform
 *
 * Released under the GNU General Public License
 * This free contribution made by request.
 * If you have found this script useful a small
 * recommendation as well as a comment on merchant form
 * would be greatly appreciated.
 *
 *
 */
package novalnet.novalnetcheckoutaddon.forms;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;

import javax.validation.constraints.NotNull;
import java.util.Locale;

import java.util.Map;

/**
 * NovalnetPaymentDetailsForm
 */
public class NovalnetPaymentDetailsForm {
    
    private String nnPaymentResponse;

    private boolean newBillingAddress;

    private String paymentId;

    private String billToCountry;
    private Map<String, String> parameters;

    private String billTo_city; // NOSONAR
    private String billTo_country; // NOSONAR
    private String billTo_customerID; // NOSONAR
    private String billTo_email; // NOSONAR
    private String billTo_firstName; // NOSONAR
    private String billTo_lastName; // NOSONAR
    private String billTo_phoneNumber; // NOSONAR
    private String billTo_postalCode; // NOSONAR
    private String billTo_titleCode; // NOSONAR
    private String billTo_state; // NOSONAR
    private String billTo_street1; // NOSONAR
    private String billTo_street2; // NOSONAR

    private AddressForm billingAddress;
    private boolean useDeliveryAddress;

    /**
     * @return the newBillingAddress
     */
    public Boolean getNewBillingAddress() {
        return newBillingAddress;
    }

    /**
     * @param newBillingAddress the newBillingAddress to set
     */
    public void setNewBillingAddress(final Boolean newBillingAddress) {
        this.newBillingAddress = newBillingAddress;
    }


    public String getNnPaymentResponse() {
        return this.nnPaymentResponse;
    }

    public void setNnPaymentResponse(String nnPaymentResponse) {
        this.nnPaymentResponse = nnPaymentResponse;
    }

    public String getBillToCountry() {
        return billToCountry;
    }

    public void setBillToCountry(String billToCountry) {
        this.billToCountry = billToCountry;
    }
   
    /**
     * @return billingAddress
     */
    public AddressForm getBillingAddress() {
        return billingAddress;
    }

    /**
     * @param billingAddress the billingAddress to set
     */
    public void setBillingAddress(final AddressForm billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * @return useDeliveryAddress
     */
    public boolean isUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    /**
     * @param useDeliveryAddress the useDeliveryAddress to set
     */
    public void setUseDeliveryAddress(final boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }

    /**
     * @return billTo_city
     */
    public String getBillTo_city() // NOSONAR
    {
        return billTo_city;
    }

    /**
     * @param billTo_city the billTo_city to set
     */
    public void setBillTo_city(final String billTo_city) // NOSONAR
    {
        this.billTo_city = billTo_city;
    }

    /**
     * @return billTo_country
     */
    public String getBillTo_country() // NOSONAR
    {
        if (billTo_country != null) {
            return billTo_country.toUpperCase(Locale.US);
        }
        return billTo_country;
    }

    /**
     * @param setBillTo_country the BillTo_country to set
     */
    public void setBillTo_country(final String billTo_country) // NOSONAR
    {
        this.billTo_country = billTo_country;
    }

    /**
     * @return billTo_customerID
     */
    public String getBillTo_customerID() // NOSONAR
    {
        return billTo_customerID;
    }

    /**
     * @param billTo_customerID the billTo_customerID to set
     */
    public void setBillTo_customerID(final String billTo_customerID) // NOSONAR
    {
        this.billTo_customerID = billTo_customerID;
    }

    /**
     * @return billTo_email
     */
    public String getBillTo_email() // NOSONAR
    {
        return billTo_email;
    }

    /**
     * @param billTo_email the billTo_email to set
     */
    public void setBillTo_email(final String billTo_email) // NOSONAR
    {
        this.billTo_email = billTo_email;
    }

    /**
     * @return billTo_firstName
     */
    public String getBillTo_firstName() // NOSONAR
    {
        return billTo_firstName;
    }

    /**
     * @param billTo_firstName the billTo_firstName to set
     */
    public void setBillTo_firstName(final String billTo_firstName) // NOSONAR
    {
        this.billTo_firstName = billTo_firstName;
    }

    /**
     * @return billTo_lastName
     */
    public String getBillTo_lastName() // NOSONAR
    {
        return billTo_lastName;
    }

    /**
     * @param billTo_lastName the billTo_lastName to set
     */
    public void setBillTo_lastName(final String billTo_lastName) // NOSONAR
    {
        this.billTo_lastName = billTo_lastName;
    }

    /**
     * @return billTo_phoneNumber
     */
    public String getBillTo_phoneNumber() // NOSONAR
    {
        return billTo_phoneNumber;
    }

    /**
     * @param billTo_phoneNumber the billTo_phoneNumber to set
     */
    public void setBillTo_phoneNumber(final String billTo_phoneNumber) // NOSONAR
    {
        this.billTo_phoneNumber = billTo_phoneNumber;
    }

    /**
     * @return billTo_postalCode
     */
    public String getBillTo_postalCode() // NOSONAR
    {
        return billTo_postalCode;
    }

    /**
     * @param billTo_postalCode the billTo_postalCode to set
     */
    public void setBillTo_postalCode(final String billTo_postalCode) // NOSONAR
    {
        this.billTo_postalCode = billTo_postalCode;
    }

    /**
     * @return billTo_titleCode
     */
    public String getBillTo_titleCode() // NOSONAR
    {
        return billTo_titleCode;
    }

    /**
     * @param billTo_titleCode the billTo_titleCode to set
     */
    public void setBillTo_titleCode(final String billTo_titleCode) // NOSONAR
    {
        this.billTo_titleCode = billTo_titleCode;
    }

    /**
     * @return billTo_state
     */
    public String getBillTo_state() // NOSONAR
    {
        return billTo_state;
    }

    /**
     * @param billTo_state the billTo_state to set
     */
    public void setBillTo_state(final String billTo_state) // NOSONAR
    {
        this.billTo_state = billTo_state;
    }

    /**
     * @return billTo_street1
     */
    public String getBillTo_street1() // NOSONAR
    {
        return billTo_street1;
    }

    /**
     * @param billTo_street1 the billTo_street1 to set
     */
    public void setBillTo_street1(final String billTo_street1) // NOSONAR
    {
        this.billTo_street1 = billTo_street1;
    }

    /**
     * @return billTo_street2
     */
    public String getBillTo_street2() // NOSONAR
    {
        return billTo_street2;
    }

    /**
     * @param billTo_street2 the billTo_street2 to set
     */
    public void setBillTo_street2(final String billTo_street2) // NOSONAR
    {
        this.billTo_street2 = billTo_street2;
    }

}
