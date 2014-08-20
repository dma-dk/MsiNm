


Date.prototype.ddmmyyyy = function() {
    var yyyy = this.getFullYear().toString();
    var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
    var dd  = this.getDate().toString();
    return (dd[1] ? dd : "0" + dd[0]) + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + yyyy;
};

Date.prototype.formatDate = function(format){

    var separator = format.match(/[.\/\-\s].*?/),
        parts = format.split(/\W+/);
    if (!separator || !parts || parts.length === 0){
        throw new Error("Invalid date format.");
    }
    var fmt = {separator: separator, parts: parts};


    var val = {
        d: this.getDate(),
        m: this.getMonth() + 1,
        yy: this.getFullYear().toString().substring(2),
        yyyy: this.getFullYear()
    };
    val.dd = (val.d < 10 ? '0' : '') + val.d;
    val.mm = (val.m < 10 ? '0' : '') + val.m;
    var date = [];
    for (var i=0, cnt = fmt.parts.length; i < cnt; i++) {
        date.push(val[fmt.parts[i]]);
    }
    return date.join(fmt.separator);
},


Date.prototype.getWeek = function() {
    var onejan = new Date(this.getFullYear(),0,1);
    return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
};

String.prototype.endsWith = function (s) {
    return this.length >= s.length && this.substr(this.length - s.length) == s;
};

String.prototype.contains = function (s) {
    return this.indexOf(s) > -1;
};

String.prototype.extension = function () {
    return this.substr((~-this.lastIndexOf(".") >>> 0) + 2);
};


function getLang(defaultLang) {
    try {
        return ('localStorage' in window && window['localStorage'] !== null)
                ? window.localStorage.getItem("lang")
                : defaultLang;
    } catch (e) {
    }
    return defaultLang;
}


/** Area selection **/

function formatParentAreas(area) {
    var txt = undefined;
    if (area) {
        txt = (area.descs && area.descs.length > 0) ? area.descs[0].name : 'N/A';
        if (area.parent) {
            txt = formatParentAreas(area.parent) + " - " + txt;
        }
    }
    return txt;
}

function initAreaField(areaId, multiple) {
    $(document).ready(function () {
        $(areaId).select2({
            placeholder: (multiple) ? "Select Areas" : "Select Area",
            multiple: multiple,
            allowClear: true,
            minimumInputLength: 1,
            type: "GET",
            quietMillis: 50,
            ajax: {
                url: "/rest/admin/areas/search",
                dataType: 'json',
                data: function (term, page) {
                    return {
                        term: term,
                        lang: getLang("en"),
                        limit: 10
                    };
                },
                results: function (data, page) {
                    var results = [];
                    for (i in data) {
                        var area = data[i];
                        results.push({
                            id: area.id,
                            text: area.descs[0].name,
                            parent: formatParentAreas(area.parent)
                        });
                    }
                    return { results: results };
                }
            },
            formatResult: function (data, term) {
                var txt = "<strong>" + data.text + "</strong>";
                if (data.parent) {
                    txt = txt + " <small>(" + data.parent + ")</small>";
                }
                return txt;
            }
        });
    });
}

/** Category selection **/

function formatParentCategories(category) {
    var txt = undefined;
    if (category) {
        txt = (category.descs && category.descs.length > 0) ? category.descs[0].name : 'N/A';
        if (category.parent) {
            txt = formatParentCategories(category.parent) + " - " + txt;
        }
    }
    return txt;
}

function initCategoryField(categoryId, multiple) {
    $(document).ready(function () {
        $(categoryId).select2({
            placeholder: (multiple) ? "Select Categores" : "Select Category",
            multiple: multiple,
            allowClear: true,
            minimumInputLength: 1,
            type: "GET",
            quietMillis: 50,
            ajax: {
                url: "/rest/admin/categories/search",
                dataType: 'json',
                data: function (term, page) {
                    return {
                        term: term,
                        lang: getLang("en"),
                        limit: 10
                    };
                },
                results: function (data, page) {
                    var results = [];
                    for (i in data) {
                        var category = data[i];
                        results.push({
                            id: category.id,
                            text: category.descs[0].name,
                            parent: formatParentCategories(category.parent)
                        });
                    }
                    return { results: results };
                }
            },
            formatResult: function (data, term) {
                var txt = "<strong>" + data.text + "</strong>";
                if (data.parent) {
                    txt = txt + " <small>(" + data.parent + ")</small>";
                }
                return txt;
            }
        });
    });
}

function initChartField(chartId, multiple) {
    $(document).ready(function () {
        $(chartId).select2({
            placeholder: (multiple) ? "Select Charts" : "Select Chart",
            multiple: multiple,
            allowClear: true,
            minimumInputLength: 1,
            type: "GET",
            quietMillis: 50,
            ajax: {
                url: "/rest/admin/charts/search",
                dataType: 'json',
                data: function (term, page) {
                    return {
                        term: term,
                        limit: 10
                    };
                },
                results: function (data, page) {
                    var results = [];
                    for (i in data) {
                        var chart = data[i];
                        if (chart.internationalNumber) {
                            results.push({ id: chart.id, text: chart.chartNumber + ' (INT ' + chart.internationalNumber + ')' });
                        } else {
                            results.push({ id: chart.id, text: chart.chartNumber });
                        }
                    }
                    return { results: results };
                }
            },
            formatResult: function (data, term) {
                return data.text;
            }
        });
    });
}