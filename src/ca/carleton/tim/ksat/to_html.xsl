<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:template match="/">
		<html>
			<body>
				<h2>Analysis Report</h2>
				<table width="100%" border="0" cellspacing="0" cellpadding="1">
					<tr>
						<th colspan="2" scope="colgroup" bgcolor="#2efbdf1">Legend</th>
					</tr>
					<tr>
						<td width="50%">
							<table border="1" cellpadding="1" cellspacing="2">
								<tr>
									<th scope="col" align="left" bgcolor="#9acd32">Site</th>
									<th scope="col" align="left" bgcolor="#9acd32">Url</th>
								</tr>
								<xsl:for-each select="analysis-report/analysis/sites/site">
									<tr>
										<td scope="row">
											<xsl:value-of select="@id" />
										</td>
										<td>
											<xsl:value-of select="text()" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</td>
						<td width="50%">
							<table border="1" cellpadding="1" cellspacing="2">
								<tr>
									<th scope="col" align="left" bgcolor="#9acd32">Keyword</th>
									<th scope="col" align="left" bgcolor="#9acd32">Expression</th>
								</tr>
								<xsl:for-each select="analysis-report/analysis/keywords/keyword">
									<tr>
										<td scope="row">
											<xsl:value-of select="@id" />
										</td>
										<td>
											<xsl:value-of select="text()" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</td>
					</tr>
				</table>
				<table border="1" cellpadding="1" cellspacing="2">
					<xsl:variable name="numKeywords"
						select="count(//analysis-report/analysis/keywords/keyword)" />
					<xsl:variable name="numKeywordsPlus" select="$numKeywords+1" />
					<colgroup></colgroup>
					<colgroup span="{$numKeywords}/"></colgroup>
					<tr>
						<th colspan="{$numKeywordsPlus}" scope="colgroup" bgcolor="#9acd32">Results</th>
					</tr>
					<xsl:for-each select="//analysis-report/analysis/results/result">
						<tr>
							<th colspan="{$numKeywordsPlus}" scope="colgroup" bgcolor="#9acd32">
								Result
								<xsl:value-of select="@id" />
								(run time=
								<xsl:value-of select="@timestamp" />
								)
							</th>
						</tr>
						<tr>
							<th rowspan="2">Site</th>
							<th colspan="{$numKeywords}" bgcolor="#9acd32">Page Counts</th>
						</tr>
						<tr>
							<xsl:for-each select="//analysis-report/analysis/keywords/keyword">
								<th>
									keyword
									<xsl:value-of select="@id" />
								</th>
							</xsl:for-each>
						</tr>
						<xsl:for-each
							select="//analysis-report/analysis/results/result/site-page-counts">
							<tr>
								<xsl:choose>
									<xsl:when test="@estimated-total-pages='-1'">
										<td bgcolor="red">
											<xsl:value-of select="@site-id" />
											(site not responding)
										</td>
										<xsl:call-template name="printEmptyCells">
											<xsl:with-param name="i">
												<xsl:value-of select="number(1)" />
											</xsl:with-param>
											<xsl:with-param name="count">
												<xsl:value-of select="$numKeywords" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<td>
											<xsl:value-of select="@site-id" />
											(estimated total number of pages=
											<xsl:value-of select="@estimated-total-pages" />
											)
										</td>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:for-each select="./keyword-page-count">
									<td align="right">
										<xsl:value-of select="text()" />
									</td>
								</xsl:for-each>
							</tr>
						</xsl:for-each>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="printEmptyCells">
		<xsl:param name="i" />
		<xsl:param name="count" />
		<xsl:if test="$i &lt;= $count">
			<td>
				<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
			</td>
		</xsl:if>
		<xsl:if test="$i &lt;= $count">
			<xsl:call-template name="printEmptyCells">
				<xsl:with-param name="i">
					<xsl:value-of select="$i + 1" />
				</xsl:with-param>
				<xsl:with-param name="count">
					<xsl:value-of select="$count" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>