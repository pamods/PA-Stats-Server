$.urlParam = function(name){
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    return results == null ? undefined : (results[1] || undefined);
}


var fillZeroes = "00000000000000000000";  // max number of zero fill ever asked for in global

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