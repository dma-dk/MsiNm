<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="weekly">
        <messages>
            <xsl:apply-templates select="temporary|preliminary"/>
        </messages>
    </xsl:template>

    <xsl:template match="temporary|preliminary">
        <message>

            <seriesIdentifier>
                <authority><xsl:value-of select="authority"/></authority>
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
                <title>
                    <xsl:apply-templates select="feature_types/feature_type"/>
                </title>
            </descs>

        </message>
    </xsl:template>

    <xsl:template match="feature_type">
        <xsl:value-of select="text()" />.
    </xsl:template>

</xsl:stylesheet>
