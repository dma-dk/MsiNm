
/**
 * This file defines the function addBaseMapLayers(layers), which
 * adds the base layers to the layers array.
 *
 * The file can be overridden to provide system specific layers
 */

function addBaseMapLayers(layers) {

    layers.push(new OpenLayers.Layer.OSM("OpenStreetMap", [
        '//a.tile.openstreetmap.org/${z}/${x}/${y}.png',
        '//b.tile.openstreetmap.org/${z}/${x}/${y}.png',
        '//c.tile.openstreetmap.org/${z}/${x}/${y}.png' ], null));


    layers.push(new OpenLayers.Layer.Google(
        "Google Hybrid",
        {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
    ));

}