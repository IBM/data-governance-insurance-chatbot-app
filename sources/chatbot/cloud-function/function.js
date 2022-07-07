/**
  *
  * main() will be run when you invoke this action
  *
  * @param Cloud Functions actions accept a single parameter, which must be a JSON object.
  *
  * @return The output of this action, which must be a JSON object.
  *
  */
  //Pre-requisites
  var axios = require('axios');
  var qs = require('qs');

  //Security Verify Details
  var tenant_url = "xxx.verify.ibm.com";
  var client_id = 'bxxxb82';
  var client_secret = "xxxxt";
  
  //API URLS
  var REGISTRATION_API_URL = "http://<openshift_url>/ins/portalsvc/register";
  var BUY_POLICY_API_URL = "http://<openshift_url>/ins/portalsvc/createpolicy";
  var SURRENDER_POLICY_API_URL = "http://<openshift_url>/ins/portalsvc/surrpolicy";
  var VIEW_ACTIVE_POLICIES_API_URL = "http://<openshift_url>/ins/chatbotsvc/getallactivepolicies";
  var VIEW_ALL_POLICIES_API_URL = "http://<openshift_url>/ins/chatbotsvc/getallpolicies";
  
  
  async function main(params) {
      
      console.log("in main function");
      var request_type = params.request_type;
      var emailID = params.emailID;
      
      if (request_type === "verify_email") {
        var response = await get_access_token();
        console.log(response);
        let access_token = response.access_token;
        
        var response1 = await validate_email(access_token, emailID);
        console.log(response1.totalResults);

        if ( response1.totalResults > 0 ) {
          var otp_response = await email_otp(access_token, emailID);
          console.log(otp_response);
          return({"result":1, "message":"registered emailID", "access_token":access_token, "otp_txn_id":otp_response.id});
        } else {
          console.log ("Please provide a registered emailID");
          return({"result":0, "message":"Please provide a registered emailID"});
        }
      } else if ( request_type === "verify_otp") {
        console.log("verify_otp");
        let emailID = params.emailID;
        let access_token = params.access_token;
        let otp_txn_id = params.otp_txn_id;
        let otp = params.otp;
        
        if (!access_token){
            return({"verified_user":0, "message":"Authorization failed."})
        }
        var response = await verify_otp(access_token, emailID, otp_txn_id, otp);
        console.log(response);
        
        if ( response.message ) {
            return({"verify_status":0, "message":response.message});
        } else {
          return({"verify_status":1, "message":"Great! The code verification is successful."});
        }
    } else if ( request_type === "email_otp") {
        
        let access_token = params.access_token;
        var otp_response = await email_otp(access_token, emailID);
        console.log(otp_response);
        return({"result":1, "access_token":access_token, "otp_txn_id":otp_response.id});
        
    } else if ( request_type === "registration" ) {
        
        return({"registrationLink":REGISTRATION_API_URL});
        
    } else if ( request_type === "policies_history") {
        
        let access_token = params.access_token;
        var response = await view_all_policies (access_token, emailID);
        console.log(response);
        if (response === "Authorization Failed") {
            return({"verified_user":0, "message":"Authorization failed."});
        }
        if ( response.policies.length > 0 ){
            var output = await format_response(response.policies);
            return({"Response": output});
            //  return({"Response": response});
        } else {
            return({"Response": "No Policies"});
        }
        return({"Response": response});
        
    } else if ( request_type === "view_active_policies") {
        
        let access_token = params.access_token;
        var response = await view_active_policies (access_token, emailID);
        console.log(response);
        if (response === "Authorization Failed") {
            return({"verified_user":0, "message":"Authorization failed."});
        }
        if ( response.policies.length > 0 ){
            var output = await format_response(response.policies);
            return({"Response": output});
        } else {
            return({"Response": "No Active Policies"});
        }
        
    } else if ( request_type === "buy_policy") {
        
        let access_token = params.access_token;
        let insurance_cover = params.insurance_cover;
        let frequency = params.frequency;
        let policy_type = params.policy_type;
        let emailID = params.emailID;
        
        var response = await buy_policy (access_token, emailID, insurance_cover, frequency, policy_type);
        console.log(response);
        if (response === "Authorization Failed") {
            return({"verified_user":0, "message":"Authorization failed."});
        }
        return({"paymentLink":response.paymentLink, "premium":response.premium});
        
    } else if ( request_type === "surrender_policy") {
        
        let access_token = params.access_token;
        let policyID = params.policyID;
        var response = await surrender_policy (access_token, policyID);
        console.log(response);
        return({"Response": response.message});
        
    } else {
          return ({"message":"Unsupported request"});
      }
 }
 
 get_access_token = async () => {
    
  var data = {
         'grant_type': 'client_credentials',
         'client_id': client_id,
         'client_secret': client_secret 
    };
        
  var request = {
    method: 'post',
    url: 'https://' + tenant_url + '/oidc/endpoint/default/token',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    data : qs.stringify(data)
  };

  return new Promise(function (resolve, reject) {
    axios(request).then((response) => {
        var tokenData = response.data;
        access_token = tokenData.access_token;
        console.log(access_token);
        resolve(response.data);
    }).catch((error) => {
        console.log(error);
        reject(error);
    });
  });
}

