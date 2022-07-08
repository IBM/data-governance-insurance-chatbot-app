# Work in progress
# Digital decoupling and data sharing between Insurance portal and Insurance chatbot

Data Security & Privacy related laws and regulations have become more stringent and at the same time businesses are expected to open for ecosystem partners. This makes data governance very critical to avoid litigation, loss of competitive position and trust. 

A collaborating application from within the enterprise or an ecosystem partner could require access to a data source for both read and write operations. A read operation must hence mask sensitive data  such as name, location, contact details, date of birth, credit card number, financial details and more. A data governance framework plays a critical role to enforce security and privacy at the same time be an enabler for business to achieve their strategy. 

Let us consider the following scenario for this code pattern. There is an insurance portal application where a customer can register, sign in, purchase a policy, view policy details and surrender a policy. Currently all this functionality is available on a web portal. Chatbots are becoming increasingly common these days. It is a very convenient, and also a preferred way for customers to interact with businesses. It was decided to build a chatbot application that will co-exist with the web portal with the following features:
- Register as a user 
- Sign in to the chatbot
- Buy an insurance policy
- View all policy details
- Surrender a policy

The web portal application owns the data and is responsible for enforcing the data policies. Now, it can be seen here that the Chatbot needs to having the following capabilities:
- Read access to data from the web portal application with sesitive data masked.
- Write access to data with data policies enforced.
- Authenticate users of the chatbot.

In this code pattern, you will learn how to:
- Set up data assets for governance in the Watson Knowledge Catalog
- Create data categories, classes, business terms and data protection rules for the data assets
- Create virtualized view of the data on Watson Query with data policies enforced
- Create a chatbot aapplication using Watson Assistant that invokes APIs exposed by the Web portal application for writing data to the data source, and consumes the read-only data with sensitive information masked from Watson Query.

Security Verify has been used to implement authentication for the Chatbot application.

![architecture](images/architecture.png)

## Flow

1. Create tables in Db2. The Db2 connection and the tables(as `Data Asset`) are added to the `Watson Knowledge Catalog(WKC)`. The data policies are configured for the data assets in `WKC`.
2. Db2 is added as a data source in Watson Query. The needed tables are virtualized and a `View` is created by joining the virtualized tables. 
3. The Watson Query virtualized tables and view are published to `WKC`. The data policies are configured for the data assets in `WKC`.
4. User accesses the chatbot. User is provided the option to `Register as a user` or `Login`.
5. In case of a new user, User is provided a web url for registration. In case of existing user, the User is authenticated using a one-time passcode sent to the user's email address.
6. User accesses the registration link hosted on the `Portal Svc`. User fills up the registration form with details.
7. A new user is created in Security Verify, and a record is added in Db2 table with other customer details.
8. The user after a successful authentication of one-time passcode can perform the following operations that involves a write operation to the data source - `Buy a Policy` or `Surrender a Policy`. The `Portal Svc` APIs for the operation is invoked. The `Portal Svc` validates the request with `Security Verify` using `Token Introspection`.
9. The `Portal Svc` then writes to the Db2 database with the data policies applied for the invoked operations.
10. The response from the `Portal Svc` is returned to `Watson Assistant`, `Chatbot Svc` and eventually to the end user accessing the chatbot interface.
11. The user requests to `View Active policies` or `View All policies`.  Since this a read operation, the request goes to `Chatbot Svc`. The `Chatbot Svc` validates the request with `Security Verify` using `Token Introspection`. 
12. The `Chatbot Svc` accesses `Watson Query` to get the results. The data policies are applied to mask sensitive data in the results.
13. All responses are sent back to the user on the chatbot interface.
 

## Pre-requisites

* IBM Cloud account
* OpenShift Cluster
* IBM Security Verify account
* Git client

## Steps

