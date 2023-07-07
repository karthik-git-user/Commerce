/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
 
let novalnetpaymentForm;

var send_call = 1;

$(document).ready(function ()
{
	if($('.order-payment-data').find('.value-order') != undefined) {
        if($('.order-payment-data').find('.value-order').html() != undefined) {
            var orderCommernts = $('.order-payment-data').find('.value-order').html();
            $('.order-payment-data').find('.value-order').html("<span class='novalnet-transaction-comments' >"+orderCommernts.replace(/&lt;br&gt;/g, "</span>&nbsp;<span class='novalnet-transaction-comments'>")+"</span>");
        }
    }
    
    if($('.order-billing-address').length && $(".novalnetaddressdata").length) {
		if($('.order-billing-address').children('.value-order').length) {
			$('.order-billing-address').children('.value-order').replaceWith($(".novalnetaddressdata").html());
		} else {
			$('.order-billing-address').append("<div class = value-order>"+$(".novalnetaddressdata").html()+"</div>")
		}
	}
    
    $('.submit_novalnetPaymentDetailsForm').attr('onclick', 'getNnPaymentData()');

	
	$('#paymentFormIframe').on('load', function(){
		loadPaymentForm();
	});


	$(document).on('change','input[name="billTo_firstName"], input[name="billTo_lastName"], input[name="billTo_street1"], input[name="billTo_street2"], input[name="billTo_city"], input[name="billTo_postalCode"]',function(){

		if($('input[name="billTo_street1"]').val() != "" && $('input[name="billTo_city"]').val() != "" && $('input[name="billTo_postalCode"]') != "") {
			let updatedData = {
				billing_address: {
					street: $('input[name="billTo_street1"]').val() + " " + $('input[name="billTo_street2"]').val(),
					city: $('input[name="billTo_city"]').val(),
					zip: $('input[name="billTo_postalCode"]').val(),
					country_code: $('select[name="billTo_country"]').val()
				},
				shipping_address: {
					street: $('#useDeliveryAddressData').attr('data-line1') + " " + $('#useDeliveryAddressData').attr('data-line2'),
					city: $('#useDeliveryAddressData').attr('data-town'),
					zip: $('#useDeliveryAddressData').attr('data-postalcode'),
					country_code: $('#useDeliveryAddressData').attr('data-countryisocode')
				},
			};
			console.log(updatedData);
			novalnetpaymentForm.updateForm(updatedData, (data) => {});
		}
		
	});
	
	$('#useDeliveryAddress').change(function() {
		if(this.checked) {
			let updatedData = {
				billing_address: {
					street: $('#useDeliveryAddressData').attr('data-line1') + " " + $('#useDeliveryAddressData').attr('data-line2'),
					city: $('#useDeliveryAddressData').attr('data-town'),
					zip: $('#useDeliveryAddressData').attr('data-postalcode'),
					country_code: $('#useDeliveryAddressData').attr('data-countryisocode')
				},
				same_as_billing : 1,
			};
			console.log(updatedData);
			novalnetpaymentForm.updateForm(updatedData, (data) => {});
		}
	});
	
    if($("#novalnetComments") != undefined && $(".orderBox.payment") != undefined && (parseFloat($("#novalnetComments").css("height")) > parseFloat( $(".orderBox.payment").css("height")))) {
        $(".orderBox.payment").css("height",
            parseFloat($(".orderBox.payment").css("height")) + parseFloat($("#novalnetComments").css("height"))
        );
    }
    
    if($('#cancelorderForm') != undefined) {
        $('#cancelorderForm').hide();
    }
    

    if($('.order-payment-data').find('.value-order') != undefined) {
        if($('.order-payment-data').find('.value-order').html() != undefined) {
            var orderCommernts = $('.order-payment-data').find('.value-order').html();
            $('.order-payment-data').find('.value-order').html("<span class='novalnet-transaction-comments' >"+orderCommernts.replace(/&lt;br&gt;/g, "</span>&nbsp;<span class='novalnet-transaction-comments'>")+"</span>");
        }
    }

    if($('#paygateurl').length) {
        window.open($('#paygateurl').val(), '_self');
    }


});

function getNnPaymentData()
{
	if($('.paymentMethods').length) {
		novalnetpaymentForm.getMPaymentRequest();
	} else {
		$('#paymentDetailsForm').submit();  
	}
}

function loadPaymentForm() {
	
	novalnetpaymentForm = new NovalnetPaymentForm();
	
	
	let paymentFormRequestObj = {
	  iframe : '#paymentFormIframe',
	  initForm: {
		styleText: {
			label : {
				type: 'normal',
				style :{
					'font-weight' : '300'
				}
			},
			paymentList : {
				padding: '10px 10px 10px 0px',
				background : '#FFFFFF'
			},
			paymentFieldContainer : {
				background : '#FFFFFF'
			},
		},
		uncheckPayments: true,
		showButton: false,
	  }
	};
	// initiate form
	novalnetpaymentForm.initiate(paymentFormRequestObj);
	
	novalnetpaymentForm.getMPaymentResponse( (response) => {
		document.getElementById("nnPaymentResponse").value = JSON.stringify(response);
		$('#paymentDetailsForm').submit();  
	});
	

	
	novalnetpaymentForm.walletResponse({
		onProcessCompletion: async (response) => {
			if (response.result.status == "SUCCESS") {
				document.getElementById("nnPaymentResponse").value = JSON.stringify(response);
			    $('#paymentDetailsForm').submit(); 
				return {status : 'SUCCESS', statusText : ''};
			} else {
				return {status : 'FAILURE', statusText : ''};
			}
		}
	});
}
