/** Creates Workout Dashboard button */
function createWorkoutDashboardButton(){
    var dashboardContainer = document.getElementsByClassName("workout-dashboard-link")[0];

    var dashboardLink = document.createElement("a");
    dashboardLink.title = "Workout Dashboard";
    dashboardLink.innerHTML = "Workout Dashboard"
    dashboardLink.href = "dashboard.html";
    dashboardLink.target= "_blank";
    dashboardContainer.appendChild(dashboardLink);

}




