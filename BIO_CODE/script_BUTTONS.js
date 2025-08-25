// SCRIPT FOR BUTTONS //

document.addEventListener("DOMContentLoaded", function () 
{
    const themeSelector = document.getElementById("theme-selector");
    const themeLink = document.getElementById("theme-link");

    themeSelector.addEventListener("change", function () 
    {
        themeLink.setAttribute("href", this.value);
    });

});
