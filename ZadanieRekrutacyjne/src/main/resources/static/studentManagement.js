function getSortedStudents(headerElement) {
    $.get( "http://localhost:8080/?column=" + $(headerElement).text(), function( result )
    {
        $(document).find("table > tbody").replaceWith($(result).find("table > tbody"));
    });
}