1. [Clone the repository](#1-clone-the-repository)
2. [Create IBM Cloud Services](#2-create-ibm-cloud-services)
3. [Configure Security Verify](#3-configure-security-verify)
4. [Create Cloud Functions Action](#4-create-cloud-functions-action)
5. [Setup Watson Assistant Chatbot](#5-setup-watson-assistant-chatbot)
6. [Deploy Insurance Portal Application](#6-deploy-insurance-portal-application)
7. [Configure Watson Query and Watson Knowloedge Studio]
8. [Access the Application](#8-access-the-application)

### 1. Clone the repository

From a command terminal, run the below command to clone the repo:

```
git clone https://github.com/IBM/data-governance-insurance-chatbot-app
```
### 2. Create IBM Cloud Services

#### 2.1 Create DB2, Watson Knowledge Catalog and Watson Query service instances on Cloud Pak for Data

In the code pattern, we will be using Cloud Pak for Data.

[Cloud Pak For Data](https://cloud.ibm.com/cloudpaks/data/overview) is available in two modes -
- [Fully-managed service](https://cloud.ibm.com/cloudpaks/data/overview) 
- [Self-managed software](https://cloud.ibm.com/catalog/content/ibm-cp-datacore-6825cc5d-dbf8-4ba2-ad98-690e6f221701-global)

##### 2.1.1 For fully managed service, click [here](CPDaaS.md) and follow the steps.

##### 2.1.2 For self managed software, click [here](CPD.md) and follow the steps.

##### 2.1.3 Create a catalog in Watson Knowledge Studio for Insurance Portal Application

Go to the Watson Knowledge Studio console. Select `View All Catalogs` on the hamburger menu on the top left.

Click on `Create Catalog`.

![click_create_catalog](images/click_create_catalog.png)

Enter a name for the catalog (say `InsClCatalog`). Enter a description. Select `Enforce data policies`. Click `Create`.

![create_catalog](images/create_catalog.png)

#### 2.2 Sign up for IBM Security Verify

Click [Security Verify](https://www.ibm.com/account/reg/signup?formid=urx-30041) to sign up for Security Verify. After you sign up for an account, the account URL (https://[tenant name].verify.ibm.com/ui/admin) and password is sent in an email.

#### 2.3 Create an OpenShift cluster to deploy applications

>Note: If you are using a Cloud Pak For Data as a self managed software, the same cluster can be used for application deployment.

Go to this [link](https://cloud.ibm.com/kubernetes/catalog/create?platformType=openshift) to create an instance of OpenShift cluster.

Make a note of the `Ingress Subdomain URL`:
![ingress](images/ingress_subdomain.png)

### 3. Configure Security Verify

Please follow the instructions [here](SECURITY_VERIFY_CONFIG.md) to configure Security Verify.

### 4. Create Cloud Functions Action

Login to your IBM Cloud account. On the dashboard, click on the hamburger menu and navigate to `Functions` and click on `Actions`.

Click the `Create` button to create a new action. 
Enter a name for action under `Action Name`. Leave `Enclosing Package` as `(Default Package)` itself. Under `Runtime` select option for Node.js.

Click on `Create` button. You are presented with actions code editor. Replace the existing code with the javascript code [here](https://github.com/IBM/secure-chatbot-interactions-using-security-verify/blob/main/sources/chatbot/cloud-function/function.js).

Next, in the javascript code, update the value of following variables (mentioned in the beginning of the file):

```
//Security Verify Details
var tenant_url = "xxxx.verify.ibm.com"
var client_id = "xxxx"
var client_secret = "xxxx"

//API Details
var REGISTRATION_API_URL = "http://<openshift_url>/ins/portalsvc/register";
var BUY_POLICY_API_URL = "http://<openshift_url>/ins/portalsvc/createpolicy";
var SURRENDER_POLICY_API_URL = "http://<openshift_url>/ins/portalsvc/surrpolicy";
var VIEW_ACTIVE_POLICIES_API_URL = "http://<openshift_url>/ins/chatbotsvc/getallactivepolicies";
var VIEW_ALL_POLICIES_API_URL = "http://<openshift_url>/ins/chatbotsvc/getallpolicies";
```

>Note: Please use the security verify credentials noted in step 3 and replace `openshift_url` by the `OpenShift ingress subdomain url` as noted in step 2.4.

Click `Save` button on the top right of the code editor. 

#### Enable cloud function action as web action

For the action just created, click `Endpoints` on the left side navigation menu. Select the checkbox `Enable as Web Action`. Click the `Save` button on the right top corner. When saved, `Copy web action url` icon, under `Web Action` section is enabled. Click the icon to copy the webhook url. This URL will be used in Watson Assistant for it to call the actions in Cloud Functions.

![Webhook URL](images/action-url.png)

### 5. Setup Watson Assistant Chatbot

Login to IBM Cloud. On the dashboard, click on the hamburger menu and click `Resource List`. Click on the Watson Assistant instance that you created earlier. Then click on `Launch Watson Assistant` button to launch Watson Assistant dashboard.

- In the Watson Assistant home page, click `Create New +` option on the top panel.
- Provide a name of your choice, say `InsuranceBot` and click `Create assistant`.
- Navigate to `Assistant Settings` in the left panel towards down. Under the Dialog section, click on `Activate Dialog`. Now, Dialog will be visible as one of the options in the left panel.
- Click on `Dialog > Options > Upload/Download` and provide a json file available at `<cloned repo>/sources/chatbot/dialog/`. Click `Upload`.
- On the left navigation links click `Options > Webhooks` and in `URL` text field, enter the REST API endpoint as noted in step 4 and append it with .json.  It should look something like this
	```
	https://eu-gb.functions.appdomain.cloud/api/v1/web/.../default/sample.json
	```
	Now the chatbot is ready to use.
- On the left panel, click the `Preview` button. 
- Click on `Customize web chat` button presented in top-right corner. Here you can make changes as per your choice.
- The following changes were made for this code pattern:
	- Under `Home Screen` tab, toggle a button to set it `off`.
	- Click on `Save and Exit`.
- Now, chatbot can be used in this preview window. 
- Optional: If you wish to embed this chatbot onto your portal, go to `Preview > Customize web chat > Embed (tab)`. It shows a code snippet like:
	```
	<script>
	  window.watsonAssistantChatOptions = {
	    integrationID: "cxxx0", // The ID of this integration.
	    region: "us-south", // The region your integration is hosted in.
	    serviceInstanceID: "fexxxa", // The ID of your service instance.
	    onLoad: function(instance) { instance.render(); }
	  };
	  setTimeout(function(){
	    const t=document.createElement('script');
	    t.src="https://web-chat.global.assistant.watson.appdomain.cloud/versions/" + (window.watsonAssistantChatOptions.clientVersion || 'latest') + "/WatsonAssistantChatEntry.js";
	    document.head.appendChild(t);
	  });
	</script>
	```

	Copy this code and paste on the homepage of the portal application. The chatbot will be integrated with your portal.

Note that this code pattern uses the preview option to ease the process.

### 6. Deploy the application


## Summary

## License
This code pattern is licensed under the Apache License, Version 2. Separate third-party code objects invoked within this code pattern are licensed by their respective providers pursuant to their own separate licenses. Contributions are subject to the [Developer Certificate of Origin, Version 1.1](https://developercertificate.org/) and the [Apache License, Version 2](https://www.apache.org/licenses/LICENSE-2.0.txt).

[Apache License FAQ](https://www.apache.org/foundation/license-faq.html#WhatDoesItMEAN)
