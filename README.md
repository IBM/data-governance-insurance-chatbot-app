# Digital decoupling and data sharing between Insurance portal and Insurance chatbot

## Flow

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

#### 2.1 Sign up for IBM Security Verify

Click [Security Verify](https://www.ibm.com/account/reg/signup?formid=urx-30041) to sign up for Security Verify. After you sign up for an account, the account URL (https://[tenant name].verify.ibm.com/ui/admin) and password is sent in an email.

#### 2.2 Create IBM DB2 Service instance

Login to IBM Cloud, in [IBM Cloud Catalog](https://cloud.ibm.com/catalog) search of DB2. Click on the Db2 tile.

Select an appropriate plan. Read and accept the license agreement. You may leave the other options to their default values. Click on `Create` button. It will take a minute or so to provision an instance.

**Make a note of service credentials**
- Click the newly created db2 entry in [IBM Cloud Resource list](https://cloud.ibm.com/resources)
- Click `Service credentials` on the left hand side navigation menu. If there are no credentials, then click on `New credential` button and create new credentials. 

![create_db2_credential](images/create_db2_credential.png)

**Note the username, password, host and port of the DB2 instance. The will be needed for configuring Insurance Portal Application, Watson Knowledge Catalog and Watson Query**

![note_credential](images/note_credential.png)

#### 2.3 Create Watson Assistant Service instance

* Login to IBM Cloud, in [IBM Cloud Catalog](https://cloud.ibm.com/catalog) search of Assistant and create a Watson Assistant service by selecting the **Lite** plan and clicking on **Create**.

* Click **Launch Watson Assistant** to launch console.

#### 2.4 Create an instance of OpenShift cluster

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
- Provide a name of your choice, say `InsuranceBot`
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
- Now, chatbot can be used using this preview option. 
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
