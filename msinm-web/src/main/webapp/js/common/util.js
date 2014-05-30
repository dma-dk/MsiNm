


Date.prototype.ddmmyyyy = function() {
    var yyyy = this.getFullYear().toString();
    var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
    var dd  = this.getDate().toString();
    return (dd[1] ? dd : "0" + dd[0]) + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + yyyy;
};


String.prototype.endsWith = function (s) {
    return this.length >= s.length && this.substr(this.length - s.length) == s;
};
