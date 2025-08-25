// SCRIPT FOR ELSE //

function loadCSS(filename) 
{
    let existingLink = document.getElementById('dynamicCSS');

    if (existingLink) 
    {
        existingLink.href = filename;
    } 
    else 
    {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.id = 'dynamicCSS';
        link.href = filename;
        document.head.appendChild(link);
    }
}