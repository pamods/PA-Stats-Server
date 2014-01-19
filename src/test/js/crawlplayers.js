var page = require('webpage').create(),
    system = require('system'),
    address;

address = "127.0.0.1:8080/listgames?listpage=1";


page.open(address, function (status) {
    if (status !== 'success') {
        console.log('Unable to access network');
    } else {
    	console.log($);
        var p = page.evaluate(function () {
        	var links = $.map($(".gamenum a"), function(elem) {return $(elem).attr('href');});
        	var randomLink = links[Math.floor(links.length * Math.random())];
        	return randomLink;
        });
        console.log(p);
    }
});


