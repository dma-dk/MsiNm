<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <!-- Match the root "weekly" element -->
    <xsl:template match="weekly">
        <messages>
            <xsl:apply-templates select="temporary|preliminary"/>
        </messages>
    </xsl:template>

    <!-- Matches T & P messages -->
    <xsl:template match="temporary|preliminary">
        <xsl:variable name="authority" select="region"/>
        <message>

            <seriesIdentifier>
                <authority><xsl:value-of select="$authority"/></authority>
                <number><xsl:value-of select="@nm_number"/></number>
                <year><xsl:value-of select="@year"/></year>
            </seriesIdentifier>

            <type>
                <xsl:choose>
                    <xsl:when test="name() = 'temporary'">TEMPORARY_NOTICE</xsl:when>
                    <xsl:otherwise>PERMANENT_NOTICE</xsl:otherwise>
                </xsl:choose>
            </type>

            <descs xsi:type="messageDescVo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <lang>en</lang>
                <title><xsl:apply-templates select="feature_types/feature_type"/></title>
                <source><xsl:value-of select="authority"/></source>
                <vicinity><xsl:value-of select="vicinities/vicinity[position() = last()]"/></vicinity>
                <description>
                    <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
                    <xsl:apply-templates select="ordered_list"/>
                    <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                </description>
            </descs>

            <xsl:for-each select="ordered_list/list_item[.//location]">
                <locations>
                    <type>POLYGON</type>
                    <xsl:for-each select=".//location">
                        <xsl:call-template name="format-point">
                            <xsl:with-param name="point" select="."/>
                        </xsl:call-template>
                    </xsl:for-each>
                </locations>
            </xsl:for-each>

            <xsl:variable name="areas">
                <xsl:for-each select="vicinities/vicinity[position() &lt; last()]">
                    <xsl:sort select="position()" data-type="number" order="descending"/>
                    <xsl:value-of select="text()"/>###
                </xsl:for-each>
                <xsl:if test="sub_region"><xsl:value-of select="sub_region"/>###</xsl:if>
                <xsl:value-of select="region"/>###
            </xsl:variable>

            <area>
                <xsl:call-template name="format-areas">
                    <xsl:with-param name="areas" select="$areas"/>
                </xsl:call-template>
            </area>

            <xsl:for-each select="feature_types/feature_type">
                <categories>
                    <descs xsi:type="categoryDescVo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <lang>en</lang>
                        <name><xsl:value-of select="text()"/></name>
                    </descs>
                </categories>
            </xsl:for-each>

            <xsl:for-each select=".//chart_numbers">
                <xsl:call-template name="format-chart">
                    <xsl:with-param name="chart" select="."/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:for-each select=".//paragraph[starts-with(text(),'Former Notice ') and contains(text(), 'is cancelled.')]">
                <xsl:variable name="serId" select="substring-after(substring-before(text(), ' is cancelled.'), 'Former Notice ')"/>
                <references>
                    <seriesIdentifier>
                        <authority><xsl:value-of select="$authority"/></authority>
                        <number><xsl:value-of select="substring-before($serId, '(')"/></number>
                        <year>20<xsl:value-of select="substring-after($serId, '/')"/></year>
                    </seriesIdentifier>
                    <type>CANCELLATION</type>
                </references>
            </xsl:for-each>
        </message>
    </xsl:template>

    <!-- Output the text of the feature_type element -->
    <xsl:template match="feature_type">
        <xsl:value-of select="text()" />.
    </xsl:template>

    <!-- formats the location into a decimal point -->
    <xsl:template name="format-point">
        <xsl:param name="point" />
        <points>
            <index><xsl:value-of select="position()"/></index>
            <lat>
                <xsl:call-template name="format-lat-lon">
                    <xsl:with-param name="value" select="$point/latitude"/>
                    <xsl:with-param name="pos-hem" select="'N'"/>
                </xsl:call-template>
            </lat>
            <lon>
                <xsl:call-template name="format-lat-lon">
                    <xsl:with-param name="value" select="$point/longitude"/>
                    <xsl:with-param name="pos-hem" select="'E'"/>
                </xsl:call-template>
            </lon>
        </points>
    </xsl:template>

    <!-- formats either lat or lon -->
    <xsl:template name="format-lat-lon">
        <xsl:param name="value" />
        <xsl:param name="pos-hem" />
        <xsl:choose>
            <xsl:when test="$value/decimals"><xsl:if test="$value/hemisphere != $pos-hem">-</xsl:if><xsl:value-of select="$value/degrees + $value/minutes div 60.0 + $value/decimals div 6000.0"/></xsl:when>
            <xsl:otherwise><xsl:if test="$value/hemisphere != $pos-hem">-</xsl:if><xsl:value-of select="$value/degrees + $value/minutes div 60.0 + $value/seconds div 3600.0"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- formats the chart -->
    <xsl:template name="format-chart">
        <xsl:param name="chart" />
        <charts>
            <chartNumber><xsl:value-of select="$chart/chart_number/prefix"/><xsl:value-of select="$chart/chart_number/infix"/><xsl:value-of select="$chart/chart_number/suffix"/></chartNumber>
            <xsl:if test="$chart/international_number">
                <internationalNumber><xsl:value-of select="$chart/international_number"/></internationalNumber>
            </xsl:if>
        </charts>
    </xsl:template>

    <!-- formats the areas -->
    <xsl:template name="format-areas">
        <xsl:param name="areas"/>

        <descs xsi:type="areaDescVo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <lang>en</lang>
            <name><xsl:value-of select="substring-before($areas, '###')"/></name>
        </descs>
        <xsl:variable name="otherAreas" select="substring-after($areas, '###')"/>
        <xsl:if test="contains($otherAreas, '###')">
            <parent>
                <xsl:call-template name="format-areas">
                    <xsl:with-param name="areas" select="$otherAreas"/>
                </xsl:call-template>
            </parent>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ordered_list">
        <ol><xsl:apply-templates select="list_item"/></ol>
    </xsl:template>

    <xsl:template match="list_item">
        <li><xsl:apply-templates/></li>
    </xsl:template>

    <!-- ============================================================= -->
    <!--  From UKHO weekly NM package                                  -->
    <!--                                                               -->
    <!-- ============================================================= -->

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- ptlist.xsl -->
    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <xsl:template match="chart_numbers">
        <xsl:apply-templates select="chart_number"/>
    </xsl:template>

    <xsl:template match="chart_number">
        <xsl:value-of select="prefix"/>
        <xsl:value-of select="infix"/>
        <xsl:value-of select="suffix"/>
    </xsl:template>


    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- weekly.xsl -->
    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <xsl:template match = "paragraph">
        <xsl:apply-templates/><br/>
    </xsl:template>

    <xsl:template match="location">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="latitude">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="longitude">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="degrees">
        <xsl:value-of select="."/>&#176; <!-- #176 is a degree symbol -->
    </xsl:template>

    <xsl:template match="minutes">
        <xsl:value-of select="."/>'
    </xsl:template>

    <xsl:template match="decimals">
        &#0183;<xsl:value-of select="."/> <!-- #0183 is a middle dot symbol -->
    </xsl:template>

    <xsl:template match="seconds">
        <xsl:value-of select="."/>"
    </xsl:template>

    <xsl:template match="hemisphere">
        <span  style="text-transform:uppercase">
            <xsl:value-of select="."/>&#160; <!-- #160 is nbsp -->
        </span>
    </xsl:template>

    <xsl:template match="emphasis">
        <i><xsl:apply-templates/></i>
    </xsl:template>

    <xsl:template match="strong">
        <b><xsl:apply-templates/></b>
    </xsl:template>

    <xsl:template match = "subscript">
        <SUB><FONT SIZE="3"><xsl:apply-templates/></FONT></SUB>
    </xsl:template>

    <xsl:template match = "superscript">
        <SUP><FONT SIZE="3"><xsl:apply-templates/></FONT></SUP>
    </xsl:template>

    <xsl:template match = "underscore">
        <u><xsl:apply-templates/></u>
    </xsl:template>

    <xsl:template match = "TABLE">
        <xsl:copy>
            <xsl:copy-of select="@width"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match = "CAPTION|THEAD|TFOOT|TBODY|COLGROUP|COL|TR|TH|TD">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="nm_symbol">
        <span style="font-family:{@face}; font-size:18pt"><xsl:value-of select="."/></span>
    </xsl:template>

    <xsl:template match="graphic">
        <a target="_blank">
            <xsl:attribute name="href"><xsl:value-of select="@file_spec"/></xsl:attribute>
            <xsl:value-of select="@type_of_graphic"/>
        </a>&#160; <!-- a nbsp -->
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
