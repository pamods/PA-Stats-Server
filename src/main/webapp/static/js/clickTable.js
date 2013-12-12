$(document).ready(function() {
	$('body tr').click(function(event) {
		console.log(event.which);
		
		var href = $(this).find("a").attr("href");
		if (href && event.which == 1) {
			window.location = href;
		}
		if (href && event.which == 2) {
			window.open(href, '_blank');
		}
	});
});