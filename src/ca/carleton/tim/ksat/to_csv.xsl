<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<xsl:strip-space elements="*" />
<xsl:output indent="no" media-type="text/plain" method="text" omit-xml-declaration="yes" />
<xsl:template match="/">Site id,Estimated Size of Site<xsl:for-each select="analysis-report/analysis/keywords/keyword">
<xsl:text>,</xsl:text>e<xsl:value-of select="@id" />
</xsl:for-each>
<xsl:text>&#13;</xsl:text>
<xsl:for-each select="analysis-report/analysis/results/result/site-page-counts">
<xsl:value-of select="@site-id" />
<xsl:text>,</xsl:text>
<xsl:choose>
<xsl:when test="@estimated-total-pages>'-1'">
<xsl:value-of select="@estimated-total-pages" />
</xsl:when>
<xsl:otherwise>
<xsl:text>,</xsl:text>
<xsl:call-template name="printEmptyCells">
<xsl:with-param name="i">
<xsl:value-of select="number(1)" />
</xsl:with-param>
<xsl:with-param name="count">
<xsl:value-of select="count(//analysis-report/analysis/keywords/keyword)"/>
</xsl:with-param>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
<xsl:for-each select="keyword-page-count">
<xsl:text>,</xsl:text>
<xsl:value-of select="text()" />
</xsl:for-each>
<xsl:text>&#13;</xsl:text>
</xsl:for-each>
</xsl:template>

<xsl:template name="printEmptyCells">
  <xsl:param name="i" />
  <xsl:param name="count" />
  <xsl:if test="$i &lt;= $count">
    <xsl:text>,</xsl:text>
  </xsl:if>
  <xsl:if test="$i &lt;= $count">
    <xsl:call-template name="printEmptyCells">
      <xsl:with-param name="i">
        <xsl:value-of select="$i+1" />
      </xsl:with-param>
      <xsl:with-param name="count">
        <xsl:value-of select="$count" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>