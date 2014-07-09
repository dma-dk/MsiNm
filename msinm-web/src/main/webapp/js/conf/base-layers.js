
/**
 * This file defines the function addBaseMapLayers(layers), which
 * adds the base layers to the layers array.
 *
 * The file can be overridden to provide system specific layers
 */

function addBaseMapLayers(layers) {

    layers.push(new OpenLayers.Layer.OSM("OpenStreetMap"));

    layers.push(new OpenLayers.Layer.Google(
        "Google Hybrid",
        {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
    ));

    /**
     * Example WMS layer
    layers.push(new OpenLayers.Layer.WMS("WMS", "http://kortforsyningen.kms.dk/", {
        layers : 'cells',
        servicename : 'XXXXX',
        transparent : 'true',
        styles : 'default',
        login : 'XXXXX',
        password : 'XXXXX'
    }, {
        isBaseLayer : false,
        visibility : false,
        projection : 'EPSG:3857'
    }));
    */

    /**
     * Other examples:
     * In order to use google, make sure the html file includes;
     * <script src="http://maps.google.com/maps/api/js?v=3&amp;sensor=false"></script>

    layers.push(new OpenLayers.Layer.Google(
        "Google Physical",
        {type: google.maps.MapTypeId.TERRAIN}
    ));

    layers.push(new OpenLayers.Layer.Google(
        "Google Streets",
        {numZoomLevels: 20}
    ));

    layers.push(new OpenLayers.Layer.Google(
        "Google Hybrid",
        {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
    ));

    layers.push(new OpenLayers.Layer.Google(
        "Google Satellite",
        {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}
    ));
    */


}