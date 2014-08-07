


Date.prototype.ddmmyyyy = function() {
    var yyyy = this.getFullYear().toString();
    var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
    var dd  = this.getDate().toString();
    return (dd[1] ? dd : "0" + dd[0]) + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + yyyy;
};

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