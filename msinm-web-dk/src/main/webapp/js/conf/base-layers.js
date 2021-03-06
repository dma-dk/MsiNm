
/**
 * This file defines the function addBaseMapLayers(layers), which
 * adds the base layers to the layers array.
 *
 * The file can be overridden to provide system specific layers
 */

function addBaseMapLayers(layers) {

    // layers.push(new OpenLayers.Layer.OSM("OSM", [ '//osm.e-navigation.net/${z}/${x}/${y}.png' ], null));
    layers.push(new OpenLayers.Layer.OSM("OSM", [
        '//a.tile.openstreetmap.org/${z}/${x}/${y}.png',
        '//b.tile.openstreetmap.org/${z}/${x}/${y}.png',
        '//c.tile.openstreetmap.org/${z}/${x}/${y}.png' ], null));

    layers.push(new OpenLayers.Layer.Google(
        "Google",
        {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
    ));

    // NB WMS layer gets proxied via "/wms/" to mask out colors and hide service-name, login and password
    // For direct access, substitute "/wms/" with: http://kortforsyningen.kms.dk/
    layers.push(new OpenLayers.Layer.WMS("WMS", "/wms/", {
            layers : 'cells',
            transparent : 'true',
            styles : 'default'
        }, {
            isBaseLayer : false,
            visibility : false,
            projection : 'EPSG:3857'
    }));

    /*
     * Test layer - MSI displayed as bitmaps
    layers.push(new OpenLayers.Layer.OSM("MsiLayer", "http://localhost:8080/msi/${z}/${x}/${y}.png", {
        displayInLayerSwitcher: false,
        'isBaseLayer': false
    }));
    */

}