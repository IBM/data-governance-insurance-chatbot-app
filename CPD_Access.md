## Set up account for collaborating user and provide access to Watson Knowledge Catalog and Watson Query


### 1. Set up user
An account for the collaborating user which in our case is the chatbot application needs to be set up.

Go to `Access Control` on the Cloud Pak for Data Console.

![click_access_control](./images/click_access_control.png)

Click on `Add users`.

![start_add_user](./images/start_add_user.png)

Enter profile information and click `Next`.

![add_users_page](./images/add_users_page.png)

Select `Data Scientist` role since the user is needed for the chatbot application.

![add_users_page](./images/add_users_page2.png)

Click `Add` to add the user.

![add_users_complete](./images/add_users_complete.png)

### 2. Provide Access to Watson Knowledge Catalog and Watson Query

- Open the Watson Knowledge Catalog console. Open the new catalog `InsClCatalog`. Click on `Access` tab and provide collaborator access to the new user created.
- Open the Watson Query console. Select `User management` in the dropdown list menu. Click on `Grant access` and provide collaborator access to the new user created.
