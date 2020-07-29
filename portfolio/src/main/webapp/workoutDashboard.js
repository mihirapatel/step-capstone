var userId;
var userName;
var userEmail;


/** Creates Workout Dashboard button on assistant main page that links to dashboard page*/
function createWorkoutDashboardButton(){
    var dashboardDiv = document.getElementsByClassName("workout-dashboard-link")[0];
    var dashboardLink = document.createElement("a");
    dashboardLink.title = "Workout Dashboard";
    dashboardLink.innerHTML = "Workout Dashboard"
    dashboardLink.href = "dashboard.html";
    dashboardLink.target= "_blank";
    dashboardDiv.appendChild(dashboardLink);

}

/** Call all functions to get data from backend and display it*/
function dashboardSetup() {

    getUserInfo();
    
}

/** Get user info from servlet and call displayUserInfo()*/
function getUserInfo() {
    fetch('/workout-user-profile').then(response => response.text()).then((info) => {
        var infoJson = JSON.parse(info);
        userId = infoJson.userId;
        userName = infoJson.userName;
        userEmail = infoJson.userEmail;
        
        displayUserInfo();
    });
}


/** Displays user info on top of dashboard */
function displayUserInfo() {

    userInfoDiv = document.getElementsByClassName("user-info")[0];    

    //User profile picture
    var userPic = document.createElement("img");
    userPic.className = "user-pic";
    userPic.src = "https://icon-library.com/images/default-user-icon/default-user-icon-4.jpg";
    userInfoDiv.appendChild(userPic);

    //User contact info
    var userContact = document.createElement("div");
    userContact.className = "user-contact";
    var name = document.createElement("h3");
    name.innerHTML = userName;
    userContact.appendChild(name);
    var email = document.createElement("p");
    email.innerHTML = userEmail;
    userContact.appendChild(email);
    userInfoDiv.appendChild(userContact);
    dashboardContainer.appendChild(dashboardLink);
}