validate_email = async (access_token, emailID) => {

  var config = {
    method: 'get',
    url: 'https://' + tenant_url + '/v2.0/Users?filter=emails.value eq "' + emailID + '"&attributes=emails',
    headers: {
      'Authorization': 'Bearer ' + access_token
    }
  };

  return new Promise(function (resolve, reject) {
    axios(config)
    .then( (response) => {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });
  })
}

email_otp = async (token, emailID) => {
  var data = JSON.stringify({
    "emailAddress": emailID,
    "correlation": "1234"
  });
  
  var config = {
    method: 'post',
    url: 'https://' + tenant_url + '/v2.0/factors/emailotp/transient/verifications',
    headers: { 
      'Content-Type': 'application/json', 
      'Authorization': 'Bearer ' + token,
    },
    data : data
  };
  
  return new Promise(function (resolve, reject) {
    axios(config)
    .then(function (response) {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });  
  })
}

verify_otp = async (token, emailID, txnID, otp) => {
  var data = JSON.stringify({
    "otp": otp
  });
  
  var config = {
    method: 'post',
    url: 'https://' + tenant_url + '/v2.0/factors/emailotp/transient/verifications/' + txnID,
    headers: { 
      'Content-Type': 'application/json', 
      'Authorization': 'Bearer ' + token
    },
    data : data
  };
  
  return new Promise(function (resolve, reject) {
      axios(config)
      .then(function (response) {
        console.log(JSON.stringify(response.data));
        resolve(response.data);
      })
      .catch(function (error) {
        console.log(error);
        reject(error);
      });  
  })
}

view_active_policies = async (access_token, emailID) => {
    console.log("in view_active_policies...");
    
    var config = {
        method: 'get',
        url: VIEW_ACTIVE_POLICIES_API_URL + '?emailid=' + emailID,
        headers: {
            'Content-Type': 'application/json', 
            'verify-token':  access_token
        }
  };

  return new Promise(function (resolve, reject) {
    axios(config)
    .then( (response) => {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });
  })
}

view_all_policies = async (access_token, emailID) => {
    console.log("in view_all_policies...");
    
    var config = {
        method: 'get',
        url: VIEW_ALL_POLICIES_API_URL + '?emailid=' + emailID,
        headers: {
            'Content-Type': 'application/json', 
            'verify-token':  access_token
        }
  };

  return new Promise(function (resolve, reject) {
    axios(config)
    .then( (response) => {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });
  })
}

buy_policy = async (access_token, emailID, insurance_cover, frequency, policy_type) => {
    console.log("In buy_policy...");
    
    var config = {
        method: 'get',
        url: BUY_POLICY_API_URL + '?emailid=' + emailID + '&policytype=' + policy_type + '&frequency=' + frequency + '&cover=' + insurance_cover,
        headers: {
            'Content-Type': 'application/json', 
            'verify-token':  access_token
        }
  };

  return new Promise(function (resolve, reject) {
    axios(config)
    .then( (response) => {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });
  })
}

surrender_policy = async (access_token, policyID) => {
    console.log("In surrender_policy...");
    var config = {
        method: 'get',
        url: SURRENDER_POLICY_API_URL + '?policyid=' + policyID,
        headers: {
            'Content-Type': 'application/json', 
            'verify-token':  access_token
        }
  };

  return new Promise(function (resolve, reject) {
    axios(config)
    .then( (response) => {
      console.log(JSON.stringify(response.data));
      resolve(response.data);
    })
    .catch(function (error) {
      console.log(error);
      reject(error);
    });
  })
}

format_response = async (policies) => {
    console.log("in format_response...");
    var output = [];
    for (let i=0; i<policies.length; i++) {
        let obj = {};
        obj = {
            "Policy ID": policies[i].policyId,
            "Policy Type":policies[i].policyDetails.PolicyType,
            "Insured Amount": policies[i].policyDetails.Cover,
            "Premium": policies[i].policyDetails.Premium,
            "Premium Payment Frequency": policies[i].policyDetails.Frequency,
            "Policy Date": policies[i].policyDate,
            "Credit Card Used": policies[i].creditCard,
            "Policy Status": policies[i].status
            }
        output.push(obj);
    }
    return (output);
}
