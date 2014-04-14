$.urlParam = function(name) {
	var results = new RegExp('[\\?&]' + name + '=([^&#]*)')
			.exec(window.location.href);
	return results == null ? undefined : (results[1] || undefined);
}

Array.prototype.extend = function(other_array) {
	for (var i = 0; i < other_array.length; i++) {
		var v = other_array[i];
		this.push(v);
	}
}

var fillZeroes = "00000000000000000000"; // max number of zero fill ever
// asked for in global

function zeroFill(number, width) {
	// make sure it's a string
	var input = number + "";
	var prefix = "";
	if (input.charAt(0) === '-') {
		prefix = '-';
		input = input.slice(1);
		--width;
	}
	var fillAmt = Math.max(width - input.length, 0);
	return prefix + fillZeroes.slice(0, fillAmt) + input;
}

function fmtTime(firstTime, n) {
	var secs = (n - firstTime) / 1000;

	if (secs < 0) {
		return "";
	}

	var min = Math.floor(secs / 60);
	var sec = Math.floor(secs % 60);
	return zeroFill(min, 2) + ":" + zeroFill(sec, 2);
}

// http://stackoverflow.com/questions/12856112/using-knockout-js-with-jquery-ui-sliders
ko.bindingHandlers.slider = {
	init : function(element, valueAccessor, allBindingsAccessor) {
		var options = allBindingsAccessor().sliderOptions() || {};

		$(element).slider(options);
		ko.utils.registerEventHandler(element, "slidechange", function(event,
				ui) {
			var observable = valueAccessor();
			observable(ui.value);
		});
		ko.utils.domNodeDisposal.addDisposeCallback(element, function() {
			$(element).slider("destroy");
		});
		ko.utils.registerEventHandler(element, "slide", function(event, ui) {
			var observable = valueAccessor();
			observable(ui.value);
		});
	},
	update : function(element, valueAccessor, allBindingsAccessor) {
		var value = ko.utils.unwrapObservable(valueAccessor());
		if (isNaN(value))
			value = 0;
		$(element).slider("value", value);

		$(element).slider("option", allBindingsAccessor().sliderOptions());

	}
};
